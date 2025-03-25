package dev.bombardinokrokodylo.cumbum;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles conversion of TNTEnderChest block entities to TNTChest block entities
 * during world loading to ensure compatibility with existing saves.
 */
@Mod.EventBusSubscriber(modid = Cumbum.MODID)
public class DataCompatibilityHandler {

    @SubscribeEvent
    public static void onChunkLoad(ChunkDataEvent.Load event) {
        CompoundTag levelTag = event.getData();

        // Check if there are any block entities in this chunk
        if (!levelTag.contains("block_entities")) {
            return;
        }

        // Check if the chunk is a LevelChunk before processing
        if (event.getChunk() instanceof LevelChunk levelChunk) {
            // Convert any TNTEnderChest block entities to TNTChest block entities
            processChunk(levelChunk);
        } else {
            Cumbum.LOGGER.warn("Cannot convert block entities in chunk: not a LevelChunk");
        }
    }

    private static void processChunk(LevelChunk chunk) {
        List<BlockEntity> entitiesToUpdate = new ArrayList<>();

        // Find all block entities in this chunk that might need conversion
        for (BlockEntity entity : chunk.getBlockEntities().values()) {
            BlockPos pos = entity.getBlockPos();

            // We need to check for both the new entity type and any old entities
            // Since we can't directly check for the old TNTEnderChestBlockEntity type
            // (as it no longer exists), we'll look for entities with the old ID in their NBT
            CompoundTag tag = entity.saveWithoutMetadata();
            if (tag.contains("id") && tag.getString("id").equals("cumbum:tnt_chest")) {
                Cumbum.LOGGER.info("Found old Chest entity at {}", pos);
                entitiesToUpdate.add(entity);
            }
        }

        // Update all the block entities we found
        for (BlockEntity entity : entitiesToUpdate) {
            chunk.removeBlockEntity(entity.getBlockPos());

            // Create a new TNTChestBlockEntity with the same data
            TNTChestBlockEntity newEntity = new TNTChestBlockEntity(entity.getBlockPos(),
                    chunk.getBlockState(entity.getBlockPos()));

            // Copy the data from the old entity
            CompoundTag tag = entity.saveWithoutMetadata();
            newEntity.load(tag);

            // Add the new entity to the chunk
            chunk.addAndRegisterBlockEntity(newEntity);

            // Mark the chunk as dirty to ensure changes are saved
            chunk.setUnsaved(true);

            Cumbum.LOGGER.info("Converted block entity at {} from TNTEnderChest to TNTChest",
                    entity.getBlockPos());
        }
    }
}