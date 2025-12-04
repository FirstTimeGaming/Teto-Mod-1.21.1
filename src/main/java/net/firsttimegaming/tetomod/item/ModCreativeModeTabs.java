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

/**
 * Registry class for all mod creative mode tabs.
 * <p>
 * Creative mode tabs organize items in the creative inventory for easy access.
 */
public class ModCreativeModeTabs {

    /** Deferred register for all creative mode tabs in this mod. */
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, TetoMod.MOD_ID);

    /** The main Teto mod creative tab containing all mod items and blocks. */
    public static final Supplier<CreativeModeTab> TETO_TAB = CREATIVE_MODE_TAB.register("tetotab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.TETO_BLOCK.get()))
                    .title(Component.translatable("creativetab.tetomod.tetotab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(new ItemStack(ModBlocks.TETO_BLOCK.get()));
                    })
                    .build()
    );

    /**
     * Registers all creative mode tabs to the event bus.
     *
     * @param eventBus the mod event bus
     */
    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }
}
