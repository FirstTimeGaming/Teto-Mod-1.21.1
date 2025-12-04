package net.firsttimegaming.tetomod.util;

import net.firsttimegaming.tetomod.config.PlushItemEntry;
import net.minecraft.util.RandomSource;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Utility class for weighted random selection operations.
 * Provides helper methods for selecting items from weighted pools.
 */
public final class WeightedRandomUtils {

    private WeightedRandomUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Selects a random {@link PlushItemEntry} from a weighted list.
     * <p>
     * Items with higher weights have a proportionally higher chance of being selected.
     * Items with weight <= 0 are excluded from selection.
     *
     * @param list   the list of entries to select from, may be null or empty
     * @param random the random source to use for selection
     * @return a randomly selected entry, or null if the list is empty or all weights are non-positive
     */
    @Nullable
    public static PlushItemEntry pickWeighted(List<PlushItemEntry> list, RandomSource random) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        int totalWeight = calculateTotalWeight(list);
        if (totalWeight <= 0) {
            return null;
        }

        int roll = random.nextInt(totalWeight);
        int accumulator = 0;

        for (PlushItemEntry entry : list) {
            if (entry == null || entry.weight <= 0) {
                continue;
            }

            accumulator += entry.weight;
            if (roll < accumulator) {
                return entry;
            }
        }

        return null;
    }

    /**
     * Calculates the total weight of all entries in the list.
     * Entries with null values or weight <= 0 are excluded.
     *
     * @param list the list of entries to calculate weight for
     * @return the total weight, or 0 if the list is empty or all weights are non-positive
     */
    public static int calculateTotalWeight(List<PlushItemEntry> list) {
        if (list == null || list.isEmpty()) {
            return 0;
        }

        int totalWeight = 0;
        for (PlushItemEntry entry : list) {
            if (entry != null && entry.weight > 0) {
                totalWeight += entry.weight;
            }
        }
        return totalWeight;
    }

    /**
     * Creates a copy of the given {@link PlushItemEntry}.
     *
     * @param entry the entry to copy, may be null
     * @return a new PlushItemEntry with the same values, or null if the input is null
     */
    @Nullable
    public static PlushItemEntry copyEntry(PlushItemEntry entry) {
        if (entry == null) {
            return null;
        }
        return new PlushItemEntry(entry.id, entry.count, entry.weight);
    }
}
