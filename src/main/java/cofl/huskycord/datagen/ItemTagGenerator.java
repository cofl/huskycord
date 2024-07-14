package cofl.huskycord.datagen;

import cofl.huskycord.BannerPOI;
import cofl.huskycord.HuskycordMod;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ItemTagGenerator extends FabricTagProvider.ItemTagProvider {
    public ItemTagGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider wrapperLookup) {
        getOrCreateTagBuilder(HuskycordMod.CONCRETE_POWDER).add(
            Items.WHITE_CONCRETE_POWDER,
            Items.ORANGE_CONCRETE_POWDER,
            Items.MAGENTA_CONCRETE_POWDER,
            Items.LIGHT_BLUE_CONCRETE_POWDER,
            Items.YELLOW_CONCRETE_POWDER,
            Items.LIME_CONCRETE_POWDER,
            Items.PINK_CONCRETE_POWDER,
            Items.GRAY_CONCRETE_POWDER,
            Items.LIGHT_GRAY_CONCRETE_POWDER,
            Items.CYAN_CONCRETE_POWDER,
            Items.PURPLE_CONCRETE_POWDER,
            Items.BLUE_CONCRETE_POWDER,
            Items.BROWN_CONCRETE_POWDER,
            Items.GREEN_CONCRETE_POWDER,
            Items.RED_CONCRETE_POWDER,
            Items.BLACK_CONCRETE_POWDER
        );

        getOrCreateTagBuilder(HuskycordMod.RETAIN_IN_CRAFTING).add(
            Items.ELYTRA
        );

        getOrCreateTagBuilder(HuskycordMod.TICKET_ITEM).add(
            Items.PAPER,
            Items.AMETHYST_SHARD
        );
    }
}
