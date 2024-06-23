package cofl.huskycord;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ChatUtil {
    public static Component asComponent(Vec3 position, ResourceKey<Level> dimension){
        var x = (int)Math.round(position.x() - 0.5);
        var y = (int)Math.round(position.y());
        var z = (int)Math.round(position.z() - 0.5);
        return ComponentUtils.wrapInSquareBrackets(
            Component.translatable("chat.coordinates", x, y, z))
                .withStyle((style) -> style
                    .withColor(ChatFormatting.GREEN)
                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, MessageFormat.format(
                        "/execute in {0} run tp @s {1,number,0} {2,number,0} {3,number,0}",
                        dimension.location(), x, y, z)))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
    }

    private static String plural(long i){
        if (i == 1L)
            return "";
        return "s";
    }

    private static final String PLURAL = ",choice,0#s|1#|2#s";
    private static final MessageFormat DAYS_HOURS = new MessageFormat("{0} day{0"+PLURAL+"}, {1} hour{1"+PLURAL+"} old");
    private static final MessageFormat DAYS = new MessageFormat("{0} day{0"+PLURAL+"} old");
    private static final MessageFormat WEEKS_DAYS = new MessageFormat("{0} week{0"+PLURAL+"}, {1} day{1"+PLURAL+"} old");
    private static final MessageFormat WEEKS = new MessageFormat("{0} week{0"+PLURAL+"} old");
    public static MutableComponent age(LocalDateTime from, LocalDateTime now){
        var hours = ChronoUnit.HOURS.between(from, now);
        if (hours <= 6)
            return Component.empty();

        var days = ChronoUnit.DAYS.between(from, now);
        if (days < 1)
            return wrapInParens(fmt("{0} hours old", hours));
        else if (days <= 3 && hours % 24 > 0)
            return wrapInParens(fmt(DAYS_HOURS, days, hours % 24));
        else if (days < 7)
            return wrapInParens(fmt(DAYS, days));

        var weeks = ChronoUnit.WEEKS.between(from, now);
        if (weeks < 3 && days % 7 > 0)
            return wrapInParens(fmt(WEEKS_DAYS, weeks, days % 7));
        else if (weeks < 8)
            return wrapInParens(fmt(WEEKS, weeks));

        var months = ChronoUnit.MONTHS.between(from, now);
        return wrapInParens(fmt("{0} months old", months));
    }

    public static MutableComponent wrapInParens(Component component){
        return Component.literal("(").append(component).append(")");
    }

    public static MutableComponent fmt(final String pattern, final Object... args){
        return Component.literal(MessageFormat.format(pattern, args));
    }

    public static MutableComponent fmt(final MessageFormat format, final Object... args){
        return Component.literal(format.format(args));
    }
}
