package cofl.huskycord.mixin;

import cofl.huskycord.HuskycordMod;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import static cofl.huskycord.HuskycordMod.LOGGER;

@Mixin(ShapedRecipe.class)
public abstract class RecipeMixin implements CraftingRecipe {
    @Override
    @SuppressWarnings("ConstantConditions")
    public @NotNull NonNullList<ItemStack> getRemainingItems(CraftingInput input){
        LOGGER.info("In method.");
        var list = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        var allowRetain = this instanceof CraftingRecipe;

        for(var i = 0; i < list.size(); i += 1){
            var stack = input.getItem(i);
            var item = stack.getItem();
            LOGGER.info("Checking remaining items for {}: allow? {}, keep? {}", stack, allowRetain, stack.is(HuskycordMod.RETAIN_IN_CRAFTING));

            if(item.hasCraftingRemainingItem()){
                list.set(i, new ItemStack(item.getCraftingRemainingItem()));
            } else if(stack.is(HuskycordMod.RETAIN_IN_CRAFTING)){
                list.set(i, stack.copy());
            }
        }

        return list;
    }
}
