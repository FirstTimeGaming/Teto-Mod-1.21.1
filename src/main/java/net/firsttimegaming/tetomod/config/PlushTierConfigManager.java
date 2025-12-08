package net.firsttimegaming.tetomod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.block.entity.PlushBlockEntity;
import net.neoforged.fml.loading.FMLPaths;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Manager class for loading, saving, and accessing plush tier configurations.
 * <p>
 * This class handles JSON-based configuration persistence and provides
 * access methods for tier configurations and unlock requirements.
 */
public final class PlushTierConfigManager {

    // ==================== Class Variables ====================

    /** GSON instance for JSON serialization/deserialization. */
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /** Configuration file name. */
    private static final String FILE_NAME = "tetomod_plush_tiers.json";

    /** Prefix for tier keys in the configuration. */
    private static final String TIER_KEY_PREFIX = "t";

    /** Default tier unlock requirements. */
    private static final int TIER_1_UNLOCK = 0;
    private static final int TIER_2_UNLOCK = 5;
    private static final int TIER_3_UNLOCK = 10;
    private static final int TIER_4_UNLOCK = 15;
    private static final int TIER_5_UNLOCK = 20;

    /** The loaded configuration root. */
    private static PlushConfigRoot ROOT;

    // ==================== Constructor ====================

    private PlushTierConfigManager() {
        // Utility class - prevent instantiation
    }

    // ==================== Getter Methods ====================

    /**
     * Gets the full configuration root.
     *
     * @return the configuration root, loading it if necessary
     */
    public static PlushConfigRoot getRoot() {
        if (ROOT == null) {
            load();
        }
        return ROOT;
    }

    /**
     * Gets the configuration for a specific tier.
     *
     * @param tierIndex the tier index (0-based: 0 = tier 1, 1 = tier 2, etc.)
     * @return the tier configuration, creating an empty one if missing
     */
    public static PlushTierConfig getTierConfig(int tierIndex) {
        if (ROOT == null) {
            load();
        }

        int idx = Math.max(0, Math.min(tierIndex, PlushBlockEntity.MAX_TIER - 1));
        String key = TIER_KEY_PREFIX + (idx + 1);

        PlushTierConfig tier = ROOT.tiers.get(key);
        if (tier == null) {
            tier = new PlushTierConfig();
            ROOT.tiers.put(key, tier);
        }
        return tier;
    }

    /**
     * Gets the number of previous tier completions required to unlock a tier.
     *
     * @param tierIndex the tier index (0-based)
     * @return the required completions of the previous tier, or 0 if always available
     */
    public static int getRequiredCompletionsForTier(int tierIndex) {
        if (ROOT == null) {
            load();
        }

        int idx = Math.max(0, Math.min(tierIndex, PlushBlockEntity.MAX_TIER - 1));
        String key = TIER_KEY_PREFIX + (idx + 1);

        if (ROOT.tierLocks == null) {
            return 0;
        }
        return ROOT.tierLocks.getOrDefault(key, 0);
    }

    /**
     * Gets the specific item required to unlock a tier.
     *
     * @param tierIndex the tier index (0-based)
     * @return the unlock requirement item, or null if none
     */
    public static PlushItemEntry getUnlockRequirementForTier(int tierIndex) {
        PlushTierConfig tier = getTierConfig(tierIndex);
        return tier.unlockRequirement;
    }

    // ==================== Custom Methods ====================

    /**
     * Loads the configuration from disk or creates a default configuration if none exists.
     */
    public static void load() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path path = configDir.resolve(FILE_NAME);

