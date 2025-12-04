package net.firsttimegaming.tetomod.block;

import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Registry class for all mod blocks.
 * <p>
 * This class handles the registration of blocks and their corresponding block items
 * to the game registry.
 */
public class ModBlocks {

    /** Deferred register for all blocks in this mod. */
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TetoMod.MOD_ID);

    /** The Teto plush block. */
    public static final DeferredBlock<Block> TETO_BLOCK = registerBlock("tetoblock",
            () -> new PlushBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.WOOL)
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)
            )
    );

    /**
     * Registers a block and its corresponding block item.
     *
     * @param name  the registry name for the block
     * @param block the block supplier
     * @param <T>   the block type
     * @return the deferred block reference
     */
    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    /**
     * Registers a block item for the given block.
     *
     * @param name  the registry name for the item
     * @param block the block to create an item for
     * @param <T>   the block type
     */
    public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name,
                () -> new BlockItem(block.get(), new Item.Properties()));
    }

    /**
     * Registers all blocks to the event bus.
     *
     * @param eventBus the mod event bus
     */
    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
