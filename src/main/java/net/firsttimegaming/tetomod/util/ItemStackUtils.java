package net.firsttimegaming.tetomod.util;

import net.firsttimegaming.tetomod.config.PlushItemEntry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Utility class for common ItemStack operations used throughout the mod.
 * Provides helper methods for converting configuration entries to ItemStack objects.
 */
public final class ItemStackUtils {

    private ItemStackUtils() {
        // Utility class - prevent instantiation
    }

    /**
     * Converts a {@link PlushItemEntry} configuration object into an {@link ItemStack}.
     * <p>
     * This method safely handles invalid or missing item IDs by returning an empty stack.
     * The count is clamped to a minimum of 1.
     *
     * @param entry the configuration entry containing item ID and count, may be null
     * @return the corresponding ItemStack, or {@link ItemStack#EMPTY} if the entry is invalid
     */
    public static ItemStack toStack(PlushItemEntry entry) {
        if (entry == null || entry.id == null || entry.id.isEmpty()) {
            return ItemStack.EMPTY;
        }

        try {
            ResourceLocation resourceLocation = ResourceLocation.parse(entry.id);
            Item item = BuiltInRegistries.ITEM.get(resourceLocation);

            if (item == null || item == Items.AIR) {
                return ItemStack.EMPTY;
            }

            int count = Math.max(1, entry.count);
            return new ItemStack(item, count);
        } catch (Exception e) {
            return ItemStack.EMPTY;
        }
    }

    /**
     * Checks if the given item ID is valid and corresponds to a registered item.
     *
     * @param itemId the item ID to validate (e.g., "minecraft:diamond")
     * @return true if the item ID is valid and not air, false otherwise
     */
    public static boolean isValidItemId(String itemId) {
        if (itemId == null || itemId.isEmpty()) {
            return false;
        }

        try {
            ResourceLocation resourceLocation = ResourceLocation.parse(itemId);
            Item item = BuiltInRegistries.ITEM.get(resourceLocation);
            return item != null && item != Items.AIR;
        } catch (Exception e) {
            return false;
        }
    }
}
