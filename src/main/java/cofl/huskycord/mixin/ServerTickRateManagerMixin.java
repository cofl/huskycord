package cofl.huskycord.mixin;

import cofl.huskycord.ITickRateManager;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.world.TickRateManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ServerTickRateManager.class)
public abstract class ServerTickRateManagerMixin extends TickRateManager implements ITickRateManager {
    @Shadow
    public abstract void setFrozen(boolean bl);

    @Override
    public void huskycord$setFrozen(boolean state, boolean broadcast) {
        if(broadcast)
            this.setFrozen(state);
        else
            super.setFrozen(state);
    }
}
