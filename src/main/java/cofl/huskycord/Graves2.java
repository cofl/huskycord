package cofl.huskycord;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

import static cofl.huskycord.Graves.*;
import static cofl.huskycord.HuskycordMod.LOGGER;

public class Graves2 {
    private static final String GRAVE_ENTITY_TAG = "huskycordGrave";

    public static void onDeath(ServerPlayer player){
        var level = player.level();
        if(level.isClientSide())
            return;

        var server = player.level().getServer();
        if(null == server)
            return;

        if(player.getInventory().isEmpty())
            return;

        var totalExperience = getExperience(player);
        if(totalExperience <= 0)
            return;

        var rules = server.getGameRules();
        var grave = createGraveItem(player, rules, totalExperience);
        var entity = createGraveEntity(player, level, grave);
        var position = getGravePosition(level, player.blockPosition(), rules);
        entity.setPos(position);
        level.addFreshEntity(entity);
        LOGGER.info("Spawned grave for {} at {} in {}", player.getName().getString(), position, level.dimension());

        if (rules.getBoolean(TELL_GRAVE_POSITION)
            || !player.position().closerThan(position, rules.getInt(TELL_GRAVE_DISTANCE)))
            player.sendSystemMessage(
                Component.literal("Your grave is at ")
                    .append(Component.literal(String.format("[%d, %d, %d]", (int)position.x, (int)position.y + 1, (int)position.z))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))));
    }

    // player.totalExperience is inaccurate when adding XP with the /xp (levels) command.
    private static int getExperience(ServerPlayer player){
        var levels = player.experienceLevel;
        var nextLevel = player.getXpNeededForNextLevel();
        var partial = nextLevel * player.experienceProgress;
        if(levels <= 16)
            return Math.round(levels * levels + 6.0f * levels + partial);
        if(levels <= 31)
            return Math.round(2.5f * levels * levels - 40.5f * levels + 360f + partial);
        return Math.round(4.5f * levels * levels - 162.5f * levels + 2220f + partial);
    }

    private static ItemStack createGraveItem(ServerPlayer player, GameRules rules, int totalExperience){
        var stack = new ItemStack(Items.STONE_BRICK_WALL);
        stack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));

        var inventory = player.getInventory();
        var list = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for(var i = 0; i < list.size(); i += 1){
            inventory.setItem(i, processItem(inventory.getItem(i)));
        }
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));

        var lossRate = (double)rules.getInt(DEATH_EXPERIENCE_LOSS) / 100d;
        stack.set(DataComponents.REPAIR_COST, Math.max(0, totalExperience - (int)Math.ceil(totalExperience * lossRate)));
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);

        return stack;
    }

    private static ItemStack processItem(ItemStack source){
        if(hasVanishingCurse(source)){
            source.setCount(0);
            return ItemStack.EMPTY;
        }

        return removeBindingCurse(source.copyAndClear());
    }

    private static boolean hasVanishingCurse(ItemStack stack){
        var component = stack.getComponents().get(DataComponents.ENCHANTMENTS);
        if(null == component || component.isEmpty())
            return false;
        for(var holder: component.keySet()){
            if(holder.is(Enchantments.VANISHING_CURSE))
                return true;
        }
        return false;
    }

    private static ItemStack removeBindingCurse(ItemStack stack){
        var component = stack.getComponents().get(DataComponents.ENCHANTMENTS);
        if(null == component || component.isEmpty())
            return stack;

        var mutable = new ItemEnchantments.Mutable(component);
        mutable.removeIf(h -> h.is(Enchantments.BINDING_CURSE));
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return stack;
    }

    private static Entity createGraveEntity(ServerPlayer player, Level level, ItemStack graveItem){
        var entity = new ArmorStand(EntityType.ARMOR_STAND, level);
        entity.setInvisible(true);
        entity.setNoGravity(true);
        entity.setSilent(true);
        entity.setInvulnerable(true);
        entity.setRightArmPose(new Rotations(0, 90, 0));
        entity.setLeftArmPose(new Rotations(0, 90, 0));
        entity.setHeadPose(new Rotations(180, 0, 0));

        // disable helmet slot: https://minecraft.wiki/w/Armor_Stand/ED
        entity.disabledSlots = 16;
        entity.setItemSlot(EquipmentSlot.HEAD, graveItem);

        var gray = Style.EMPTY.withColor(ChatFormatting.GRAY);
        entity.setCustomName(Component.literal("[").setStyle(gray)
            .append(player.getName().plainCopy().setStyle(Style.EMPTY
                .withColor(ChatFormatting.WHITE)
                .withItalic(true)))
            .append(Component.literal("]").setStyle(gray)));

        entity.setXRot(0);
        entity.addTag(GRAVE_ENTITY_TAG);

        return entity;
    }

    private static Vec3 getGravePosition(Level level, BlockPos startPosition, GameRules rules){
        var lowestPosition = sinkPosition(level, startPosition);
        var highestPosition = floatPosition(level, lowestPosition, rules);
        var finalPosition = highestPosition;
        // TODO: adjust position for any blocks in the way
        return new Vec3(finalPosition.getX(), finalPosition.getY(), finalPosition.getZ());
    }

    private static BlockPos sinkPosition(Level level, BlockPos initial){
        var position = initial.mutable();
        var highestFloatingPosition = (BlockPos)null;
        while (position.getY() >= level.getMinBuildHeight() && canSink(level, position)){
            if (highestFloatingPosition == null && canFloat(level, position))
                highestFloatingPosition = position.immutable();
            position.move(Direction.DOWN, 1);
        }

        // didn't fall out of the world.
        if (position.getY() >= level.getMinBuildHeight())
            return Objects.requireNonNullElse(highestFloatingPosition, position);

        // fell out of the world, but encountered a block we can float in on the way.
        if (highestFloatingPosition != null)
            return highestFloatingPosition;

        // all just sinkable, wrap to the top and try again
        highestFloatingPosition = null;
        position.setY(level.getMaxBuildHeight());
        while (position.getY() > initial.getY() && canSink(level, position)){
            if (highestFloatingPosition == null && canFloat(level, position))
                highestFloatingPosition = position.immutable();
            position.move(Direction.DOWN, 1);
        }

        // found a block to stop at
        if (position.getY() > initial.getY())
            return Objects.requireNonNullElse(highestFloatingPosition, position);

        // didn't, but found something to float in
        if (highestFloatingPosition != null)
            return highestFloatingPosition;

        // column is entirely just sinkable.
        return position.atY(level.getMinBuildHeight());
    }

    private static boolean canSink(Level level, BlockPos position){
        // TODO
        return true;
    }

    private static BlockPos floatPosition(Level level, BlockPos initial, GameRules rules){
        // floating deals with the "head height" of the grave
        var position = initial.above().mutable();

        // float up while our head is in a float-able block
        while (position.getY() <= level.getMaxBuildHeight() + 1 && canFloat(level, position))
            position.move(Direction.UP, 1);

        // we went all the way to the top of the world
        if (position.getY() > level.getMaxBuildHeight())
            return position.atY(level.getMaxBuildHeight());

        // canPlace deals in "foot" positions.
        if (canPlace(level, position.below()))
            return position.below().immutable();

        // if we can't place and can't try to escape, just go back to the bottom of this column.
        if (!shouldTryEscapeDanger(level, position.below()))
            return initial;

        // try to look for a safe spot above danger.
        var searchHeight = rules.getInt(GRAVE_DANGEROUS_CEILING_HEIGHT);
        for (var i = 1; i <= searchHeight; i += 1){
            var testPosition = position.above(i);
            if (testPosition.getY() > level.getMaxBuildHeight())
                break;
            if (canPlace(level, testPosition.below()) && !isDangerous(level, testPosition))
                return testPosition.below().immutable();
        }

        // couldn't escape danger, just go back to the bottom of this column.
        return initial;
    }

    private static boolean canFloat(Level level, BlockPos position){
        // TODO
        return true;
    }

    private static boolean canPlace(Level level, BlockPos position){
        // TODO
        return true;
    }

    private static boolean shouldTryEscapeDanger(Level level, BlockPos position){
        // TODO
        return true;
    }

    private static boolean isDangerous(Level level, BlockPos position){
        // TODO
        return true;
    }
}
