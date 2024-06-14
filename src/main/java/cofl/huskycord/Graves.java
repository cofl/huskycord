package cofl.huskycord;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.fabricmc.fabric.api.item.v1.EnchantingContext;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.*;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.Category;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import static cofl.huskycord.HuskycordMod.LOGGER;

public class Graves {
    public static final GameRules.Key<GameRules.IntegerValue> DEATH_EXPERIENCE_LOSS =
        GameRuleRegistry.register("deathExperienceLoss", Category.PLAYER, GameRuleFactory.createIntRule(0, 0, 100));
    public static final GameRules.Key<GameRules.BooleanValue> CAN_OP_OPEN_GRAVES =
        GameRuleRegistry.register("canOpOpenGraves", Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> TELL_GRAVE_POSITION =
        GameRuleRegistry.register("tellGravePosition", Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.IntegerValue> TELL_GRAVE_DISTANCE =
        GameRuleRegistry.register("tellGraveDistance", Category.PLAYER, GameRuleFactory.createIntRule(4, 0, 16));

    private static final String GRAVE_ENTITY_TAG = "huskycordGrave";
    public static void register(){
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if(entity instanceof ServerPlayer player){
                onDeath(player, damageSource, damageAmount);
            }
            return true;
        });

        UseEntityCallback.EVENT.register(Graves::onEntityInteract);
    }

    // player.totalExperience is inaccurate when adding XP with the /xp (levels) command.
    private static int getTotalExperience(Player player){
        var levels = player.experienceLevel;
        var nextLevel = player.getXpNeededForNextLevel();
        var partial = nextLevel * player.experienceProgress;
        if(levels <= 16)
            return Math.round(levels * levels + 6.0f * levels + partial);
        if(levels <= 31)
            return Math.round(2.5f * levels * levels - 40.5f * levels + 360f + partial);
        return Math.round(4.5f * levels * levels - 162.5f * levels + 2220f + partial);
    }

    private static BlockState getFillerBlock(ResourceKey<Level> level){
        if(level == Level.END)
            return Blocks.END_STONE.defaultBlockState();
        if(level == Level.NETHER)
            return Blocks.NETHERRACK.defaultBlockState();
        return Blocks.STONE.defaultBlockState();
    }

    private static boolean hasEnchantment(ItemStack stack, ResourceKey<Enchantment> enchantment){
        var component = stack.getComponents().get(DataComponents.ENCHANTMENTS);
        if(null == component || component.isEmpty())
            return false;
        for(var holder: component.keySet()){
            if(holder.is(enchantment))
                return true;
        }
        return false;
    }

    private static ItemStack removeEnchantment(ItemStack stack, ResourceKey<Enchantment> enchantment){
        var component = stack.getComponents().get(DataComponents.ENCHANTMENTS);
        if(null == component || component.isEmpty())
            return stack;

        var mutable = new ItemEnchantments.Mutable(component);
        mutable.removeIf(h -> h.is(enchantment));
        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return stack;
    }

