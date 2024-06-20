package cofl.huskycord.mixin;

import cofl.huskycord.HuskycordMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Shadow public abstract void setSilent(boolean bl);
    @Shadow public abstract boolean isSilent();

    @Inject(at = @At("HEAD"), method = "setCustomName")
    private void setCustomName(Component component, CallbackInfo ci){
        var self = (Entity)(Object)this;

        //noinspection UnreachableCode
        if(self instanceof Mob && component != null){
            var name = component.getString();
            if(name.equalsIgnoreCase("silence me")
                    || name.equalsIgnoreCase("silence_me")){
                setSilent(true);
                HuskycordMod.LOGGER.debug("Silenced entity.");
            } else if(isSilent()){
                setSilent(false);
                HuskycordMod.LOGGER.debug("Unsilenced entity.");
            }
        }
    }
}
