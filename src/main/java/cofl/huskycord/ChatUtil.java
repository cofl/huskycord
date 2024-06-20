package cofl.huskycord;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.world.phys.Vec3;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ChatUtil {
    public static Component asComponent(Vec3 position){
        var x = (int)Math.round(position.x() - 0.5);
        var y = (int)Math.round(position.y());
        var z = (int)Math.round(position.z() - 0.5);
        return ComponentUtils.wrapInSquareBrackets(
            Component.translatable("chat.coordinates", x, y, z))
                .withStyle((style) -> style
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
                        "/tp @s " + x + " " + y + " " + z
                    ))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
    }

    private static String plural(long i){
        if (i == 1L)
            return "";
        return "s";
    }

    public static MutableComponent age(LocalDateTime from, LocalDateTime now){
        var hours = ChronoUnit.HOURS.between(from, now);
        if (hours <= 6)
            return Component.empty();

        var days = ChronoUnit.DAYS.between(from, now);
        if (days < 1)
            return wrapInParens(Component.literal("%d hours old".formatted(hours)));
        else if (days <= 3 && hours % 24 > 0)
            return wrapInParens(Component.literal("%d day%s, %d hour%s old".formatted(days, plural(days), hours % 24, plural(hours % 24))));
        else if (days < 7)
            return wrapInParens(Component.literal("%d day%s old".formatted(days, plural(days))));

        var weeks = ChronoUnit.WEEKS.between(from, now);
        if (weeks < 3 && days % 7 > 0)
            return wrapInParens(Component.literal("%d week%s, %d day%s old".formatted(weeks, plural(weeks), days % 7, plural(days % 7))));
        else if (weeks < 8)
            return wrapInParens(Component.literal("%d week%s old".formatted(weeks, plural(weeks))));

        var months = ChronoUnit.MONTHS.between(from, now);
        return wrapInParens(Component.literal("%d months old".formatted(months)));
    }

    public static MutableComponent wrapInParens(Component component){
        return Component.literal("(").append(component).append(")");
    }
}
