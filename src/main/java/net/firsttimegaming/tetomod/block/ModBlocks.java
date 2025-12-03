package net.firsttimegaming.tetomod.block;

import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(TetoMod.MOD_ID);

    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    public static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name,
                () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static final DeferredBlock<Block> TETO_BLOCK = registerBlock("tetoblock",
            () -> new PlushBlock(BlockBehaviour.Properties.of()
                    .sound(SoundType.WOOL) // TODO: Modify this with a custom sound or another existing sound
                    .noOcclusion() // Make sure the block does not occlude (needed for transparency)
                    .isViewBlocking((state, level, pos) -> false) // Make sure the block does not block view
            )
    );

    public static void register(net.neoforged.bus.api.IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
