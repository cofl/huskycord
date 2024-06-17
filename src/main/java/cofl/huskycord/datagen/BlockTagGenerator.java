package cofl.huskycord.datagen;

import cofl.huskycord.HuskycordMod;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

import static cofl.huskycord.Graves.*;

public class BlockTagGenerator extends FabricTagProvider.BlockTagProvider {
    public BlockTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(HuskycordMod.DISPENSER_PLACEABLE).add(
            Blocks.SNOW_BLOCK
        );

        getOrCreateTagBuilder(HuskycordMod.DEAD_CORALS).add(
            Blocks.DEAD_BRAIN_CORAL,
            Blocks.DEAD_BRAIN_CORAL_FAN,
            Blocks.DEAD_BRAIN_CORAL_WALL_FAN,
            Blocks.DEAD_BUBBLE_CORAL,
            Blocks.DEAD_BUBBLE_CORAL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_WALL_FAN,
            Blocks.DEAD_FIRE_CORAL,
            Blocks.DEAD_FIRE_CORAL_FAN,
            Blocks.DEAD_FIRE_CORAL_WALL_FAN,
            Blocks.DEAD_HORN_CORAL,
            Blocks.DEAD_HORN_CORAL_FAN,
            Blocks.DEAD_HORN_CORAL_WALL_FAN,
            Blocks.DEAD_TUBE_CORAL,
            Blocks.DEAD_TUBE_CORAL_FAN,
            Blocks.DEAD_TUBE_CORAL_WALL_FAN
        );

        getOrCreateTagBuilder(GRAVE_SINK).add(
            Blocks.AMETHYST_CLUSTER,
            Blocks.BAMBOO_SAPLING,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.BROWN_MUSHROOM,
            Blocks.BUBBLE_COLUMN,
            Blocks.CAVE_VINES,
            Blocks.CAVE_VINES_PLANT,
            Blocks.COBWEB,
            Blocks.COCOA,
            Blocks.CRIMSON_FUNGUS,
            Blocks.CRIMSON_ROOTS,
            Blocks.DEAD_BUSH,
            Blocks.END_PORTAL,
            Blocks.FERN,
            Blocks.FROGSPAWN,
            Blocks.GLOW_LICHEN,
            Blocks.HANGING_ROOTS,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.LADDER,
            Blocks.LANTERN,
            Blocks.LARGE_AMETHYST_BUD,
            Blocks.LARGE_FERN,
            Blocks.LAVA,
            Blocks.LEVER,
            Blocks.LIGHT,
            Blocks.MEDIUM_AMETHYST_BUD,
            Blocks.NETHER_PORTAL,
            Blocks.NETHER_SPROUTS,
            Blocks.NETHER_WART,
            Blocks.PINK_PETALS,
            Blocks.PISTON_HEAD,
            Blocks.MOVING_PISTON,
            Blocks.POWDER_SNOW,
            Blocks.RED_MUSHROOM,
            Blocks.REDSTONE_TORCH,
            Blocks.REDSTONE_WALL_TORCH,
            Blocks.REDSTONE_WIRE,
            Blocks.SCULK_VEIN,
            Blocks.SEA_PICKLE,
            Blocks.SEAGRASS,
            Blocks.SHORT_GRASS,
            Blocks.SMALL_AMETHYST_BUD,
            Blocks.SMALL_DRIPLEAF,
            Blocks.SNIFFER_EGG,
            Blocks.SOUL_LANTERN,
            Blocks.SOUL_TORCH,
            Blocks.SOUL_WALL_TORCH,
            Blocks.SPORE_BLOSSOM,
            Blocks.STRUCTURE_VOID,
            Blocks.SUGAR_CANE,
            Blocks.SWEET_BERRY_BUSH,
            Blocks.TALL_GRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.TORCH,
            Blocks.WALL_TORCH,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.TWISTING_VINES,
            Blocks.TWISTING_VINES_PLANT,
            Blocks.VINE,
            Blocks.WARPED_FUNGUS,
            Blocks.WARPED_ROOTS,
            Blocks.WATER,
            Blocks.WEEPING_VINES,
            Blocks.WEEPING_VINES_PLANT
        )
            .forceAddTag(BlockTags.AIR)
            .forceAddTag(BlockTags.SIGNS)
            .forceAddTag(BlockTags.WALL_HANGING_SIGNS)
            .forceAddTag(BlockTags.DOORS)
            .forceAddTag(BlockTags.BUTTONS)
            .forceAddTag(BlockTags.FIRE)
            .forceAddTag(BlockTags.CANDLES)
            .forceAddTag(BlockTags.BANNERS)
            .forceAddTag(BlockTags.SAPLINGS)
            .forceAddTag(BlockTags.CROPS)
            .forceAddTag(BlockTags.SMALL_FLOWERS)
            .forceAddTag(BlockTags.TALL_FLOWERS)
            .forceAddTag(BlockTags.CORALS)
            .forceAddTag(BlockTags.WALL_CORALS)
            .forceAddTag(HuskycordMod.DEAD_CORALS);

