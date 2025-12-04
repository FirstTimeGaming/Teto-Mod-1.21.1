package net.firsttimegaming.tetomod.config;

/**
 * Represents an item entry in the plush tier configuration.
 * <p>
 * Each entry specifies an item ID, count, and weight for weighted random selection.
 */
public class PlushItemEntry {

    /**
     * The registry ID of the item (e.g., "minecraft:diamond").
     */
    public String id;

    /**
     * The number of items in this entry.
     */
    public int count;

    /**
     * The weight for random selection. Higher weights increase selection probability.
     */
    public int weight;

    /**
     * Default constructor for GSON deserialization.
     */
    public PlushItemEntry() {
    }

    /**
     * Constructs a PlushItemEntry with the specified values.
     *
     * @param id     the item registry ID
     * @param count  the item count
     * @param weight the selection weight
     */
    public PlushItemEntry(String id, int count, int weight) {
        this.id = id;
        this.count = count;
        this.weight = weight;
    }
}
