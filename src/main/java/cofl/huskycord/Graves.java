package cofl.huskycord;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

import static cofl.huskycord.HuskycordMod.LOGGER;
import static cofl.huskycord.HuskycordMod.MOD_NAME;

public class Graves {
    public static final GameRules.Key<GameRules.IntegerValue> DEATH_EXPERIENCE_LOSS =
        GameRuleRegistry.register("deathExperienceLoss", Category.PLAYER, GameRuleFactory.createIntRule(0, 0, 100));
    public static final GameRules.Key<GameRules.BooleanValue> CAN_OP_OPEN_GRAVES =
        GameRuleRegistry.register("canOpOpenGraves", Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> TELL_GRAVE_POSITION =
        GameRuleRegistry.register("tellGravePosition", Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.IntegerValue> TELL_GRAVE_DISTANCE =
        GameRuleRegistry.register("tellGraveDistance", Category.PLAYER, GameRuleFactory.createIntRule(4, 0, 16));
    public static final GameRules.Key<GameRules.IntegerValue> GRAVE_DANGEROUS_CEILING_HEIGHT =
        GameRuleRegistry.register("graveDangerousCeilingHeight", Category.PLAYER, GameRuleFactory.createIntRule(3, 0, 16));
    public static final GameRules.Key<GameRules.IntegerValue> GRAVE_NAME_VISIBLE_RADIUS =
        GameRuleRegistry.register("graveNameVisibleRadius", Category.PLAYER, GameRuleFactory.createIntRule(5, 1, 16));

    // graves always sink through these
    public static final TagKey<Block> GRAVE_SINK = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_sink"));

    // graves only sink through these if they're not sturdy
    public static final TagKey<Block> GRAVE_SINK_IF_OPEN = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_sink_if_open"));

    // graves always float through these
    public static final TagKey<Block> GRAVE_FLOAT = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_float"));
    // and if they float over these, try to peek through the ceiling
    public static final TagKey<Block> GRAVE_DANGEROUS_FLOAT = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_dangerous_float"));

    // graves can be placed in these blocks
    public static final TagKey<Block> GRAVE_PLACEABLE = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_placeable"));
    // and these support graves on their own
    public static final TagKey<Block> GRAVE_SUPPORTS = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_supports"));

    private static final String GRAVE_ENTITY_TAG = "huskycordGrave";
    public static void register(){
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if(entity instanceof ServerPlayer player){
                onDeath(player);
            }
            return true;
        });

        UseEntityCallback.EVENT.register(Graves::onEntityInteract);

        ServerTickEvents.START_WORLD_TICK.register(Graves::onStartTick);
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

    private static boolean sinks(Level level, BlockPos position){
        var state = level.getBlockState(position);
        return state.is(GRAVE_SINK)
            || state.is(GRAVE_SINK_IF_OPEN, s -> !s.isFaceSturdy(level, position, Direction.UP));
    }

    private static BlockPos sinkGrave(Level level, BlockPos position){
        var pos = position.mutable();
        while (pos.getY() >= level.getMinBuildHeight() && sinks(level, pos)){
            pos.move(Direction.DOWN, 1);
        }
        if(pos.getY() < level.getMinBuildHeight()){
            // fell out of the world, wrap around to the top.
            pos.setY(level.getMaxBuildHeight());
            while(pos.getY() >= position.getY() && sinks(level, pos)){
                pos.move(Direction.DOWN, 1);
            }

            if(pos.getY() < position.getY()){
                pos.setY(level.getMinBuildHeight());
            }
        }

        // shift down 1 if we're above a placeable (lily pads, etc.)
        if (level.getBlockState(pos).is(GRAVE_PLACEABLE))
            return pos.below().immutable();

        return pos.immutable();
    }

    private static BlockPos floatGrave(Level level, BlockPos position){
        // we float in the upper half of the armor stand
        var pos = position.above().mutable();

        // float as high as we can go (checking the block above where the grave is placed)
        // the top half can be above the build limit!
        while(pos.getY() <= level.getMaxBuildHeight() + 1 && level.getBlockState(pos).is(GRAVE_FLOAT)) {
            pos.move(Direction.UP, 1);
        }

        if(pos.getY() <= level.getMinBuildHeight() + 1){
            // we fell into the void -- this should only happen if the column is empty.
            return position.atY(level.getMinBuildHeight());
        }

        // cap at build height
        pos.setY(Math.min(pos.getY(), level.getMaxBuildHeight() + 1));

        // if we can't place a grave here
        if(!level.getBlockState(pos).is(GRAVE_PLACEABLE)){
            // and we're above dangerous blocks, try to go through thin ceilings
            if(level.getBlockState(pos.below()).is(GRAVE_DANGEROUS_FLOAT)){
                var max = Objects.requireNonNull(level.getServer()).getGameRules().getInt(GRAVE_DANGEROUS_CEILING_HEIGHT);
                for(var i = 0; i < max && pos.getY() + i <= level.getMaxBuildHeight(); i += 1){
                    var test = pos.above(i + 1);
                    var state = level.getBlockState(test);
                    // as long as that wouldn't put us in more dangerous blocks
                    if(state.is(GRAVE_PLACEABLE) && !state.is(GRAVE_DANGEROUS_FLOAT))
                        return test.below().immutable();
                }
            }

            // otherwise fail and return the original position
            return position;
        }

        // if we get here, we must be in a safe block.
        return pos.below().immutable();
    }

    private static void placeSupportBlock(Level level, BlockPos position){
        if(level.getBlockState(position.above()).is(GRAVE_SUPPORTS)){
            return;
        }
        if(!sinks(level, position))
            return;

        var dimension = level.dimension();
        if(dimension == Level.NETHER){
            level.setBlock(position, Blocks.NETHERRACK.defaultBlockState(), 2);
        } else if(dimension == Level.END){
            var state = level.getBlockState(position);
            if(state.is(Blocks.WATER) && state.getFluidState().isSource())
                level.setBlock(position.above(), Blocks.LILY_PAD.defaultBlockState(), 2);
            else
                level.setBlock(position, Blocks.END_STONE.defaultBlockState(), 2);
        } else if(dimension == Level.OVERWORLD){
            if(position.above().getY() >= level.getSeaLevel()){
                var state = level.getBlockState(position);
                if(state.is(Blocks.LAVA)){
                    level.setBlock(position, Blocks.STONE.defaultBlockState(), 2);
                } else if(state.is(Blocks.WATER) && state.getFluidState().isSource()){
                    level.setBlock(position.above(), Blocks.LILY_PAD.defaultBlockState(), 2);
                } else {
                    level.setBlock(position, Blocks.DIRT.defaultBlockState(), 2);
                }
            } else if(position.getY() <= 0) {
                level.setBlock(position, Blocks.DEEPSLATE.defaultBlockState(), 2);
            } else {
                level.setBlock(position, Blocks.STONE.defaultBlockState(), 2);
            }
        } else {
            level.setBlock(position, Blocks.STONE.defaultBlockState(), 2);
        }
    }

    public static void onDeath(ServerPlayer player){
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
            if(hasVanishingCurse(stack)) {
                inventoryList.set(i, ItemStack.EMPTY);
                stack.setCount(0);
            } else {
                inventoryList.set(i, removeBindingCurse(stack.copyAndClear()));
            }
        }
        grave.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(inventoryList));