        getOrCreateTagBuilder(GRAVE_SINK_IF_OPEN)
            .forceAddTag(BlockTags.TRAPDOORS)
            .forceAddTag(BlockTags.FENCE_GATES);

        getOrCreateTagBuilder(GRAVE_FLOAT).add(
            Blocks.BUBBLE_COLUMN,
            Blocks.END_GATEWAY,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.LAVA,
            Blocks.LILY_PAD,
            Blocks.POWDER_SNOW,
            Blocks.SEAGRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.WATER
        );

        getOrCreateTagBuilder(GRAVE_FLOAT_WATERLOGGED).add(
            Blocks.AMETHYST_CLUSTER,
            Blocks.BAMBOO_SAPLING,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.GLOW_LICHEN,
            Blocks.HANGING_ROOTS,
            Blocks.LADDER,
            Blocks.LANTERN,
            Blocks.LARGE_AMETHYST_BUD,
            Blocks.LIGHT,
            Blocks.MEDIUM_AMETHYST_BUD,
            Blocks.SCULK_VEIN,
            Blocks.SEA_PICKLE,
            Blocks.SMALL_AMETHYST_BUD,
            Blocks.SMALL_DRIPLEAF,
            Blocks.SOUL_LANTERN
        )
            .forceAddTag(BlockTags.SIGNS)
            .forceAddTag(BlockTags.WALL_HANGING_SIGNS)
            .forceAddTag(BlockTags.DOORS)
            .forceAddTag(BlockTags.CANDLES)
            .forceAddTag(BlockTags.SAPLINGS)
            .forceAddTag(BlockTags.CORALS)
            .forceAddTag(BlockTags.WALL_CORALS)
            .forceAddTag(HuskycordMod.DEAD_CORALS);

        getOrCreateTagBuilder(GRAVE_FLOAT_FROM_WATERLOGGED)
            .add(Blocks.SCAFFOLDING)
            .forceAddTag(GRAVE_FLOAT_WATERLOGGED)
            .forceAddTag(BlockTags.RAILS);

        getOrCreateTagBuilder(GRAVE_FLOAT_IF_ABOVE_IS_FLOAT).add(
            Blocks.WATER_CAULDRON,
            Blocks.LAVA_CAULDRON,
            Blocks.POWDER_SNOW_CAULDRON
        );

        getOrCreateTagBuilder(GRAVE_FLOAT_FROM_WATERLOGGED_LOWER)
            .forceAddTag(BlockTags.SLABS)
            .forceAddTag(BlockTags.STAIRS);

        getOrCreateTagBuilder(GRAVE_FLOAT_WATERLOGGED_IF_OPEN)
            .forceAddTag(BlockTags.TRAPDOORS)
            .forceAddTag(BlockTags.FENCE_GATES);

        getOrCreateTagBuilder(GRAVE_SUPPORTS).add(
            Blocks.WATER_CAULDRON,
            Blocks.LAVA_CAULDRON,
            Blocks.POWDER_SNOW_CAULDRON,
            Blocks.LILY_PAD,
            Blocks.MOSS_CARPET,
            Blocks.SCAFFOLDING
        )
            .forceAddTag(BlockTags.CEILING_HANGING_SIGNS)
            .forceAddTag(BlockTags.SLABS)
            .forceAddTag(BlockTags.RAILS)
            .forceAddTag(BlockTags.BEDS)
            .forceAddTag(BlockTags.WOOL_CARPETS)
            .forceAddTag(BlockTags.STAIRS);

        getOrCreateTagBuilder(GRAVE_SUPPORTS_IF_CLOSED)
            .forceAddTag(BlockTags.TRAPDOORS)
            .forceAddTag(BlockTags.FENCE_GATES);

