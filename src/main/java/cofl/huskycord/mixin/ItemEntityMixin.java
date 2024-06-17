package cofl.huskycord.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ConcretePowderBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static cofl.huskycord.HuskycordMod.CONCRETE_POWDER;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements TraceableEntity {
    public ItemEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    public abstract ItemStack getItem();

    @Shadow
    public abstract void setItem(ItemStack itemStack);

    @Inject(at = @At("TAIL"), method = "tick")
    private void tick(CallbackInfo info){
        if(this.level().isClientSide())
            return;

        var item = this.getItem();
        if (item.is(CONCRETE_POWDER)
            && item.getItem() instanceof BlockItem blockItem
            && blockItem.getBlock() instanceof ConcretePowderBlock concretePowderBlock
            && this.getInBlockState().is(Blocks.WATER_CAULDRON)
        ){
            var concrete = concretePowderBlock.concrete;
            this.setItem(new ItemStack(concrete.asItem(), item.getCount()));
        }
    }
}
