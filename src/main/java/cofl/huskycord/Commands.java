package cofl.huskycord;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands.CommandSelection;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.time.LocalDateTime;
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
        dispatcher.register(literal("grave")
                .requires(CommandSourceStack::isPlayer)
                .executes(Commands::grave)
                .then(literal("all").executes(Commands::allGraves)));
    }

    private static int forfeit(CommandContext<CommandSourceStack> context) {
        var player = Objects.requireNonNull(context.getSource().getPlayer());
        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
        return 1;
    }

    @SuppressWarnings("SameReturnValue")
    private static int grave(CommandContext<CommandSourceStack> context){
        var player = Objects.requireNonNull(context.getSource().getPlayer());
        var server = Objects.requireNonNull(player.getServer());
        var level = Objects.requireNonNull(player.serverLevel());
        var graves = PlayerGraveData.getServerState(server).getGraves(player);
        if (graves.isEmpty()){
            player.sendSystemMessage(Component.literal("You have no graves."));
            return 0;
        }

        var grave = graves.getFirst();
        var message = Component.literal("Your last grave")
            .append(graveCount(graves.size()))
            .append(" is at ")
            .append(ChatUtil.asComponent(grave.position().relative(Direction.UP, 1)))
            .append(graveDimension(level.dimension(), grave.dimension()))
            .append(" ")
            .append(ChatUtil.age(grave.dateTime(), LocalDateTime.now())
                .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        player.sendSystemMessage(message);
        return 0;
    }

    private static MutableComponent graveCount(int count){
        if (count <= 1)
            return Component.empty();
        return Component.literal(" (%d more)".formatted(count));
    }

    private static MutableComponent graveDimension(ResourceKey<Level> current, ResourceKey<Level> grave){
        if (current.location().equals(grave.location()))
            return Component.empty();
        return Component.literal(" in ").append(dimensionName(grave));
    }

    private static int allGraves(CommandContext<CommandSourceStack> context){
        var player = Objects.requireNonNull(context.getSource().getPlayer());
        var server = Objects.requireNonNull(player.getServer());
        var level = Objects.requireNonNull(player.serverLevel());
        var graves = PlayerGraveData.getServerState(server).getGraves(player);

        var message = switch (graves.size()){
            case 0 -> Component.literal("You have no graves.");
            case 1 -> Component.literal("You have 1 grave:");
            default -> Component.literal("You have %d graves:".formatted(graves.size()));
        };

        var now = LocalDateTime.now();
        for (var grave: graves){
            message = message.append(Component.literal("\n    ")
                .append(ChatUtil.asComponent(grave.position().relative(Direction.UP, 1)))
                .append(" in ").append(dimensionName(grave.dimension())))
                .append(" ")
                .append(ChatUtil.age(grave.dateTime(), now)
                    .setStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }

        player.sendSystemMessage(message);
        return 0;
    }

    private static String dimensionName(ResourceKey<Level> dimension){
        if (dimension == Level.OVERWORLD)
            return "the Overworld";
        if (dimension == Level.NETHER)
            return "the Nether";
        if (dimension == Level.END)
            return "The End";
        return dimension.location().toString();
    }
}
