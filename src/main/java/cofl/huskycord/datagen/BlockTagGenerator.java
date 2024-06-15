package cofl.huskycord.datagen;

import cofl.huskycord.Graves;
import cofl.huskycord.HuskycordMod;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class BlockTagGenerator extends FabricTagProvider.BlockTagProvider {
    public BlockTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(HuskycordMod.DISPENSER_PLACEABLE).add(
            Blocks.SNOW_BLOCK
        );

        getOrCreateTagBuilder(Graves.GRAVE_SINK_IF_OPEN)
            .forceAddTag(BlockTags.TRAPDOORS);

        getOrCreateTagBuilder(Graves.GRAVE_SUPPORTS).add(
            Blocks.FROGSPAWN,
            Blocks.SNOW,
            Blocks.LILY_PAD,
            Blocks.CAULDRON,
            Blocks.WATER_CAULDRON,
            Blocks.CAKE
        )   .forceAddTag(BlockTags.CANDLE_CAKES);

        getOrCreateTagBuilder(Graves.GRAVE_PLACEABLE).add(
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.POWDER_SNOW
        )   .forceAddTag(BlockTags.AIR)
            .forceAddTag(Graves.GRAVE_SUPPORTS);

        getOrCreateTagBuilder(Graves.GRAVE_DANGEROUS_FLOAT).add(
            Blocks.LAVA
        );

        getOrCreateTagBuilder(Graves.GRAVE_FLOAT).add(
            Blocks.WATER
        )   .forceAddTag(Graves.GRAVE_DANGEROUS_FLOAT);

        getOrCreateTagBuilder(Graves.GRAVE_SINK).add(
            Blocks.WATER,
            Blocks.LAVA,
            Blocks.CAKE,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.SMALL_DRIPLEAF,
            Blocks.CRIMSON_FUNGUS,
            Blocks.CRIMSON_ROOTS,
            Blocks.WARPED_FUNGUS,
            Blocks.WARPED_ROOTS,
            Blocks.BELL,
            Blocks.COBWEB,
            Blocks.SNOW,
            Blocks.POWDER_SNOW,
            Blocks.LANTERN,
            Blocks.SOUL_LANTERN,
            Blocks.TORCH,
            Blocks.SOUL_TORCH,
            Blocks.REDSTONE_TORCH,
            Blocks.WALL_TORCH,
            Blocks.REDSTONE_WALL_TORCH,
            Blocks.SOUL_WALL_TORCH,
            Blocks.REDSTONE_WIRE,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK
        )   .forceAddTag(BlockTags.AIR)
            .forceAddTag(BlockTags.FIRE)
            .forceAddTag(BlockTags.DOORS)
            .forceAddTag(BlockTags.ALL_HANGING_SIGNS)
            .forceAddTag(BlockTags.ALL_SIGNS)
            .forceAddTag(BlockTags.BANNERS)
            .forceAddTag(BlockTags.CANDLE_CAKES)
            .forceAddTag(BlockTags.CANDLES)
            .forceAddTag(BlockTags.BUTTONS)
            .forceAddTag(BlockTags.PRESSURE_PLATES)
            .forceAddTag(BlockTags.CAULDRONS)
            .forceAddTag(BlockTags.CLIMBABLE)
            .forceAddTag(BlockTags.FLOWER_POTS)
            .forceAddTag(BlockTags.FLOWERS)
            .forceAddTag(BlockTags.SAPLINGS)
            .forceAddTag(BlockTags.CORAL_PLANTS)
            .forceAddTag(BlockTags.WALL_CORALS)
            .forceAddTag(BlockTags.PORTALS);
    }
}
