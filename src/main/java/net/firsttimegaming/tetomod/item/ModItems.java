package net.firsttimegaming.tetomod.item;

import net.firsttimegaming.tetomod.TetoMod;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(TetoMod.MOD_ID);

//    public static final DeferredItem<Item> TETO_ITEM = ITEMS.register("tetoitem",
//            () -> new Item(new Item.Properties())
//    );

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
