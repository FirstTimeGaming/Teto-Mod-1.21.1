package net.firsttimegaming.tetomod.block.entity;

import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities{

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, TetoMod.MOD_ID);

    public static final Supplier<BlockEntityType<PlushBlockEntity>> PLUSH_BLOCK_ENTITY = BLOCK_ENTITIES.register("plush_block_entity",
            () -> BlockEntityType.Builder.of(PlushBlockEntity::new, ModBlocks.TETO_BLOCK.get()
            ).build(null)
    );

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
