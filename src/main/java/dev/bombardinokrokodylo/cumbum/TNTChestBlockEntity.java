package dev.bombardinokrokodylo.cumbum;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

public class TNTChestBlockEntity extends ChestBlockEntity {
    private UUID ownerUUID = null;
    private String ownerName = "";
    private int cooldownTicks = 0;

    public TNTChestBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return BlockEntityRegistry.TNT_CHEST_ENTITY.get();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TNTChestBlockEntity blockEntity) {
        if (blockEntity.cooldownTicks > 0) {
            blockEntity.cooldownTicks--;
            blockEntity.setChanged();
        }
    }

    // Cooldown methods
    public boolean isOnCooldown() {
        return cooldownTicks > 0;
    }

    public void setCooldown(int ticks) {
        this.cooldownTicks = ticks;
        setChanged();
    }

    public void setOwner(Player player) {
        this.ownerUUID = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    public boolean isOwner(Player player) {
        return player != null && ownerUUID != null && player.getUUID().equals(ownerUUID);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (ownerUUID != null) {
            tag.putUUID("OwnerUUID", ownerUUID);
            tag.putString("OwnerName", ownerName);
        }
        tag.putInt("CooldownTicks", cooldownTicks);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("OwnerUUID")) {
            ownerUUID = tag.getUUID("OwnerUUID");
            ownerName = tag.getString("OwnerName");
        }
        cooldownTicks = tag.getInt("CooldownTicks");
    }

    @Nullable
    @Override
    public Component getCustomName() {
        return super.getCustomName();
    }

    public Optional<Component> getOptionalCustomName() {
        return Optional.ofNullable(getCustomName());
    }


    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}