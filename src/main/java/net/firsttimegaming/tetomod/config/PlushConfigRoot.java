package net.firsttimegaming.tetomod.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Root configuration object for plush tier settings.
 * <p>
 * Contains all tier configurations and their unlock requirements.
 * This class is serialized/deserialized to JSON for persistent storage.
 */
public class PlushConfigRoot {

    /**
     * Map of tier configurations, keyed by tier identifier (e.g., "t1", "t2", etc.).
     */
    public Map<String, PlushTierConfig> tiers = new HashMap<>();

    /**
     * Map of tier unlock requirements, keyed by tier identifier.
     * Values represent the number of completions of the previous tier required to unlock.
     */
    public Map<String, Integer> tierLocks = new HashMap<>();

    /**
     * Default constructor for GSON deserialization.
     */
    public PlushConfigRoot() {
    }
}
