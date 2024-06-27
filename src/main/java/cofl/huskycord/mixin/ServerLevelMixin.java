package cofl.huskycord.mixin;

import cofl.huskycord.MarkerSetData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin extends Level {
    protected ServerLevelMixin(WritableLevelData p1, ResourceKey<Level> p2, RegistryAccess p3, Holder<DimensionType> p4,
                               Supplier<ProfilerFiller> p5, boolean bl, boolean bl2, long l, int i) {
        super(p1, p2, p3, p4, p5, bl, bl2, l, i);
    }

    @Inject(method = "onBlockStateChange", at = @At("HEAD"))
    void onBlockStateChange(BlockPos pos, BlockState before, BlockState after, CallbackInfo ci){
        if(!before.is(BlockTags.BANNERS) || after.is(before.getBlock()))
            return;
        MarkerSetData.get((ServerLevel)(Object)this).remove(pos);
    }
}
