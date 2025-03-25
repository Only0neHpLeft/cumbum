package dev.bombardinokrokodylo.cumbum;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.MissingMappingsEvent;

/**
 * Handles remapping of old registry entries to new ones.
 * This is crucial for worlds created with previous versions of the mod.
 */
@Mod.EventBusSubscriber(modid = Cumbum.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class RegistryRemapper {

    @SubscribeEvent
    public static void onMissingMappings(MissingMappingsEvent event) {
        // Handle block remappings
        event.getMappings(ForgeRegistries.Keys.BLOCKS, Cumbum.MODID).forEach(mapping -> {
            if (mapping.getKey().getPath().equals("tnt_chest")) {
                mapping.remap(Cumbum.TNT_CHEST_BLOCK.get());
                Cumbum.LOGGER.info("Remapped block: tnt_chest -> tnt_chest");
            }
        });

        // Handle item remappings
        event.getMappings(ForgeRegistries.Keys.ITEMS, Cumbum.MODID).forEach(mapping -> {
            if (mapping.getKey().getPath().equals("tnt_chest")) {
                mapping.remap(Cumbum.TNT_CHEST_BLOCK_ITEM.get());
                Cumbum.LOGGER.info("Remapped item: tnt_chest -> tnt_chest");
            }
        });

        // Handle block entity remappings
        event.getMappings(ForgeRegistries.Keys.BLOCK_ENTITY_TYPES, Cumbum.MODID).forEach(mapping -> {
            if (mapping.getKey().getPath().equals("tnt_chest")) {
                mapping.remap(BlockEntityRegistry.TNT_CHEST_ENTITY.get());
                Cumbum.LOGGER.info("Remapped block entity: tnt_chest -> tnt_chest");
            }
        });
    }
}