        int playerXp = getTotalExperience(player);
        int totalExperience = Math.max(0, playerXp - (int)Math.ceil(playerXp * deathExperienceLoss));
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        grave.set(DataComponents.REPAIR_COST, totalExperience);

        // set up our armor stand
        var graveEntity = new ArmorStand(EntityType.ARMOR_STAND, level);
        graveEntity.setInvisible(true);
        graveEntity.setNoGravity(true);
        graveEntity.setInvulnerable(true);
        graveEntity.setSilent(true);
        graveEntity.setRightArmPose(new Rotations(0, 90, 0));
        graveEntity.setLeftArmPose(new Rotations(0, 90, 0));
        graveEntity.setHeadPose(new Rotations(180, 0, 0));

        // disable helmet slot: https://minecraft.wiki/w/Armor_Stand/ED
        graveEntity.disabledSlots = 16;
        graveEntity.setItemSlot(EquipmentSlot.HEAD, grave);

        var gray = Style.EMPTY.withColor(ChatFormatting.GRAY);
        graveEntity.setCustomName(Component.literal("[").setStyle(gray)
            .append(player.getName().plainCopy().setStyle(Style.EMPTY
                    .withColor(ChatFormatting.WHITE)
                    .withItalic(true)))
            .append(Component.literal("]").setStyle(gray)));

        // calculate spawn position for grave
        // place a support block if necessary (for void deaths/lakes)
        var position = floatGrave(level, sinkGrave(level, player.blockPosition()));
        LOGGER.info("Placing in {} at {}", level.getBlockState(position), position);
        placeSupportBlock(level, position);

        graveEntity.setXRot(0);
        graveEntity.setPos(position.getX() + 0.5f, position.getY() + 0.15f, position.getZ() + 0.5f);
        graveEntity.addTag(GRAVE_ENTITY_TAG);
        level.addFreshEntity(graveEntity);
        LOGGER.info("Spawned grave for {} at {} in {}", player.getName().getString(), graveEntity.position(), level.dimension());

        if(gameRules.getBoolean(TELL_GRAVE_POSITION) || !player.position().closerThan(graveEntity.position(), gameRules.getInt(TELL_GRAVE_DISTANCE))){
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
        return player.hasPermissions(2) && rules.getBoolean(CAN_OP_OPEN_GRAVES);
    }

    private static boolean isArmorSlot(int i){
        for(var a: Inventory.ALL_ARMOR_SLOTS)
            if(a == i)
                return true;
        return false;
    }

    private static InteractionResult onEntityInteract(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult){
        if(!(entity instanceof ArmorStand graveEntity)
            || !entity.getTags().contains(GRAVE_ENTITY_TAG)
            || level.isClientSide()
            || player.isSpectator()
            || hand == InteractionHand.OFF_HAND
            || !player.getMainHandItem().isEmpty()
        )
            return InteractionResult.PASS;

        var grave = graveEntity.getItemBySlot(EquipmentSlot.HEAD);
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

    private static void onStartTick(ServerLevel world) {
        // every 5 ticks
        if (world.getGameTime() % 5 != 0)
            return;
        var radius = world.getServer().getGameRules().getInt(GRAVE_NAME_VISIBLE_RADIUS);
        for (var armorStand : world.getEntities(EntityTypeTest.forClass(ArmorStand.class),
            armorStand -> armorStand.getTags().contains(GRAVE_ENTITY_TAG))) {
            armorStand.setCustomNameVisible(!world
                .getNearbyPlayers(TargetingConditions.forNonCombat(), armorStand,
                    new AABB(armorStand.blockPosition()).inflate(radius))
                .isEmpty());
        }
    }
}
