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
import java.util.Map;

public final class PlushTierConfigManager {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "tetomod_plush_tiers.json";

    private static PlushConfigRoot ROOT;

    private PlushTierConfigManager() {}

    public static void load() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        Path path = configDir.resolve(FILE_NAME);

        if (Files.notExists(path)) {
            // write a default config
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

    private static void save(Path path, PlushConfigRoot root) {
        try (Writer writer = new BufferedWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            TetoMod.LOGGER.error("Failed to write plush tiers config to {}", path.toAbsolutePath(), e);
        }
    }

    private static PlushConfigRoot createDefaultConfig() {
        PlushConfigRoot root = new PlushConfigRoot();

        // --- Tier 1 ---
        PlushTierConfig t1 = new PlushTierConfig();
        // player gives (cheap / early-game stuff)
        t1.itemsToGive.add(new PlushItemEntry("minecraft:dirt",          16, 3));
        t1.itemsToGive.add(new PlushItemEntry("minecraft:cobblestone",   16, 3));
        t1.itemsToGive.add(new PlushItemEntry("minecraft:oak_log",        8, 2));
        t1.itemsToGive.add(new PlushItemEntry("minecraft:wheat_seeds",   16, 2));
        t1.itemsToGive.add(new PlushItemEntry("minecraft:apple",          4, 1));
        t1.itemsToGive.add(new PlushItemEntry("minecraft:raw_copper",    12, 1));

        // player receives
        t1.itemsToReceive.add(new PlushItemEntry("minecraft:coal",        8, 3));
        t1.itemsToReceive.add(new PlushItemEntry("minecraft:iron_ingot",  3, 2));
        t1.itemsToReceive.add(new PlushItemEntry("minecraft:bread",       4, 2));
        t1.itemsToReceive.add(new PlushItemEntry("minecraft:diamond",     1, 1));

        // --- Tier 2 ---
        PlushTierConfig t2 = new PlushTierConfig();
        t2.itemsToGive.add(new PlushItemEntry("minecraft:iron_ingot",     4, 3));
        t2.itemsToGive.add(new PlushItemEntry("minecraft:raw_iron",       8, 2));
        t2.itemsToGive.add(new PlushItemEntry("minecraft:redstone",      16, 2));
        t2.itemsToGive.add(new PlushItemEntry("minecraft:lapis_lazuli",  12, 2));
        t2.itemsToGive.add(new PlushItemEntry("minecraft:copper_ingot",   8, 2));
        t2.itemsToGive.add(new PlushItemEntry("minecraft:gold_ingot",     4, 1));

        t2.itemsToReceive.add(new PlushItemEntry("minecraft:emerald",          1, 3));
        t2.itemsToReceive.add(new PlushItemEntry("minecraft:gold_ingot",       3, 2));
        t2.itemsToReceive.add(new PlushItemEntry("minecraft:experience_bottle",4, 2));
        t2.itemsToReceive.add(new PlushItemEntry("minecraft:diamond",          1, 1));

        // --- Tier 3 ---
        PlushTierConfig t3 = new PlushTierConfig();
        t3.itemsToGive.add(new PlushItemEntry("minecraft:gold_ingot",      6, 3));
        t3.itemsToGive.add(new PlushItemEntry("minecraft:emerald",         2, 2));
        t3.itemsToGive.add(new PlushItemEntry("minecraft:diamond",         2, 1));
        t3.itemsToGive.add(new PlushItemEntry("minecraft:lapis_block",     2, 2));
        t3.itemsToGive.add(new PlushItemEntry("minecraft:redstone_block",  2, 2));
        t3.itemsToGive.add(new PlushItemEntry("minecraft:amethyst_shard",  8, 2));

        t3.itemsToReceive.add(new PlushItemEntry("minecraft:diamond",          2, 3));
        t3.itemsToReceive.add(new PlushItemEntry("minecraft:emerald",          3, 2));
        t3.itemsToReceive.add(new PlushItemEntry("minecraft:golden_apple",     1, 2));
        t3.itemsToReceive.add(new PlushItemEntry("minecraft:enchanted_book",   1, 1));

        // --- Tier 4 ---
        PlushTierConfig t4 = new PlushTierConfig();
        t4.itemsToGive.add(new PlushItemEntry("minecraft:diamond",         4, 3));
        t4.itemsToGive.add(new PlushItemEntry("minecraft:emerald",         4, 2));
        t4.itemsToGive.add(new PlushItemEntry("minecraft:netherite_scrap",1, 1));
        t4.itemsToGive.add(new PlushItemEntry("minecraft:blaze_rod",       4, 2));
        t4.itemsToGive.add(new PlushItemEntry("minecraft:ender_pearl",     4, 2));
        t4.itemsToGive.add(new PlushItemEntry("minecraft:obsidian",        8, 2));

        t4.itemsToReceive.add(new PlushItemEntry("minecraft:netherite_scrap",        2, 2));
        t4.itemsToReceive.add(new PlushItemEntry("minecraft:diamond_block",          1, 2));
        t4.itemsToReceive.add(new PlushItemEntry("minecraft:shulker_shell",          2, 2));
        t4.itemsToReceive.add(new PlushItemEntry("minecraft:enchanted_golden_apple", 1, 1));

        // --- Tier 5 ---
        PlushTierConfig t5 = new PlushTierConfig();
        t5.itemsToGive.add(new PlushItemEntry("minecraft:netherite_ingot",   1, 2));
        t5.itemsToGive.add(new PlushItemEntry("minecraft:ancient_debris",   2, 2));
        t5.itemsToGive.add(new PlushItemEntry("minecraft:dragon_breath",    4, 2));
        t5.itemsToGive.add(new PlushItemEntry("minecraft:echo_shard",       4, 2));
        t5.itemsToGive.add(new PlushItemEntry("minecraft:wither_skeleton_skull", 1, 1));
        t5.itemsToGive.add(new PlushItemEntry("minecraft:end_crystal",      1, 1));

        t5.itemsToReceive.add(new PlushItemEntry("minecraft:nether_star",   1, 2));
        t5.itemsToReceive.add(new PlushItemEntry("minecraft:beacon",        1, 1));
        t5.itemsToReceive.add(new PlushItemEntry("minecraft:netherite_ingot",2, 2));
        t5.itemsToReceive.add(new PlushItemEntry("minecraft:dragon_egg",    1, 1));

        root.tiers.put("t1", t1);
        root.tiers.put("t2", t2);
        root.tiers.put("t3", t3);
        root.tiers.put("t4", t4);
        root.tiers.put("t5", t5);

        root.tierLocks.put("t1", 0);   // Tier 1 always available
        root.tierLocks.put("t2", 5);   // need 5 completions of Tier 1
        root.tierLocks.put("t3", 10);  // need 10 completions of Tier 2
        root.tierLocks.put("t4", 15);  // need 15 completions of Tier 3
        root.tierLocks.put("t5", 20);  // need 20 completions of Tier 4

        return root;
    }


    /** Get the full root config (if you ever need it). */
    public static PlushConfigRoot getRoot() {
        if (ROOT == null) {
            load();
        }
        return ROOT;
    }

    /** Helper: get config for a given tier index (0-based: 0->t1, 1->t2, etc.). */
    public static PlushTierConfig getTierConfig(int tierIndex) {
        if (ROOT == null) {
            load();
        }
        // clamp
        int idx = Math.max(0, Math.min(tierIndex, PlushBlockEntity.MAX_TIER - 1));
        String key = "t" + (idx + 1);

        PlushTierConfig tier = ROOT.tiers.get(key);
        if (tier == null) {
            // lazily create an empty tier if missing
            tier = new PlushTierConfig();
            ROOT.tiers.put(key, tier);
        }
        return tier;
    }

    public static int getRequiredCompletionsForTier(int tierIndex) {
        if (ROOT == null) {
            load();
        }
        int idx = Math.max(0, Math.min(tierIndex, PlushBlockEntity.MAX_TIER - 1));
        String key = "t" + (idx + 1);
        if (ROOT.tierLocks == null) {
            return 0;
        }
        return ROOT.tierLocks.getOrDefault(key, 0);
    }

}
