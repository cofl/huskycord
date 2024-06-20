package cofl.huskycord;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class PlayerGraveData extends SavedData {
    private static final String NBT_NAME = "player_grave_data";

    public record GravePosition(UUID id, Vec3 position, ResourceKey<Level> dimension, LocalDateTime dateTime){
        public GravePosition(ArmorStand grave, LocalDateTime dateTime){
            this(grave.getUUID(), grave.position(), grave.level().dimension(), dateTime);
        }
    }

    private final HashMap<UUID, List<GravePosition>> ALL_PLAYERS = new HashMap<>();
    private static final SavedData.Factory<PlayerGraveData> FACTORY = new SavedData.Factory<>(
        PlayerGraveData::new,
        PlayerGraveData::fromTag,
        null
    );

    public static PlayerGraveData getServerState(MinecraftServer server){
        var manager = Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getDataStorage();
        return manager.computeIfAbsent(FACTORY, HuskycordMod.MOD_NAME);
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        var players = new CompoundTag();
        ALL_PLAYERS.forEach((uuid, gravePositions) -> {
            var list = new ListTag();
            for (var position: gravePositions){
                var tag = new CompoundTag();
                tag.putUUID("uuid", position.id());
                tag.putFloat("x", (float)position.position().x());
                tag.putFloat("y", (float)position.position().y());
                tag.putFloat("z", (float)position.position().z());
                tag.putString("dimension", position.dimension().location().toString());
                tag.putLong("time", position.dateTime().toEpochSecond(ZoneOffset.UTC));
                list.add(tag);
            }
            players.put(uuid.toString(), list);
        });

        compoundTag.put(NBT_NAME, players);
        return compoundTag;
    }

    private static PlayerGraveData fromTag(CompoundTag tag, HolderLookup.Provider provider){
        var state = new PlayerGraveData();
        var players = tag.getCompound(NBT_NAME);
        for(var key: players.getAllKeys()){
            var uuid = UUID.fromString(key);
            var player = players.getList(key, 10);
            var list = state.ALL_PLAYERS.computeIfAbsent(uuid, k -> new ArrayList<>());
            for(var i = 0; i < player.size(); i += 1){
                var grave = player.getCompound(i);
                list.add(new GravePosition(
                    grave.getUUID("uuid"),
                    new Vec3(
                        grave.getFloat("x"),
                        grave.getFloat("y"),
                        grave.getFloat("z")),
                    ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(grave.getString("dimension"))),
                    LocalDateTime.ofEpochSecond(grave.getLong("time"), 0, ZoneOffset.UTC)
                ));
            }
        }

        return state;
    }

    private List<GravePosition> getPlayerGraves(GameProfile profile){
        return ALL_PLAYERS.computeIfAbsent(profile.getId(), k -> new ArrayList<>());
    }

    public void add(ArmorStand grave){
        var item = Graves.getGraveItem(grave);
        var owner = Graves.getGraveOwner(item);
        getPlayerGraves(owner.gameProfile()).add(new GravePosition(grave, LocalDateTime.now()));
        setDirty();
    }

    public void remove(ArmorStand grave){
        var item = Graves.getGraveItem(grave);
        var owner = Graves.getGraveOwner(item);
        var list = getPlayerGraves(owner.gameProfile());
        if(list.removeIf(a -> a.id().equals(grave.getUUID())))
            setDirty();
    }

    public List<GravePosition> getGraves(ServerPlayer player) {
        return getPlayerGraves(player.getGameProfile());
    }
}
