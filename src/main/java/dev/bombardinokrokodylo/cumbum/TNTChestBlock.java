package dev.bombardinokrokodylo.cumbum;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.function.Supplier;

public class TNTChestBlock extends ChestBlock {
    // Configuration constants (consider moving to config file)
    private static final float EXPLOSION_STRENGTH = 4.0f;
    private static final int COOLDOWN_TICKS = 100;

    public TNTChestBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> blockEntityType) {
        super(properties, blockEntityType);
    }

    @Override
    public BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new TNTChestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                  @NotNull BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null :
                createTickerHelper(blockEntityType, BlockEntityRegistry.TNT_CHEST_ENTITY.get(), TNTChestBlockEntity::serverTick);
    }

    @Override
    public void onRemove(BlockState state, @NotNull Level level, @NotNull BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof TNTChestBlockEntity) {
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void playerWillDestroy(@NotNull Level level, @NotNull BlockPos pos, BlockState state, @NotNull Player player) {
        if (level.isClientSide) {
            super.playerWillDestroy(level, pos, state, player);
            return;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof TNTChestBlockEntity tntChestEntity) {
            ItemStack itemStack = new ItemStack(Cumbum.TNT_CHEST_BLOCK_ITEM.get());

            // Save NBT data safely
            CompoundTag tag = new CompoundTag();
            tntChestEntity.saveAdditional(tag);
            if (!tag.isEmpty()) {
                itemStack.addTagElement("BlockEntityData", tag);
            }

            // Preserve custom name
            tntChestEntity.getOptionalCustomName().ifPresent(itemStack::setHoverName);

            // Handle item drop
            if (!player.isCreative()) {
                spawnAtLocation(level, pos, itemStack);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public @NotNull InteractionResult use(@NotNull BlockState state, Level level, @NotNull BlockPos pos,
                                          @NotNull Player player, @NotNull InteractionHand hand,
                                          @NotNull BlockHitResult hit) {
        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof TNTChestBlockEntity tntEntity)) {
            return InteractionResult.PASS;
        }

        // Cooldown check
        if (tntEntity.isOnCooldown()) {
            player.displayClientMessage(Component.literal("Chest is cooling down!"), true);
            return InteractionResult.FAIL;
        }

        // Ownership check with explosion
        if (!tntEntity.isOwner(player)) {
            handleUnauthorizedAccess(level, pos, player);
            tntEntity.setCooldown(COOLDOWN_TICKS);
            return InteractionResult.SUCCESS;
        }

        // Open menu for owner
        MenuProvider menuProvider = this.getMenuProvider(state, level, pos);
        if (menuProvider != null) {
            player.openMenu(menuProvider);
        }
        return InteractionResult.CONSUME;
    }

    private void handleUnauthorizedAccess(Level level, BlockPos pos, Player player) {
        // Safety check
        if (level.isClientSide) return;

        // Remove block before explosion to prevent duplication
        level.removeBlock(pos, false);

        // Create safer explosion
        level.explode(
                player,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                EXPLOSION_STRENGTH,
                Level.ExplosionInteraction.TNT
        );

        // Optional: Create fire effect
        level.setBlock(pos, Blocks.FIRE.defaultBlockState(), 3);
    }

    private void spawnAtLocation(Level level, BlockPos pos, ItemStack stack) {
        if (level instanceof ServerLevel serverLevel && !stack.isEmpty()) {
            ItemEntity itemEntity = new ItemEntity(
                    serverLevel,
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    stack
            );
            itemEntity.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(itemEntity);
        }
    }
}
