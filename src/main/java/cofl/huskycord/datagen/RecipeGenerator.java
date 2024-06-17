package cofl.huskycord.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

import static cofl.huskycord.HuskycordMod.MOD_NAME;

public class RecipeGenerator extends FabricRecipeProvider {
    public RecipeGenerator(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    public void buildRecipes(RecipeOutput exporter) {
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, Items.ELYTRA)
            .pattern("PRP")
            .pattern("PKP")
            .pattern("B B")
            .define('P', Items.PHANTOM_MEMBRANE)
            .define('R', Items.END_ROD)
            .define('K', Items.ELYTRA)
            .define('B', Items.BREEZE_ROD)
            .unlockedBy(getItemName(Items.ELYTRA), FabricRecipeProvider.has(Items.ELYTRA))
            .save(exporter, ResourceLocation.fromNamespaceAndPath(MOD_NAME, getItemName(Items.ELYTRA)));
    }
}
