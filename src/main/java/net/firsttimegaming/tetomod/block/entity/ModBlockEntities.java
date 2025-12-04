package net.firsttimegaming.tetomod.block.entity;

import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.block.ModBlocks;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry class for all mod block entities.
 * <p>
 * Block entities are used to store additional data for blocks and provide
 * extended functionality beyond what block states can offer.
 */
public class ModBlockEntities {

    /** Deferred register for all block entities in this mod. */
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TetoMod.MOD_ID);

    /** The plush block entity type. */
    public static final Supplier<BlockEntityType<PlushBlockEntity>> PLUSH_BLOCK_ENTITY =
            BLOCK_ENTITIES.register("plush_block_entity",
                    () -> BlockEntityType.Builder.of(PlushBlockEntity::new, ModBlocks.TETO_BLOCK.get())
                            .build(null)
            );

    /**
     * Registers all block entities to the event bus.
     *
     * @param eventBus the mod event bus
     */
    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