        if (Files.notExists(path)) {
            ROOT = createDefaultConfig();
            save(path, ROOT);
            TetoMod.LOGGER.info("Created default plush tiers config at {}", path.toAbsolutePath());
        } else {
            try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                ROOT = GSON.fromJson(reader, PlushConfigRoot.class);
            } catch (IOException | JsonParseException e) {
                TetoMod.LOGGER.error("Failed to read plush tiers config, using defaults", e);
                ROOT = createDefaultConfig();
            }
        }

        if (ROOT == null) {
            ROOT = createDefaultConfig();
        }
    }

    /**
     * Saves the configuration to the specified path.
     *
     * @param path the path to save the configuration to
     * @param root the configuration root to save
     */
    private static void save(Path path, PlushConfigRoot root) {
        try (Writer writer = new BufferedWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            TetoMod.LOGGER.error("Failed to write plush tiers config to {}", path.toAbsolutePath(), e);
        }
    }

    /**
     * Creates a default configuration with five tiers of increasing difficulty.
     *
     * @return the default configuration root
     */
    private static PlushConfigRoot createDefaultConfig() {
        PlushConfigRoot root = new PlushConfigRoot();

        root.tiers.put("t1", createTier1Config());
        root.tiers.put("t2", createTier2Config());
        root.tiers.put("t3", createTier3Config());
        root.tiers.put("t4", createTier4Config());
        root.tiers.put("t5", createTier5Config());

        root.tierLocks.put("t1", TIER_1_UNLOCK);
        root.tierLocks.put("t2", TIER_2_UNLOCK);
        root.tierLocks.put("t3", TIER_3_UNLOCK);
        root.tierLocks.put("t4", TIER_4_UNLOCK);
        root.tierLocks.put("t5", TIER_5_UNLOCK);

        return root;
    }

    private static PlushTierConfig createTier1Config() {
        PlushTierConfig tier = new PlushTierConfig();
        tier.itemsToGive.add(new PlushItemEntry("minecraft:dirt", 16, 3));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:cobblestone", 16, 3));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:oak_log", 8, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:wheat_seeds", 16, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:apple", 4, 1));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:raw_copper", 12, 1));

        tier.itemsToReceive.add(new PlushItemEntry("minecraft:coal", 8, 3));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:iron_ingot", 3, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:bread", 4, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:diamond", 1, 1));

        tier.unlockRequirement = null;

        return tier;
    }

    private static PlushTierConfig createTier2Config() {
        PlushTierConfig tier = new PlushTierConfig();
        tier.itemsToGive.add(new PlushItemEntry("minecraft:iron_ingot", 4, 3));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:raw_iron", 8, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:redstone", 16, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:lapis_lazuli", 12, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:copper_ingot", 8, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:gold_ingot", 4, 1));

        tier.itemsToReceive.add(new PlushItemEntry("minecraft:emerald", 1, 3));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:gold_ingot", 3, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:experience_bottle", 4, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:diamond", 1, 1));

        tier.unlockRequirement = new PlushItemEntry("minecraft:emerald", 1, 1);

        return tier;
    }

    private static PlushTierConfig createTier3Config() {
        PlushTierConfig tier = new PlushTierConfig();
        tier.itemsToGive.add(new PlushItemEntry("minecraft:gold_ingot", 6, 3));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:emerald", 2, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:diamond", 2, 1));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:lapis_block", 2, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:redstone_block", 2, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:amethyst_shard", 8, 2));

        tier.itemsToReceive.add(new PlushItemEntry("minecraft:diamond", 2, 3));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:emerald", 3, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:golden_apple", 1, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:enchanted_book", 1, 1));

        tier.unlockRequirement = new PlushItemEntry("minecraft:diamond", 4, 1);

        return tier;
    }

    private static PlushTierConfig createTier4Config() {
        PlushTierConfig tier = new PlushTierConfig();
        tier.itemsToGive.add(new PlushItemEntry("minecraft:diamond", 4, 3));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:emerald", 4, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:netherite_scrap", 1, 1));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:blaze_rod", 4, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:ender_pearl", 4, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:obsidian", 8, 2));

        tier.itemsToReceive.add(new PlushItemEntry("minecraft:netherite_scrap", 2, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:diamond_block", 1, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:shulker_shell", 2, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:enchanted_golden_apple", 1, 1));

        tier.unlockRequirement = new PlushItemEntry("minecraft:netherite_scrap", 4, 1);

        return tier;
    }

    private static PlushTierConfig createTier5Config() {
        PlushTierConfig tier = new PlushTierConfig();
        tier.itemsToGive.add(new PlushItemEntry("minecraft:netherite_ingot", 1, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:ancient_debris", 2, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:dragon_breath", 4, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:echo_shard", 4, 2));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:wither_skeleton_skull", 1, 1));
        tier.itemsToGive.add(new PlushItemEntry("minecraft:end_crystal", 1, 1));

        tier.itemsToReceive.add(new PlushItemEntry("minecraft:nether_star", 1, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:beacon", 1, 1));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:netherite_ingot", 2, 2));
        tier.itemsToReceive.add(new PlushItemEntry("minecraft:dragon_egg", 1, 1));

        tier.unlockRequirement = new PlushItemEntry("minecraft:netherite_ingot", 1, 1);

        return tier;
    }
}
