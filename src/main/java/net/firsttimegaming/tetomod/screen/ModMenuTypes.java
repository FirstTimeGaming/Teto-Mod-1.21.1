package net.firsttimegaming.tetomod.screen;

import net.firsttimegaming.tetomod.TetoMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry class for all mod menu types.
 * <p>
 * Menu types define the container menus used for block and item GUIs.
 */
public class ModMenuTypes {

    /** Deferred register for all menu types in this mod. */
    public static final DeferredRegister<MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, TetoMod.MOD_ID);

    /** The plush block menu type. */
    public static final DeferredHolder<MenuType<?>, MenuType<PlushMenu>> PLUSH_MENU =
            registerMenuType("plush_menu", PlushMenu::new);

    /**
     * Registers a menu type with the given factory.
     *
     * @param name    the registry name
     * @param factory the container factory
     * @param <T>     the menu type
     * @return the deferred holder for the menu type
     */
    private static <T extends AbstractContainerMenu> DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(
            String name, IContainerFactory<T> factory) {
        return MENU_TYPES.register(name, () -> IMenuTypeExtension.create(factory));
    }

    /**
     * Registers all menu types to the event bus.
     *
     * @param eventBus the mod event bus
     */
    public static void register(IEventBus eventBus) {
        MENU_TYPES.register(eventBus);
    }
}
