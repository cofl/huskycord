package cofl.huskycord.datagen;

import cofl.huskycord.Graves;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class GraveTagGenerator extends FabricTagProvider.BlockTagProvider {
    public GraveTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(Graves.GRAVE_SINK_IF_OPEN)
            .forceAddTag(BlockTags.TRAPDOORS);
        getOrCreateTagBuilder(Graves.GRAVE_SUPPORTS)
            .add(Blocks.FROGSPAWN, Blocks.SNOW, Blocks.LILY_PAD)
            .add(Blocks.CAULDRON, Blocks.WATER_CAULDRON);
        getOrCreateTagBuilder(Graves.GRAVE_PLACEABLE)
            .forceAddTag(BlockTags.AIR)
            .add(Blocks.WATER, Blocks.LAVA, Blocks.POWDER_SNOW)
            .forceAddTag(Graves.GRAVE_SUPPORTS);
        getOrCreateTagBuilder(Graves.GRAVE_DANGEROUS_FLOAT)
            .add(Blocks.LAVA);
        getOrCreateTagBuilder(Graves.GRAVE_FLOAT)
            .add(Blocks.WATER)
            .forceAddTag(Graves.GRAVE_DANGEROUS_FLOAT);
        getOrCreateTagBuilder(Graves.GRAVE_SINK)
            .forceAddTag(BlockTags.AIR).add(Blocks.WATER, Blocks.LAVA)
            .forceAddTag(BlockTags.FIRE)
            .forceAddTag(BlockTags.DOORS)
            .forceAddTag(BlockTags.ALL_HANGING_SIGNS)
            .forceAddTag(BlockTags.ALL_SIGNS)
            .forceAddTag(BlockTags.BANNERS)
            .forceAddTag(BlockTags.CANDLE_CAKES).add(Blocks.CAKE)
            .forceAddTag(BlockTags.BUTTONS).forceAddTag(BlockTags.PRESSURE_PLATES)
            .forceAddTag(BlockTags.CAULDRONS)
            .forceAddTag(BlockTags.CLIMBABLE)
            .forceAddTag(BlockTags.FLOWER_POTS).forceAddTag(BlockTags.FLOWERS).forceAddTag(BlockTags.SAPLINGS)
            .forceAddTag(BlockTags.CORAL_PLANTS)
            .add(Blocks.BIG_DRIPLEAF, Blocks.BIG_DRIPLEAF_STEM, Blocks.SMALL_DRIPLEAF)
            .add(Blocks.CRIMSON_FUNGUS, Blocks.CRIMSON_ROOTS, Blocks.WARPED_FUNGUS, Blocks.WARPED_ROOTS)
            .forceAddTag(BlockTags.WALL_CORALS)
            .add(Blocks.BELL, Blocks.COBWEB)
            .forceAddTag(BlockTags.PORTALS)
            .add(Blocks.SNOW, Blocks.POWDER_SNOW)
            .add(Blocks.LANTERN, Blocks.SOUL_LANTERN, Blocks.TORCH, Blocks.SOUL_TORCH, Blocks.REDSTONE_TORCH)
            .add(Blocks.WALL_TORCH, Blocks.REDSTONE_WALL_TORCH, Blocks.SOUL_WALL_TORCH)
            .add(Blocks.REDSTONE_WIRE, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK);
    }
}
