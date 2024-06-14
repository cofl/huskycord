package cofl.huskycord.mixin;

import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static cofl.huskycord.HuskycordMod.DISPENSER_PLACEABLE;
import static cofl.huskycord.HuskycordMod.LOGGER;

@Mixin(DefaultDispenseItemBehavior.class)
public class DefaultDispenseItemBehaviorMixin {
    @Inject(at = @At("HEAD"), method = "execute", cancellable = true)
    protected void execute(BlockSource blockSource, ItemStack itemStack, CallbackInfoReturnable<ItemStack> ci){
        var block = Block.byItem(itemStack.getItem()).defaultBlockState();
        if(block.is(DISPENSER_PLACEABLE)){
            var level = blockSource.level();
            var blockPos = blockSource.pos()
                    .relative(blockSource.state().getValue(DispenserBlock.FACING));
            LOGGER.debug("Attempting to dispense block: {}", block);
            if(level.isEmptyBlock(blockPos)){
                if(!level.isClientSide){
                    LOGGER.debug(">>> Placing block {}", itemStack);
                    level.setBlock(blockPos, block, 3);
                    level.gameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
                }
                itemStack.shrink(1);
            }
            ci.setReturnValue(itemStack);
        }
    }
}