        getOrCreateTagBuilder(GRAVE_HEAD_PLACEABLE).add(
                Blocks.AMETHYST_CLUSTER,
                Blocks.BAMBOO_SAPLING,
                Blocks.BIG_DRIPLEAF,
                Blocks.BIG_DRIPLEAF_STEM,
                Blocks.BROWN_MUSHROOM,
                Blocks.BUBBLE_COLUMN,
                Blocks.CAVE_VINES,
                Blocks.CAVE_VINES_PLANT,
                Blocks.COBWEB,
                Blocks.COCOA,
                Blocks.CRIMSON_FUNGUS,
                Blocks.CRIMSON_ROOTS,
                Blocks.DEAD_BUSH,
                Blocks.FERN,
                Blocks.GLOW_LICHEN,
                Blocks.HANGING_ROOTS,
                Blocks.KELP,
                Blocks.KELP_PLANT,
                Blocks.LADDER,
                Blocks.LANTERN,
                Blocks.LARGE_AMETHYST_BUD,
                Blocks.LARGE_FERN,
                Blocks.LAVA,
                Blocks.LEVER,
                Blocks.LIGHT,
                Blocks.MEDIUM_AMETHYST_BUD,
                Blocks.NETHER_PORTAL,
                Blocks.NETHER_SPROUTS,
                Blocks.NETHER_WART,
                Blocks.PINK_PETALS,
                Blocks.PISTON_HEAD,
                Blocks.MOVING_PISTON,
                Blocks.POWDER_SNOW,
                Blocks.RED_MUSHROOM,
                Blocks.REDSTONE_TORCH,
                Blocks.REDSTONE_WALL_TORCH,
                Blocks.REDSTONE_WIRE,
                Blocks.SCAFFOLDING,
                Blocks.SCULK_VEIN,
                Blocks.SEA_PICKLE,
                Blocks.SEAGRASS,
                Blocks.SHORT_GRASS,
                Blocks.SMALL_AMETHYST_BUD,
                Blocks.SMALL_DRIPLEAF,
                Blocks.SNIFFER_EGG,
                Blocks.SOUL_LANTERN,
                Blocks.SOUL_TORCH,
                Blocks.SOUL_WALL_TORCH,
                Blocks.SPORE_BLOSSOM,
                Blocks.STRUCTURE_VOID,
                Blocks.SUGAR_CANE,
                Blocks.SWEET_BERRY_BUSH,
                Blocks.TALL_GRASS,
                Blocks.TALL_SEAGRASS,
                Blocks.TORCH,
                Blocks.WALL_TORCH,
                Blocks.TRIPWIRE,
                Blocks.TRIPWIRE_HOOK,
                Blocks.TWISTING_VINES,
                Blocks.TWISTING_VINES_PLANT,
                Blocks.VINE,
                Blocks.WARPED_FUNGUS,
                Blocks.WARPED_ROOTS,
                Blocks.WATER,
                Blocks.WEEPING_VINES,
                Blocks.WEEPING_VINES_PLANT
            )
            .forceAddTag(BlockTags.AIR)
            .forceAddTag(BlockTags.SIGNS)
            .forceAddTag(BlockTags.WALL_HANGING_SIGNS)
            .forceAddTag(BlockTags.CEILING_HANGING_SIGNS)
            .forceAddTag(BlockTags.TRAPDOORS)
            .forceAddTag(BlockTags.DOORS)
            .forceAddTag(BlockTags.BUTTONS)
            .forceAddTag(BlockTags.FENCE_GATES)
            .forceAddTag(BlockTags.FIRE)
            .forceAddTag(BlockTags.SLABS)
            .forceAddTag(BlockTags.RAILS)
            .forceAddTag(BlockTags.CANDLES)
            .forceAddTag(BlockTags.BANNERS)
            .forceAddTag(BlockTags.PRESSURE_PLATES)
            .forceAddTag(BlockTags.WOOL_CARPETS)
            .forceAddTag(BlockTags.SAPLINGS)
            .forceAddTag(BlockTags.CROPS)
            .forceAddTag(BlockTags.SMALL_FLOWERS)
            .forceAddTag(BlockTags.TALL_FLOWERS)
            .forceAddTag(BlockTags.CORALS)
            .forceAddTag(BlockTags.WALL_SIGNS)
            .forceAddTag(HuskycordMod.DEAD_CORALS);

        getOrCreateTagBuilder(GRAVE_REPLACEABLE).add(
            Blocks.BUBBLE_COLUMN,
            Blocks.LAVA,
            Blocks.POWDER_SNOW,
            Blocks.WATER
        )
            .forceAddTag(BlockTags.AIR)
            .forceAddTag(BlockTags.FIRE);

        getOrCreateTagBuilder(GRAVE_DANGEROUS).add(
            Blocks.LAVA_CAULDRON,
            Blocks.END_GATEWAY,
            Blocks.END_PORTAL,
            Blocks.LAVA,
            Blocks.NETHER_PORTAL,
            Blocks.POWDER_SNOW,
            Blocks.POWDER_SNOW_CAULDRON
        )
            .forceAddTag(BlockTags.FIRE)
            .forceAddTag(BlockTags.STAIRS);

        getOrCreateTagBuilder(GRAVE_SPECIAL)
            .forceAddTag(GRAVE_SINK)
            .forceAddTag(GRAVE_SINK_IF_OPEN)
            .forceAddTag(GRAVE_FLOAT)
            .forceAddTag(GRAVE_FLOAT_IF_ABOVE_IS_FLOAT)
            .forceAddTag(GRAVE_FLOAT_WATERLOGGED)
            .forceAddTag(GRAVE_FLOAT_WATERLOGGED_IF_OPEN)
            .forceAddTag(GRAVE_FLOAT_FROM_WATERLOGGED)
            .forceAddTag(GRAVE_FLOAT_FROM_WATERLOGGED_LOWER)
            .forceAddTag(GRAVE_HEAD_PLACEABLE)
            .forceAddTag(GRAVE_REPLACEABLE)
            .forceAddTag(GRAVE_SUPPORTS)
            .forceAddTag(GRAVE_SUPPORTS_IF_CLOSED)
            .forceAddTag(GRAVE_DANGEROUS);
    }
}
