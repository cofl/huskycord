package cofl.huskycord;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuskycordMod implements ModInitializer {
    public static final String MOD_NAME = "huskycord";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final TagKey<Block> DISPENSER_PLACEABLE = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "dispenser_placeable"));
    public static final TagKey<Item> CONCRETE_POWDER = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "concrete_powder"));
    public static final TagKey<Item> RETAIN_IN_CRAFTING = TagKey.create(
        Registries.ITEM,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "retain_in_crafting"));

    @Override
    public void onInitialize() {
        Graves.register();
        ServerPause.register();
        Commands.register();

        //BannerPOI.register();
        LOGGER.info("Initialized HuskyCord.");
    }
}
