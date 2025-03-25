package dev.bombardinokrokodylo.cumbum;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TNTChestItem extends BlockItem {

    public TNTChestItem() {
        super(Cumbum.TNT_CHEST_BLOCK.get(), new Item.Properties());
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        boolean result = super.placeBlock(context, state);

        if (result) {
            Level level = context.getLevel();
            BlockPos blockPos = context.getClickedPos();
            Player player = context.getPlayer();

            if (!level.isClientSide && player instanceof ServerPlayer) {
                BlockEntity blockEntity = level.getBlockEntity(blockPos);
                if (blockEntity instanceof TNTChestBlockEntity tntChest) {
                    tntChest.setOwner(player);
                }
            }
        }

        return result;
    }
}