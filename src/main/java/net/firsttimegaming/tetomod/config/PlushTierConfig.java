package net.firsttimegaming.tetomod.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for a single tier in the plush trading system.
 * <p>
 * Each tier defines:
 * <ul>
 *   <li>Items the player must give (requirements)</li>
 *   <li>Items the player may receive (rewards)</li>
 * </ul>
 */
public class PlushTierConfig {

    /**
     * List of items the player must provide to complete this tier.
     * A random item from this list is selected as the requirement.
     */
    public List<PlushItemEntry> itemsToGive = new ArrayList<>();

    /**
     * List of items the player may receive as rewards for completing this tier.
     * A random item from this list is given based on weighted selection.
     */
    public List<PlushItemEntry> itemsToReceive = new ArrayList<>();

    /**
     * Default constructor for GSON deserialization.
     */
    public PlushTierConfig() {
    }
}
