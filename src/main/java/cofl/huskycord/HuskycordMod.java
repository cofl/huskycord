package cofl.huskycord;

import net.fabricmc.api.ModInitializer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuskycordMod implements ModInitializer {
    public static final String MOD_NAME = "huskycord";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
    public static final TagKey<Block> DISPENSER_PLACEABLE = TagKey.create(
            Registries.BLOCK,
            ResourceLocation.fromNamespaceAndPath(MOD_NAME, "dispenser_placeable"));

    @Override
    public void onInitialize() {
        Graves.register();
        //BannerPOI.register();
        LOGGER.info("Initialized HuskyCord.");
    }
}
