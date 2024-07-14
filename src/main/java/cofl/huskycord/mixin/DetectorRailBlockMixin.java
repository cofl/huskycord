package cofl.huskycord.mixin;

import cofl.huskycord.HuskycordMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity.MAX_BOOKS_IN_STORAGE;

@Mixin(DetectorRailBlock.class)
public abstract class DetectorRailBlockMixin {
    @Shadow
    @Final
    public static BooleanProperty POWERED;

    @Shadow
    protected abstract <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos blockPos, Class<T> class_, Predicate<Entity> predicate);

    @Shadow
    protected abstract void updatePowerToConnected(Level level, BlockPos blockPos, BlockState blockState, boolean bl);

    @SuppressWarnings("UnreachableCode")
    @Inject(method = "checkPressed", at = @At("HEAD"), cancellable = true)
    private void checkPressed(Level level, BlockPos blockPos, BlockState state, CallbackInfo ci){
        var below = level.getBlockState(blockPos.below());
        if (!below.is(Blocks.CHISELED_BOOKSHELF))
            return;
        var entity = (ChiseledBookShelfBlockEntity)level.getBlockEntity(blockPos.below());
        if(entity == null || entity.isEmpty())
            return;

        // from here on, we are now handling special logic and should cancel the standard call
        var carts = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, e -> true);
        var tickets = getTickets(carts);
        var matches = false;
        if (!tickets.isEmpty()) {
            for (var i = 0; i < MAX_BOOKS_IN_STORAGE && !matches; i += 1) {
                var book = entity.getItem(i);
                if (book.isEmpty())
                    continue;
                matches = book.has(DataComponents.WRITTEN_BOOK_CONTENT)
                    ? Objects.requireNonNull(book.get(DataComponents.WRITTEN_BOOK_CONTENT)).getPages(false)
                        .stream()
                        .map(Component::getString)
                        .anyMatch(text -> tickets.stream().anyMatch(p -> p.matcher(text).find()))
                    : book.has(DataComponents.WRITABLE_BOOK_CONTENT)
                    && Objects.requireNonNull(book.get(DataComponents.WRITABLE_BOOK_CONTENT)).getPages(false)
                        .anyMatch(text -> tickets.stream().anyMatch(p -> p.matcher(text).find()));
            }
        }

        var rail = (DetectorRailBlock)(Object)this;
        var isPowered = state.getValue(POWERED);
        if (matches != isPowered){
            var state2 = state.setValue(POWERED, matches);
            level.setBlock(blockPos, state2, 3);
            updatePowerToConnected(level, blockPos, state2, true);
            level.updateNeighborsAt(blockPos, rail);
            level.updateNeighborsAt(blockPos.below(), rail);
            level.setBlocksDirty(blockPos, state, state2);
        }

        if (matches){
            level.scheduleTick(blockPos, rail, 20);
        }

        level.updateNeighbourForOutputSignal(blockPos, rail);

        ci.cancel();
    }

    @Unique @NotNull
    private NonNullList<Pattern> getTickets(List<AbstractMinecart> cart){
        var tickets = NonNullList.<Pattern>create();
        if (cart.isEmpty())
            return tickets;

        if (cart.getFirst().getFirstPassenger() instanceof ServerPlayer player){
            var inventory = player.getInventory();
            var size = inventory.getContainerSize();
            for (var i = 0; i < size; i += 1) {
                var stack = inventory.getItem(i);
                if (!stack.is(HuskycordMod.TICKET_ITEM) || !stack.has(DataComponents.CUSTOM_NAME))
                    continue;
                var name = Objects.requireNonNull(stack.get(DataComponents.CUSTOM_NAME)).getString();
                if (!name.isBlank())
                    tickets.add(Pattern.compile("^" + Pattern.quote(name) + "$", Pattern.MULTILINE));
            }
        } else if (cart.getFirst() instanceof AbstractMinecartContainer container){
            for(var stack: container.getItemStacks()){
                if(!stack.is(HuskycordMod.TICKET_ITEM) || !stack.has(DataComponents.CUSTOM_NAME))
                    continue;
                var name = Objects.requireNonNull(stack.get(DataComponents.CUSTOM_NAME)).getString();
                if (!name.isBlank())
                    tickets.add(Pattern.compile("^" + Pattern.quote(name) + "$", Pattern.MULTILINE));
            }
        }

        return tickets;
    }
}
