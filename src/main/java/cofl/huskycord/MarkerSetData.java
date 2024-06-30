package cofl.huskycord;

import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

import static cofl.huskycord.HuskycordMod.LOGGER;

public final class MarkerSetData extends SavedData {
    private static final String NBT_NAME = "huskycord_poi_data";
    private static final SavedData.Factory<MarkerSetData> FACTORY = new SavedData.Factory<>(
        MarkerSetData::new,
        MarkerSetData::fromTag,
        null
    );
    public static final String DEFAULT_LABEL = "Points of Interest";

    private final MarkerSet markerSet;
    private final HashMap<DyeColor, String> assets;

    public MarkerSetData(){
        this.markerSet = new MarkerSet(DEFAULT_LABEL);
        this.assets = new HashMap<>();
    }

    public MarkerSetData(MarkerSet markerSet, HashMap<DyeColor, String> assets) {
        this.markerSet = markerSet;
        this.assets = assets;
    }
    public MarkerSet markerSet() { return markerSet; }

    public static MarkerSetData get(ServerLevel level){
        return level.getDataStorage().computeIfAbsent(FACTORY, NBT_NAME);
    }

    private static String id(BlockEntity banner){
        return id(banner.getBlockPos());
    }

    private static String id(BlockPos pos){
        return pos.toShortString();
    }

    public void add(BannerBlockEntity banner){
        var name = banner.hasCustomName()
            ? Objects.requireNonNull(banner.getCustomName()).getString()
            : banner.getBlockState().getBlock().getName().getString();
        if(name.isBlank())
            return;
        var position = banner.getBlockPos().getCenter();
        var builder = POIMarker.builder()
            .label(name)
            .position(position.x(), position.y(), position.z());
        if (assets.containsKey(banner.getBaseColor()))
            builder = builder.icon(assets.get(banner.getBaseColor()), 0, 0);
        var marker = builder.build();
        this.markerSet.put(id(banner), marker);
        this.setDirty(true);
        LOGGER.info("Added marker {} -- {}", id(banner), name);
    }

    public void remove(BannerBlockEntity banner){
        remove(banner.getBlockPos());
    }

    public void remove(BlockPos pos){
        if(null != this.markerSet.remove(id(pos)))
            LOGGER.info("Removed marker {}", id(pos));
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        var set = new CompoundTag();
        set.putString("label", markerSet.getLabel());
        set.putInt("sorting", markerSet.getSorting());
        set.putBoolean("is_default_hidden", markerSet.isDefaultHidden());
        set.putBoolean("is_toggleable", markerSet.isToggleable());

        var markers = new CompoundTag();
        for (var entry : markerSet.getMarkers().entrySet()) {
            if (entry.getValue() instanceof POIMarker poi) {
                if(poi.getLabel().isEmpty())
                    continue;
                var tag = new CompoundTag();
                tag.putString("label", poi.getLabel());
                tag.putString("type", poi.getType());
                tag.putBoolean("listed", poi.isListed());
                tag.putInt("sorting", poi.getSorting());
                tag.putString("detail", poi.getDetail());
                tag.putDouble("min", poi.getMinDistance());
                tag.putDouble("max", poi.getMaxDistance());

                var position = poi.getPosition();
                tag.putDouble("pos_x", position.getX());
                tag.putDouble("pos_y", position.getY());
                tag.putDouble("pos_z", position.getZ());

                var anchor = poi.getAnchor();
                tag.putInt("anchor_x", anchor.getX());
                tag.putInt("anchor_y", anchor.getY());
                tag.putString("icon_address", poi.getIconAddress());
                tag.putString("classes", String.join("\0", poi.getStyleClasses()));

                markers.put(entry.getKey(), tag);
            }
        }
        set.put("markers", markers);

        var assets = new CompoundTag();
        for (var entry : this.assets.entrySet()){
            assets.putString(entry.getKey().getName(), entry.getValue());
        }
        set.put("assets", assets);

        return set;
    }

    private static MarkerSetData fromTag(CompoundTag tag, HolderLookup.Provider provider){
        var set = tag.getCompound(NBT_NAME);
        var markerSet = new MarkerSet(set.contains("label") ? set.getString("label") : DEFAULT_LABEL);
        if (set.contains("sorting"))
            markerSet.setSorting(set.getInt("sorting"));
        if (set.contains("is_default_hidden"))
            markerSet.setDefaultHidden(set.getBoolean("is_default_hidden"));
        if (set.contains("is_toggleable"))
            markerSet.setToggleable(set.getBoolean("is_toggleable"));

        var markers = tag.getCompound("markers");
        for(var id: markers.getAllKeys()){
            var markerTag = markers.getCompound(id);
            var marker = POIMarker.builder()
                .label(markerTag.getString("label"))
                .listed(markerTag.getBoolean("listed"))
                .sorting(markerTag.getInt("sorting"))
                .detail(markerTag.getString("detail"))
                .minDistance(markerTag.getDouble("min"))
                .maxDistance(markerTag.getDouble("max"))
                .position(
                    markerTag.getDouble("pos_x"),
                    markerTag.getDouble("pos_y"),
                    markerTag.getDouble("pos_z"))
                .icon(
                    markerTag.getString("icon_address"),
                    markerTag.getInt("anchor_x"),
                    markerTag.getInt("anchor_y"))
                .styleClasses(Arrays.stream(markerTag.getString("classes").split("\0"))
                    .filter(s -> !s.isBlank())
                    .toList()
                    .toArray(new String[0]))
                .build();
            markerSet.put(id, marker);
        }

        var assets = new HashMap<DyeColor, String>();
        var assetTag = tag.getCompound("assets");
        for (var color: DyeColor.values()){
            var path = assetTag.getString(color.getName());
            assets.put(color, path.isEmpty() ? null : path);
        }

        return new MarkerSetData(markerSet, assets);
    }

    public boolean toggle(BannerBlockEntity banner) {
        if (markerSet.getMarkers().containsKey(id(banner))){
            remove(banner);
            return false;
        } else {
            add(banner);
            return true;
        }
    }

    public void importAssets(BlueMapMap map){
        var storage = map.getAssetStorage();
        for(var color: DyeColor.values()){
            var icon = color.getName() + ".png";
            try {
                if(!storage.assetExists(icon)){
                    try(var stream = storage.writeAsset(icon)){
                        try(var read = HuskycordMod.class.getResourceAsStream("/assets/huskycord/icons/banners/" + icon)){
                            if(null != read){
                                stream.write(read.readAllBytes());
                            }
                        }
                    }
                }

                var path = storage.getAssetUrl(icon);
                if (!path.equals(assets.getOrDefault(color, ""))){
                    assets.put(color, path);
                    this.setDirty(true);
                }
            } catch (IOException e){
                LOGGER.error("Loading icon {} failed.", icon, e);
            }
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (MarkerSetData) obj;
        return Objects.equals(this.markerSet, that.markerSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(markerSet);
    }

    @Override
    public String toString() {
        return "MarkerSetData[" +
            "markerSet=" + markerSet + ']';
    }
}
