package net.firsttimegaming.tetomod.item;

import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TetoMod.MOD_ID);

    public static final Supplier<CreativeModeTab> TETO_TAB = CREATIVE_MODE_TAB.register("tetotab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.TETO_BLOCK.get()))
                    .title(Component.translatable("creativetab.tetomod.tetotab"))
                    .displayItems(((itemDisplayParameters, output) -> {
                        output.accept(new ItemStack(ModBlocks.TETO_BLOCK.get()));
//                        output.accept(new ItemStack(ModItems.TETO_ITEM.get()));
                    }))
                    .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
