package cofl.huskycord;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;

import java.util.Objects;

import static net.minecraft.commands.Commands.*;

public class Commands {
    public static void register(){
        CommandRegistrationCallback.EVENT.register(Commands::register);
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registry, CommandSelection environment) {
        dispatcher.register(literal("gg").executes(Commands::forfeit));
        dispatcher.register(literal("qq").executes(Commands::forfeit));
        dispatcher.register(literal("ff").executes(Commands::forfeit));
        dispatcher.register(literal("forfeit").executes(Commands::forfeit));
    }

    private static int forfeit(CommandContext<CommandSourceStack> context) {
        var player = Objects.requireNonNull(context.getSource().getPlayer());
        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
        return 1;
    }
}