    public static void onDeath(ServerPlayer player, DamageSource source, float damageAmount){
        var level = player.level();
        if(level.isClientSide())
            return;
        if(player.getInventory().isEmpty() && player.totalExperience == 0)
            return;

        var server = player.level().getServer();
        if(null == server)
            return;

        var gameRules = server.getGameRules();
        var deathExperienceLoss = (double)gameRules.getInt(DEATH_EXPERIENCE_LOSS) / 100d;

        // create a replica of the player inventory and experience
        var grave = new ItemStack(Items.STONE_BRICK_WALL);
        grave.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));

        var inventory = player.getInventory();
        var inventoryList = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for(var i = 0; i < inventoryList.size(); i += 1){
            var stack = inventory.getItem(i);
            if(hasEnchantment(stack, Enchantments.VANISHING_CURSE)) {
                inventoryList.set(i, ItemStack.EMPTY);
                stack.setCount(0);
            } else {
                inventoryList.set(i, removeEnchantment(stack.copyAndClear(), Enchantments.BINDING_CURSE));
            }
        }
        grave.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inventoryList));

        int playerXp = getTotalExperience(player);
        int totalExperience = Math.max(0, playerXp - (int)Math.ceil(playerXp * deathExperienceLoss));
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        grave.set(DataComponents.REPAIR_COST, totalExperience);

        // set up our armor stand
        var armorStand = new ArmorStand(EntityType.ARMOR_STAND, level);
        armorStand.setInvisible(true);
        armorStand.setNoGravity(true);
        armorStand.setInvulnerable(true);
        armorStand.setSilent(true);
        armorStand.setRightArmPose(new Rotations(0, 90, 0));
        armorStand.setLeftArmPose(new Rotations(0, 90, 0));
        armorStand.setHeadPose(new Rotations(180, 0, 0));

        // disable helmet slot: https://minecraft.wiki/w/Armor_Stand/ED
        armorStand.disabledSlots = 16;
        armorStand.setItemSlot(EquipmentSlot.HEAD, grave);

        var gray = Style.EMPTY.withColor(ChatFormatting.GRAY);
        armorStand.setCustomName(Component.literal("[").setStyle(gray)
            .append(player.getName().plainCopy().setStyle(Style.EMPTY
                .withItalic(true)
                .withColor(ChatFormatting.GRAY)))
            .append(Component.literal("]").setStyle(gray)));
        armorStand.setCustomNameVisible(true);

        // calculate spawn position for grave
        // spawn a fake block if necessary (for void deaths/lava lakes)
        var position = player.blockPosition().mutable();
        var placeBlock = false;
        while (position.getY() >= level.getMinBuildHeight() && !level.getBlockState(position).isSolid())
            position.move(Direction.DOWN, 1);

        if(position.getY() < level.getMinBuildHeight()){
            // went into the void, go back to the top and start over.
            position.setY(level.getMaxBuildHeight());
            while(position.getY() >= level.getMinBuildHeight() && !level.getBlockState(position).isSolid())
                position.move(Direction.DOWN, 1);
        }

        if(position.getY() < level.getMinBuildHeight()){
            // Went out the top. We must be in an empty column.
            placeBlock = true;
            position.setY(level.getMinBuildHeight());
        } else if(level.getBlockState(position.above(1)).is(Blocks.LAVA)
            && level.getBlockState(position.above(2)).is(Blocks.LAVA)
            && level.getBlockState(position.above(3)).is(Blocks.LAVA)){
            // check for lava lakes
            var testPosition = position.above(3).mutable();
            while(testPosition.getY() <= level.getMaxBuildHeight() && level.getBlockState(testPosition).is(Blocks.LAVA)){
                testPosition.move(Direction.UP, 1);
            }

            // maybe with a small roof
            var top = level.getBlockState(testPosition);
            if(top.isAir()){
                placeBlock = true;
                position.setY(Math.min(testPosition.getY(), level.getMaxBuildHeight()));
            } else if(top.isSolid()){
                for(var i = 1; i <= 3; i += 1) {
                    if (level.getBlockState(testPosition.above(i)).isAir() && level.getBlockState(testPosition.above(i - 1)).isSolid()) {
                        position.setY(testPosition.above(i).getY());
                        break;
                    }
                }
            }
        }

        if(placeBlock){
            level.setBlock(position, getFillerBlock(level.dimension()), 2);
        }

        armorStand.setXRot(0);
        armorStand.setPos(position.getX() + 0.5f, position.getY() + 0.15f, position.getZ() + 0.5f);
        armorStand.addTag(GRAVE_ENTITY_TAG);
        level.addFreshEntity(armorStand);
        LOGGER.info("Spawned grave for {} at {} in {}", player.getName().getString(), armorStand.position(), level.dimension());

        if(gameRules.getBoolean(TELL_GRAVE_POSITION) || !player.position().closerThan(armorStand.position(), gameRules.getInt(TELL_GRAVE_DISTANCE))){
            player.sendSystemMessage(
                Component.literal("Your grave is at ")
                    .append(Component.literal(String.format("[%d, %d, %d]", position.getX(), position.getY() + 1, position.getZ()))
                        .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN))));
        }
    }

    private static ResolvableProfile getGraveOwner(ItemStack grave){
        return grave.getComponents().get(DataComponents.PROFILE);
    }

    private static boolean canOpenGrave(Level level, Player player, ResolvableProfile owner){
        if(null == owner || owner.id().isEmpty())
            return true;
        if(player.getGameProfile().getId().equals(owner.id().get()))
            return true;

        var rules = level.getServer().getGameRules();
        if(player.hasPermissions(2) && rules.getBoolean(CAN_OP_OPEN_GRAVES))
            return true;

        return false;
    }

    private static boolean isArmorSlot(int i){
        for(var a: Inventory.ALL_ARMOR_SLOTS)
            if(a == i)
                return true;
        return false;
    }

    private static InteractionResult onEntityInteract(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult){
        if(!(entity instanceof ArmorStand armorStand)
            || !entity.getTags().contains(GRAVE_ENTITY_TAG)
            || level.isClientSide()
            || player.isSpectator()
            || hand == InteractionHand.OFF_HAND
            || !player.getMainHandItem().isEmpty()
        )
            return InteractionResult.PASS;

        var grave = armorStand.getItemBySlot(EquipmentSlot.HEAD);
        var owner = getGraveOwner(grave);
        if(!canOpenGrave(level, player, owner))
            return InteractionResult.FAIL;

        LOGGER.info("{} opened grave owned by {}.", player.getName().getString(), owner);
        player.swing(hand, true);

        // copy items back into inventory
        var container = grave.getComponents().get(DataComponents.CONTAINER);
        if(container != null){
            var inventory = player.getInventory();
            var items = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
            container.copyInto(items);

            for(var i = 0; i < items.size(); i += 1){
                var stack = items.get(i);
                if(stack.isEmpty())
                    continue;
                if(inventory.getItem(i).isEmpty()){
                    var item = stack.copyAndClear();
                    inventory.setItem(i, item);

                    // play armor equip sounds
                    if(item.getItem() instanceof ArmorItem armor && isArmorSlot(i)){
                        var equipSound = armor.getEquipSound();
                        level.playSound(null, player.blockPosition(),
                            equipSound.value(), SoundSource.PLAYERS,
                            0.5F, level.random.nextFloat() * 0.1F + 0.9F);
                    } else {
                        level.playSound(null, player.blockPosition(),
                            SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                            0.5F, level.random.nextFloat() * 0.1F + 0.9F);
                    }
                }
            }

            for(var stack: items) {
                if (!stack.isEmpty()) {
                    inventory.placeItemBackInInventory(stack.copyAndClear());
                    level.playSound(null, player.blockPosition(),
                        SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS,
                        0.5F, level.random.nextFloat() * 0.1F + 0.9F);
                }
            }
        }

        // restore experience
        var repairCost = grave.getComponents().get(DataComponents.REPAIR_COST);
        if(repairCost != null)
            player.giveExperiencePoints(repairCost);

        // clean up armor stand
        entity.remove(Entity.RemovalReason.DISCARDED);

        level.playSound(null, player.blockPosition(),
            SoundEvents.SOUL_SAND_BREAK, SoundSource.PLAYERS,
            0.3F, level.random.nextFloat() * 0.1F + 0.9F);

        return InteractionResult.SUCCESS_NO_ITEM_USED;
    }
}
