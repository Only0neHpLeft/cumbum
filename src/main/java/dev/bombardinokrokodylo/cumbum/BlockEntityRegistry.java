package dev.bombardinokrokodylo.cumbum;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class BlockEntityRegistry {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, Cumbum.MODID);

    // Use BlockEntityType<?> to match the return type from getType()
    public static final RegistryObject<BlockEntityType<TNTChestBlockEntity>> TNT_CHEST_ENTITY =
            BLOCK_ENTITIES.register("tnt_chest",
                    () -> BlockEntityType.Builder.of(
                            TNTChestBlockEntity::new,
                            Cumbum.TNT_CHEST_BLOCK.get()
                    ).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}