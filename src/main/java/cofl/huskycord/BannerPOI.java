package cofl.huskycord;

import de.bluecolored.bluemap.api.BlueMapAPI;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import java.util.function.Consumer;

import static cofl.huskycord.HuskycordMod.LOGGER;
import static cofl.huskycord.HuskycordMod.MOD_NAME;

public class BannerPOI {
    public static void register(){
        ServerLifecycleEvents.SERVER_STARTING.register(BannerPOI::onServerStart);
        ServerLifecycleEvents.SERVER_STOPPING.register(BannerPOI::onServerStopping);
        UseBlockCallback.EVENT.register(BannerPOI::toggleBanner);

        // block breaking is handled by ServerLevelMixin
    }

    private static Consumer<BlueMapAPI> onEnableCallback;
    private static void onServerStart(MinecraftServer server) {
        BlueMapAPI.onEnable(onEnableCallback = api -> {
            try {
                for(var level: server.getAllLevels()){
                    var set = MarkerSetData.get(level);
                    api.getWorld(level).ifPresent(world -> {
                        for(var map: world.getMaps()){
                            map.getMarkerSets().put(MOD_NAME, set.markerSet());
                        }
                    });
                }
            } catch (Exception e){
                LOGGER.error("Loading markers failed.", e);
                throw e;
            }
        });
    }

    private static void onServerStopping(MinecraftServer server) {
        if (null != onEnableCallback)
            BlueMapAPI.unregisterListener(onEnableCallback);
    }

    private static InteractionResult toggleBanner(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()
            || player.isSpectator()
            || !player.getItemInHand(hand).is(Items.FILLED_MAP)
            || !level.getBlockState(hitResult.getBlockPos()).is(BlockTags.BANNERS)
            || !(level.getBlockEntity(hitResult.getBlockPos()) instanceof BannerBlockEntity banner)
            || !(level instanceof ServerLevel serverLevel))
            return InteractionResult.PASS;

        player.swing(hand, true);

        var set = MarkerSetData.get(serverLevel);
        if(set.toggle(banner)){
            level.playSound(null, player.blockPosition(),
                SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.PLAYERS,
                0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        } else {
            level.playSound(null, player.blockPosition(),
                SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.PLAYERS,
                0.5F, level.random.nextFloat() * 0.1F + 0.9F);
        }

        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
