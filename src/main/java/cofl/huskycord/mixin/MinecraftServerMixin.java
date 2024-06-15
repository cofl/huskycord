package cofl.huskycord.mixin;

import cofl.huskycord.IAutosaveManager;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.server.MinecraftServer;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin implements IAutosaveManager {
    @Unique
    private boolean shouldSave = true;

    @Override
    public void huskycord$setShouldSave(boolean state){
        shouldSave = state;
    }

    @WrapWithCondition(
        method = "tickServer",
        at = @At(
            value = "FIELD",
            target = "Lnet/minecraft/server/MinecraftServer;ticksUntilAutosave:I",
            ordinal = 0,
            opcode = Opcodes.PUTFIELD
        )
    )
    public boolean getShouldSave(MinecraftServer server, int ticksUntilAutosave){
        return shouldSave;
    }
}
