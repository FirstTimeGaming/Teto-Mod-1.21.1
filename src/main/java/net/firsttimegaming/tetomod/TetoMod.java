package net.firsttimegaming.tetomod;

import net.firsttimegaming.tetomod.block.ModBlocks;
import net.firsttimegaming.tetomod.block.entity.ModBlockEntities;
import net.firsttimegaming.tetomod.config.PlushTierConfigManager;
import net.firsttimegaming.tetomod.item.ModCreativeModeTabs;
import net.firsttimegaming.tetomod.item.ModItems;
import net.firsttimegaming.tetomod.screen.ModMenuTypes;
import net.firsttimegaming.tetomod.screen.PlushScreen;
import net.firsttimegaming.tetomod.sound.ModSounds;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

/**
 * Main mod class for the Teto Mod.
 * <p>
 * This mod adds a plush block with a tiered trading system where players
 * can exchange items for random rewards based on the selected tier.
 */
@Mod(TetoMod.MOD_ID)
public class TetoMod {

    /** The unique mod identifier. Must match the entry in neoforge.mods.toml. */
    public static final String MOD_ID = "tetomod";

    /** Logger instance for mod-wide logging. */
    public static final Logger LOGGER = LogUtils.getLogger();

    /**
     * Constructs the main mod instance.
     * <p>
     * This is the entry point called by NeoForge when the mod is loaded.
     * Registers all mod components and event listeners.
     *
     * @param modEventBus the mod-specific event bus
     * @param modContainer the mod container
     */
    public TetoMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        NeoForge.EVENT_BUS.register(this);

        ModCreativeModeTabs.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModSounds.register(modEventBus);

        PlushTierConfigManager.load();

        modEventBus.addListener(this::addCreative);
    }

    /**
     * Called during the common setup phase of mod loading.
     *
     * @param event the common setup event
     */
    private void commonSetup(FMLCommonSetupEvent event) {
        // Common setup tasks can be added here
    }

    /**
     * Adds items to creative mode tabs.
     *
     * @param event the creative tab contents event
     */
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        // Additional creative tab items can be added here
    }

    /**
     * Called when the server is starting.
     *
     * @param event the server starting event
     */
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        // Server startup tasks can be added here
    }

    /**
     * Client-side event subscriber for registering client-only features.
     */
    @EventBusSubscriber(modid = TetoMod.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    static class ClientModEvents {

        /**
         * Called during client setup.
         *
         * @param event the client setup event
         */
        @SubscribeEvent
        static void onClientSetup(FMLClientSetupEvent event) {
            // Client setup tasks can be added here
        }

        /**
         * Registers menu screens for containers.
         *
         * @param event the menu screen registration event
         */
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.PLUSH_MENU.get(), PlushScreen::new);
        }
    }
}
