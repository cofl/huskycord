package cofl.huskycord;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.*;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Objects;

import static cofl.huskycord.HuskycordMod.LOGGER;
import static cofl.huskycord.HuskycordMod.MOD_NAME;

public class Graves {
    private static final String GRAVE_ENTITY_TAG = "huskycordGrave";
    public static final GameRules.Key<GameRules.IntegerValue> DEATH_EXPERIENCE_LOSS =
        GameRuleRegistry.register("deathExperienceLoss", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(0, 0, 100));
    public static final GameRules.Key<GameRules.BooleanValue> KEEP_EXPERIENCE_ON_DEATH =
        GameRuleRegistry.register("keepExperienceOnDeath", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));

    public static final GameRules.Key<GameRules.BooleanValue> CAN_OP_OPEN_GRAVES =
        GameRuleRegistry.register("canOpOpenGraves", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.BooleanValue> TELL_GRAVE_POSITION =
        GameRuleRegistry.register("tellGravePosition", GameRules.Category.PLAYER, GameRuleFactory.createBooleanRule(false));
    public static final GameRules.Key<GameRules.IntegerValue> TELL_GRAVE_DISTANCE =
        GameRuleRegistry.register("tellGraveDistance", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(4, 0, 16));
    public static final GameRules.Key<GameRules.IntegerValue> GRAVE_DANGEROUS_CEILING_HEIGHT =
        GameRuleRegistry.register("graveDangerousCeilingHeight", GameRules.Category.PLAYER, GameRuleFactory.createIntRule(3, 0, 16));

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
    // and these if they're waterlogged
    public static final TagKey<Block> GRAVE_FLOAT_WATERLOGGED = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_float_waterlogged"));
    // but only if they're the first block in the column
    public static final TagKey<Block> GRAVE_FLOAT_FROM_WATERLOGGED = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_float_from_if_waterlogged"));
    // but only if they're the first block in the column and in the lower placement half
    public static final TagKey<Block> GRAVE_FLOAT_FROM_WATERLOGGED_LOWER = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_float_from_if_waterlogged_lower"));
    // and these if they're waterlogged and open
    public static final TagKey<Block> GRAVE_FLOAT_WATERLOGGED_IF_OPEN = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_float_waterlogged_if_open"));
    // and this if the block above is waterlogged
    public static final TagKey<Block> GRAVE_FLOAT_IF_ABOVE_IS_FLOAT = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_float_if_above_is_float"));

    // and these support graves on their own
    public static final TagKey<Block> GRAVE_SUPPORTS = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_supports"));
    // ...only if they're closed
    public static final TagKey<Block> GRAVE_SUPPORTS_IF_CLOSED = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_supports_if_closed"));

    // graves can be placed in these blocks
    public static final TagKey<Block> GRAVE_HEAD_PLACEABLE = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_placeable"));

    // these can be replaced with support blocks
    public static final TagKey<Block> GRAVE_REPLACEABLE = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_replaceable"));

    // and if they float over these, try to peek through the ceiling
    public static final TagKey<Block> GRAVE_DANGEROUS = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_dangerous"));

    public static final TagKey<Block> GRAVE_SPECIAL = TagKey.create(
        Registries.BLOCK,
        ResourceLocation.fromNamespaceAndPath(MOD_NAME, "grave_special"));

    public static void register(){
        ServerLivingEntityEvents.ALLOW_DEATH.register(((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer player){
                onDeath(player);
            }
            return true;
        }));

        UseEntityCallback.EVENT.register(Graves::onEntityInteract);
        UseBlockCallback.EVENT.register(Graves::onBlockInteract);
    }

    public static void onDeath(ServerPlayer player){
        var level = player.level();
        if(level.isClientSide())
            return;

        var server = player.level().getServer();
        if(null == server)
            return;

        var rules = server.getGameRules();
        var keepExperience = getRetainedExperience(player, rules);
        if(player.getInventory().isEmpty() && keepExperience <= 0)
            return;

        var blockPosition = getGravePosition(level, player.blockPosition(), rules);
        if(blockPosition == null){
            LOGGER.info("Couldn't spawn grave for {}, retaining inventory.", player.getName().getString());
            // TODO: retain items, set XP to amount
            // TODO: send player a system message letting them know.
            return;
        }

        var realPosition = ensureSupported(level, blockPosition);
        var position = adjustedPosition(level, realPosition);
        var grave = createGraveItem(player, keepExperience);
        var entity = createGraveEntity(level, grave, position);
        var nameplate = createNameplate(player, level, entity);

        level.addFreshEntity(entity);
        level.addFreshEntity(nameplate);
        PlayerGraveData.getServerState(server).add(entity);
        LOGGER.info("Spawned grave for {} at {} in {}",
            player.getName().getString(),
            position.relative(Direction.UP, 1),
            level.dimension().location());

        if (rules.getBoolean(TELL_GRAVE_POSITION)
            || !player.position().closerThan(position, rules.getInt(TELL_GRAVE_DISTANCE)))
            player.sendSystemMessage(Component.literal("Your grave is at ")
                .append(ChatUtil.asComponent(position.relative(Direction.UP, 1), level.dimension())));
    }

    // player.totalExperience is inaccurate when adding XP with the /xp (levels) command.
    private static int getRetainedExperience(ServerPlayer player, GameRules rules){
        if(rules.getBoolean(KEEP_EXPERIENCE_ON_DEATH)){
            // run our own retention logic
            var lossRate = rules.getInt(DEATH_EXPERIENCE_LOSS) / 100f;
            var retentionRate = 1 - lossRate;
            var totalExperience = getPlayerTotalExperience(player);
            return (int)Math.floor(totalExperience * retentionRate);
        } else {
            // default is you drop at most enough XP to reach level 7, based on the number of current levels.
            return Math.min(7 * player.experienceLevel, 100);
        }
    }

    private static int getPlayerTotalExperience(ServerPlayer player){
        var levels = player.experienceLevel;
        var nextLevel = player.getXpNeededForNextLevel();
        var partial = nextLevel * player.experienceProgress;
        if(levels <= 16)
            return Math.round(levels * levels + 6.0f * levels + partial);
        if(levels <= 31)
            return Math.round(2.5f * levels * levels - 40.5f * levels + 360f + partial);
        return Math.round(4.5f * levels * levels - 162.5f * levels + 2220f + partial);
    }

    private static ItemStack createGraveItem(ServerPlayer player, int retainedExperience){
        var stack = new ItemStack(Items.STONE_BRICK_WALL);
        stack.set(DataComponents.PROFILE, new ResolvableProfile(player.getGameProfile()));

        var inventory = player.getInventory();
        var list = NonNullList.withSize(inventory.getContainerSize(), ItemStack.EMPTY);
        for(var i = 0; i < list.size(); i += 1){
            list.set(i, processItem(inventory.getItem(i)));
        }
        stack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(list));

        stack.set(DataComponents.REPAIR_COST, retainedExperience);
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

    private static ArmorStand createGraveEntity(Level level, ItemStack graveItem, Vec3 position){
        var entity = new ArmorStand(EntityType.ARMOR_STAND, level);
        entity.addTag(GRAVE_ENTITY_TAG);
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
        entity.setXRot(0);
        entity.setPos(position);

        return entity;
    }

    private static final float BLOCK_VIEW_DISTANCE_RATIO = 64.0f;
    private static Display.TextDisplay createNameplate(ServerPlayer player, Level level, ArmorStand grave){
        var nameplate = new Display.TextDisplay(EntityType.TEXT_DISPLAY, level);
        nameplate.addTag(GRAVE_ENTITY_TAG);
        nameplate.setPos(grave.position().relative(Direction.UP, 1));
        nameplate.setViewRange(6f / BLOCK_VIEW_DISTANCE_RATIO);
        nameplate.setBillboardConstraints(Display.BillboardConstraints.VERTICAL);
        nameplate.startRiding(grave, true);
        nameplate.setBackgroundColor(0x00_00_00_00);
        nameplate.setFlags((byte) (nameplate.getFlags() & ~Display.TextDisplay.FLAG_USE_DEFAULT_BACKGROUND));

        var gray = Style.EMPTY.withColor(ChatFormatting.GRAY);
        var wi = Style.EMPTY.withColor(ChatFormatting.WHITE).withItalic(true);
        nameplate.setText(Component.literal("[").setStyle(gray)
            .append(player.getName().plainCopy().setStyle(wi))
            .append(Component.literal("]").setStyle(gray)));

        var data = nameplate.getEntityData();
        data.set(Display.DATA_TRANSLATION_ID, new Vector3f(0, -0.35f, 0));

        return nameplate;
    }

    private static @Nullable BlockPos getGravePosition(Level level, BlockPos position, GameRules rules){
        if (canFloat(level, position, true)) {
            var found = floatPosition(level, position, rules);
            if (found != null)
                return found;
        }

        if(isDangerous(level, position)){
            var found = tryEscapeDanger(level, position, rules);
            if (found != null)
                return found;
        }

        return sinkPosition(level, position);
    }

    private static @Nullable BlockPos sinkPosition(Level level, BlockPos initial){
        var position = trySink(level, initial, level.getMinBuildHeight());

        // we found a grave location
        if (position.getY() > level.getMinBuildHeight())
            return position.immutable();

        // fell into the void, loop back to the top and try again
        position = trySink(level, initial.atY(level.getMaxBuildHeight() + 1), initial.getY());
        if (position.getY() > initial.getY())
            return position.immutable();

        // we fell into the void for real
        position = initial.atY(level.getMinBuildHeight() + 1);

        // but we can place a block at the bottom of the column
        if(canReplace(level, position))
            return position.immutable();

        // we fell into the void *and* we can't recover
        // don't spawn a grave, just keepInventory!
        return null;
    }

    private static boolean canSink(Level level, BlockPos position){
        var state = level.getBlockState(position);
        return state.is(GRAVE_SINK)
            || state.is(GRAVE_SINK_IF_OPEN) && state.getValue(BlockStateProperties.OPEN);
    }

    private static boolean canReplace(Level level, BlockPos position){
        var state = level.getBlockState(position);
        return state.is(GRAVE_REPLACEABLE);
    }

    private static @NotNull BlockPos trySink(Level level, BlockPos initial, int stopAtY){
        var position = initial.mutable();
        var floating = canFloat(level, position);
        while (position.getY() >= stopAtY && canSink(level, position)){
            // we need to detect if we go from not floating to floating
            // then, if at that transition point we can place, do so.

            if(floating){
                // first, if we *are* floating, keep floating or stop.
                floating = canFloat(level, position);
            } else if(canFloat(level, position)) {
                // otherwise, detect if we just entered a new floating region.
                floating = true;
                // and if the last block we were in is placeable, place there.
                var above = position.above();
                if (canPlace(level, above)){
                    // we found the highest placeable position below us
                    return above;
                }
            }

            // lastly, move down so we can try again
            position.move(Direction.DOWN, 1);
        }

        // we always sink one block too far when exiting the loop
        return position.above();
    }

    private static @Nullable BlockPos floatPosition(Level level, BlockPos initial, GameRules rules){
        // floating deals with the "head height" of the grave
        var position = initial.mutable();

        // float up while our feet are in a float-able block
        // we will rise to one block above the last block we can float in
        // and will adjust later so our head is in that block.
        if (position.getY() <= level.getMaxBuildHeight() && canFloat(level, position, true))
            position.move(Direction.UP, 1);
        while (position.getY() <= level.getMaxBuildHeight() && canFloat(level, position, false))
            position.move(Direction.UP, 1);

        if (canPlace(level, position))
            return position.immutable();

        if (isDangerous(level, position.below()))
            return tryEscapeDanger(level, position, rules);

        return null;
    }

    private static @Nullable BlockPos tryEscapeDanger(Level level, BlockPos initial, GameRules rules){
        var searchHeight = rules.getInt(GRAVE_DANGEROUS_CEILING_HEIGHT);
        for(var i = 1; i <= searchHeight; i += 1){
            var position = initial.above(i);
            if (canPlace(level, position) && !isDangerous(level, position))
                return position.immutable();
            if (position.getY() > level.getMaxBuildHeight())
                break;
        }

        return null;
    }

    private static boolean canFloat(Level level, BlockPos position){
        return canFloat(level, position, false);
    }

    private static boolean canFloat(Level level, BlockPos position, boolean isFirst){
        var state = level.getBlockState(position);
        if(state.is(GRAVE_FLOAT)
            || isFirst && state.is(GRAVE_FLOAT_IF_ABOVE_IS_FLOAT) && canFloat(level, position.above(), false))
            return true;

        // if not waterlogged, cannot float
        if(state.getFluidState().isEmpty())
            return false;

        // all below here waterlogged
        // default is float if waterlogged
        if(!state.is(GRAVE_SPECIAL))
            return isFirst;

        return state.is(GRAVE_FLOAT_WATERLOGGED)
            || isFirst && state.is(GRAVE_FLOAT_FROM_WATERLOGGED)
            || isFirst && state.is(GRAVE_FLOAT_FROM_WATERLOGGED_LOWER) && isLower(state)
            || state.is(GRAVE_FLOAT_WATERLOGGED_IF_OPEN) && state.getValue(BlockStateProperties.OPEN);
    }

    private static boolean isLower(BlockState state){
        if (state.hasProperty(BlockStateProperties.HALF))
            return state.getValue(BlockStateProperties.HALF) == Half.BOTTOM;
        if (state.hasProperty(BlockStateProperties.SLAB_TYPE))
            return state.getValue(BlockStateProperties.SLAB_TYPE) == SlabType.BOTTOM;
        return false;
    }

    private static boolean canPlace(Level level, BlockPos position){
        var state = level.getBlockState(position);
        return state.is(GRAVE_HEAD_PLACEABLE) && (state.is(GRAVE_SUPPORTS) || canSupport(state, level, position.below()));
    }

    private static boolean canSupport(BlockState supported, Level level, BlockPos position){
        var state = level.getBlockState(position);
        return state.is(GRAVE_SUPPORTS)
            || !state.is(GRAVE_SPECIAL)
            || state.is(GRAVE_SUPPORTS_IF_CLOSED) && !state.getValue(BlockStateProperties.OPEN)
            || state.is(GRAVE_REPLACEABLE)

            // we can put lily pads here!
            || state.getFluidState().isSourceOfType(Fluids.WATER) && supported.is(GRAVE_REPLACEABLE);
    }

    /**
     * @param level The world the block is in
     * @param position The position of the block
     * @return If the block is dangerous (we should try to peek through ceilings)
     */
    private static boolean isDangerous(Level level, BlockPos position){
        var state = level.getBlockState(position);
        return state.is(GRAVE_DANGEROUS) || !state.is(GRAVE_SPECIAL);
    }

    private static @NotNull BlockPos ensureSupported(Level level, BlockPos position){
        var below = level.getBlockState(position.below());
        if(below.is(GRAVE_SUPPORTS)
            || !below.is(GRAVE_SPECIAL)
            || below.is(GRAVE_SUPPORTS_IF_CLOSED) && !below.getValue(BlockStateProperties.OPEN))
            return position;

        var state = level.getBlockState(position);
        if (below.getFluidState().isSourceOfType(Fluids.WATER) && state.is(GRAVE_REPLACEABLE)){
            level.setBlock(position, Blocks.LILY_PAD.defaultBlockState(), 2);

            // if we place a lily pad, we need to place the grave *above* the lily pad
            return position.above();
        }

        if(!below.is(GRAVE_REPLACEABLE))
            // just cross your fingers and hope things work out.
            return position;

        if (level.dimension() == Level.END)
            level.setBlock(position.below(), Blocks.END_STONE.defaultBlockState(), 2);
        else if (level.dimension() == Level.NETHER)
            level.setBlock(position.below(), Blocks.NETHERRACK.defaultBlockState(), 2);
        else if (position.getY() >= level.getSeaLevel() && !below.is(Blocks.LAVA))
            level.setBlock(position.below(), Blocks.DIRT.defaultBlockState(), 2);
        else if (position.getY() < 0)
            level.setBlock(position.below(), Blocks.DEEPSLATE.defaultBlockState(), 2);
        else
            level.setBlock(position.below(), Blocks.STONE.defaultBlockState(), 2);
        return position;
    }

    private static Vec3 adjustedPosition(Level level, BlockPos position){
        return new Vec3(
            position.getX() + 0.5,
            position.getY() - 0.85 - blockDepth(level, position.below()),
            position.getZ() + 0.5
        );
    }

    private static float blockDepth(Level level, BlockPos position){
        var state = level.getBlockState(position);
        if(state.is(Blocks.SCULK_SHRIEKER))
            return 0.5f;
        if (state.is(BlockTags.CAULDRONS) || state.is(Blocks.COMPOSTER))
            return 0.75f;

        var shape = state.getShape(level, position, CollisionContext.empty());
        var depth = 1f - (float)Math.clamp(0, shape.max(Direction.Axis.Y), 1);

        // very, very short blocks have slight issues with how they line up
        if (depth >= 0.9f)
            return 1f;

        return depth;
    }

    private static InteractionResult onEntityInteract(Player player, Level level, InteractionHand hand, Entity entity, @Nullable EntityHitResult hitResult){
        if (level.isClientSide()
            || player.isSpectator()
            || hand == InteractionHand.OFF_HAND
            || !player.getMainHandItem().isEmpty()
            || !entity.getTags().contains(GRAVE_ENTITY_TAG))
            return InteractionResult.PASS;

        if(entity instanceof ArmorStand grave) {
            if (tryRestoreGrave(player, level, grave))
                return InteractionResult.SUCCESS_NO_ITEM_USED;
        }

        return InteractionResult.FAIL;
    }

    private static InteractionResult onBlockInteract(Player player, Level level, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide()
            || player.isSpectator()
            || hand == InteractionHand.OFF_HAND
            || !player.getMainHandItem().isEmpty())
            return InteractionResult.PASS;

        var pos = player.getEyePosition(1f);
        var direction = player.getViewVector(1f);
        var reach = player.entityInteractionRange();
        var limit = pos.add(direction.multiply(reach, reach, reach));

        var result = ProjectileUtil.getEntityHitResult(level, player, pos, limit, new AABB(pos, limit), EntitySelector.NO_SPECTATORS
            .and(e -> e != null && e.isPickable() && e instanceof ArmorStand));

        if (result != null && result.getEntity() instanceof ArmorStand grave && tryRestoreGrave(player, level, grave))
            return InteractionResult.SUCCESS_NO_ITEM_USED;

        return InteractionResult.PASS;
    }

    private static boolean tryRestoreGrave(Player player, Level level, ArmorStand graveEntity){
        var server = player.getServer();
        if(server == null)
            // how does this even happen?
            // better safe than sorry.
            return false;

        var grave = getGraveItem(graveEntity);
        var owner = getGraveOwner(grave);
        if(!canOpenGrave(level, player, owner))
            return false;

        LOGGER.info("{} opened grave owned by {}.", player.getName().getString(), owner.name().orElseGet(() -> owner.gameProfile().getName()));
        player.swing(InteractionHand.MAIN_HAND, true);

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
        for(var passenger: graveEntity.getPassengers()){
            if (passenger.getTags().contains(GRAVE_ENTITY_TAG)){
                passenger.remove(Entity.RemovalReason.DISCARDED);
            }
        }
        graveEntity.remove(Entity.RemovalReason.DISCARDED);
        PlayerGraveData.getServerState(server).remove(graveEntity);

        level.playSound(null, player.blockPosition(),
            SoundEvents.SOUL_SAND_BREAK, SoundSource.PLAYERS,
            0.3F, level.random.nextFloat() * 0.1F + 0.9F);

        return true;
    }

    public static @NotNull ItemStack getGraveItem(ArmorStand graveEntity) {
        return graveEntity.getItemBySlot(EquipmentSlot.HEAD);
    }

    public static ResolvableProfile getGraveOwner(ItemStack grave){
        return grave.getComponents().get(DataComponents.PROFILE);
    }

    private static boolean canOpenGrave(Level level, Player player, ResolvableProfile owner){
        if(null == owner || owner.id().isEmpty())
            return true;
        if(player.getGameProfile().getId().equals(owner.id().get()))
            return true;

        var rules = Objects.requireNonNull(level.getServer()).getGameRules();
        return player.hasPermissions(2) && rules.getBoolean(CAN_OP_OPEN_GRAVES);
    }

    private static boolean isArmorSlot(int i){
        for(var a: Inventory.ALL_ARMOR_SLOTS)
            if(a == i)
                return true;
        return false;
    }
}
