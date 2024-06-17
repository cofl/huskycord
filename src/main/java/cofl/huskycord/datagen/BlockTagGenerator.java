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
            Blocks.CAKE,
            Blocks.MOSS_CARPET,
            Blocks.FARMLAND,
            Blocks.DIRT_PATH,
            Blocks.ENCHANTING_TABLE,
            Blocks.CONDUIT,
            Blocks.CAULDRON,
            Blocks.WATER_CAULDRON,
            Blocks.COMPOSTER,
            Blocks.CREEPER_HEAD,
            Blocks.CREEPER_WALL_HEAD,
            Blocks.DRAGON_HEAD,
            Blocks.DRAGON_WALL_HEAD,
            Blocks.PIGLIN_HEAD,
            Blocks.PIGLIN_WALL_HEAD,
            Blocks.PLAYER_HEAD,
            Blocks.PLAYER_WALL_HEAD,
            Blocks.ZOMBIE_HEAD,
            Blocks.ZOMBIE_WALL_HEAD,
            Blocks.SKELETON_SKULL,
            Blocks.SKELETON_WALL_SKULL,
            Blocks.WITHER_SKELETON_SKULL,
            Blocks.WITHER_SKELETON_WALL_SKULL,
            Blocks.COMPARATOR,
            Blocks.REPEATER,
            Blocks.END_PORTAL_FRAME,
            Blocks.DAYLIGHT_DETECTOR,
            Blocks.SCULK_SENSOR,
            Blocks.SCULK_SHRIEKER,
            Blocks.CALIBRATED_SCULK_SENSOR,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.HEAVY_CORE
        )
            .forceAddTag(BlockTags.CAMPFIRES)
            .forceAddTag(BlockTags.BEDS)
            .forceAddTag(BlockTags.ANVIL)
            .forceAddTag(BlockTags.SLABS)
            .forceAddTag(BlockTags.CANDLE_CAKES)
            .forceAddTag(BlockTags.WOOL_CARPETS);

        getOrCreateTagBuilder(Graves.GRAVE_SUPPORTS_IF_CLOSED)
            .forceAddTag(BlockTags.TRAPDOORS);

        getOrCreateTagBuilder(Graves.GRAVE_PLACEABLE).add(
            Blocks.WATER,
            Blocks.BUBBLE_COLUMN,
            Blocks.LAVA,
            Blocks.POWDER_SNOW
        )   .forceAddTag(BlockTags.AIR)
            .forceAddTag(Graves.GRAVE_SUPPORTS)
            .forceAddTag(Graves.GRAVE_SUPPORTS_IF_CLOSED);

        getOrCreateTagBuilder(Graves.GRAVE_FLOAT).add(
            Blocks.WATER,
            Blocks.BUBBLE_COLUMN
        )   .forceAddTag(Graves.GRAVE_DANGEROUS_FLOAT);

        getOrCreateTagBuilder(Graves.GRAVE_DANGEROUS_FLOAT).add(
            Blocks.LAVA
        );

        getOrCreateTagBuilder(Graves.GRAVE_FLOAT_WATERLOGGED).add(
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
            Blocks.DEAD_TUBE_CORAL_WALL_FAN,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.AMETHYST_CLUSTER,
            Blocks.BUDDING_AMETHYST,
            Blocks.LARGE_AMETHYST_BUD,
            Blocks.MEDIUM_AMETHYST_BUD,
            Blocks.SMALL_AMETHYST_BUD,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.GLOW_LICHEN,
            Blocks.HANGING_ROOTS,
            Blocks.LANTERN,
            Blocks.SOUL_LANTERN,
            Blocks.SCULK_SENSOR,
            Blocks.SCULK_SHRIEKER,
            Blocks.SCULK_VEIN,
            Blocks.SEAGRASS,
            Blocks.CHEST,
            Blocks.TRAPPED_CHEST,
            Blocks.ENDER_CHEST,
            Blocks.HEAVY_CORE
        )
            .forceAddTag(BlockTags.ALL_SIGNS)
            .forceAddTag(BlockTags.RAILS)
            .forceAddTag(BlockTags.CLIMBABLE)
            .forceAddTag(BlockTags.ALL_HANGING_SIGNS)
            .forceAddTag(BlockTags.CANDLES)
            .forceAddTag(BlockTags.CORAL_PLANTS)
            .forceAddTag(BlockTags.WALL_CORALS)
            .forceAddTag(BlockTags.SAPLINGS);

        getOrCreateTagBuilder(Graves.GRAVE_FLOAT_WATERLOGGED_IF_OPEN)
            .forceAddTag(BlockTags.TRAPDOORS);

        getOrCreateTagBuilder(Graves.GRAVE_SINK).add(
            Blocks.BUBBLE_COLUMN,
            Blocks.AMETHYST_CLUSTER,
            Blocks.BELL,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.BROWN_MUSHROOM,
            Blocks.BUDDING_AMETHYST,
            Blocks.CAKE,
            Blocks.COBWEB,
            Blocks.COCOA,
            Blocks.CRIMSON_FUNGUS,
            Blocks.CRIMSON_ROOTS,
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
            Blocks.DEAD_TUBE_CORAL_WALL_FAN,
            Blocks.HANGING_ROOTS,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.LANTERN,
            Blocks.LARGE_AMETHYST_BUD,
            Blocks.LAVA,
            Blocks.MEDIUM_AMETHYST_BUD,
            Blocks.NETHER_SPROUTS,
            Blocks.NETHER_WART,
            Blocks.POWDER_SNOW,
            Blocks.REDSTONE_TORCH,
            Blocks.REDSTONE_WALL_TORCH,
            Blocks.REDSTONE_WIRE,
            Blocks.RED_MUSHROOM,
            Blocks.SCULK_VEIN,
            Blocks.SEAGRASS,
            Blocks.SHORT_GRASS,
            Blocks.SMALL_AMETHYST_BUD,
            Blocks.SMALL_DRIPLEAF,
            Blocks.SNIFFER_EGG,
            Blocks.SNOW,
            Blocks.SOUL_LANTERN,
            Blocks.SOUL_TORCH,
            Blocks.SOUL_WALL_TORCH,
            Blocks.SPORE_BLOSSOM,
            Blocks.SUGAR_CANE,
            Blocks.TALL_GRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.TORCH,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.TURTLE_EGG,
            Blocks.WALL_TORCH,
            Blocks.WARPED_FUNGUS,
            Blocks.WARPED_ROOTS,
            Blocks.WATER
        )
            .forceAddTag(BlockTags.CROPS)
            .forceAddTag(BlockTags.AIR)
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
            .forceAddTag(BlockTags.CORALS)
            .forceAddTag(BlockTags.WALL_CORALS)
            .forceAddTag(BlockTags.PORTALS);
    }
}
