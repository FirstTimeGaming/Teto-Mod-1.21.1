package net.firsttimegaming.tetomod.item;

import net.firsttimegaming.tetomod.TetoMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Registry class for all mod items.
 * <p>
 * This class handles the registration of items to the game registry.
 */
public class ModItems {

    /** Deferred register for all items in this mod. */
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TetoMod.MOD_ID);

    /**
     * Registers all items to the event bus.
     *
     * @param eventBus the mod event bus
     */
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
