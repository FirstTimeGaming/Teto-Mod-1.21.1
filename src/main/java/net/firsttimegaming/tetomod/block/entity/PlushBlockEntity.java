package net.firsttimegaming.tetomod.block.entity;

import net.firsttimegaming.tetomod.config.PlushItemEntry;
import net.firsttimegaming.tetomod.config.PlushTierConfig;
import net.firsttimegaming.tetomod.config.PlushTierConfigManager;
import net.firsttimegaming.tetomod.screen.PlushMenu;
import net.firsttimegaming.tetomod.util.ItemStackUtils;
import net.firsttimegaming.tetomod.util.WeightedRandomUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Block entity for the Plush block that manages tiered item trading.
 * <p>
 * The plush block allows players to submit required items and receive random rewards
 * based on the currently selected tier. Each tier has its own pool of required items
 * and potential rewards with configurable weights.
 * <p>
 * Tier progression is tracked per block entity, and higher tiers require completing
 * previous tiers a certain number of times before they become available.
 */
public class PlushBlockEntity extends BlockEntity implements MenuProvider {

    /** The maximum number of tiers available in the plush system (0-indexed: 0 to MAX_TIER-1). */
    public static final int MAX_TIER = 5;

    /** The number of inventory slots in the plush block. */
    public static final int INVENTORY_SIZE = 2;

    /** Slot index for the required item display. */
    public static final int SLOT_REQUIREMENT = 0;

    /** Slot index for player item submission. */
    public static final int SLOT_SUBMIT = 1;

    /** Default stack size limit for inventory slots. */
    private static final int DEFAULT_STACK_LIMIT = 64;

    /** Block update flags for synchronizing state to clients. */
    private static final int BLOCK_UPDATE_FLAGS = 3;

    /** NBT key for storing the inventory data. */
    private static final String NBT_INVENTORY = "inventory";

    /** NBT key for storing the selected tier. */
    private static final String NBT_SELECTED_TIER = "SelectedTier";

    /** NBT key for storing cached rewards. */
    private static final String NBT_CACHED_REWARDS = "CachedRewards";

    /** NBT key for storing tier completion counts. */
    private static final String NBT_TIER_COMPLETIONS = "TierCompletions";

    /** NBT key prefix for individual tier entries. */
    private static final String NBT_TIER_PREFIX = "tier_";

    /** NBT key for item ID in cached rewards. */
    private static final String NBT_ITEM_ID = "id";

    /** NBT key for item count in cached rewards. */
    private static final String NBT_ITEM_COUNT = "count";

    /** NBT key for item weight in cached rewards. */
    private static final String NBT_ITEM_WEIGHT = "weight";

    /** Sound volume for XP pickup sound. */
    private static final float SOUND_VOLUME = 1.0F;

    /** Sound pitch for XP pickup sound. */
    private static final float SOUND_PITCH = 1.0F;

    /** Offset for item drop position above the block. */
    private static final double DROP_Y_OFFSET = 1.0;

    /** Center offset for item drop position. */
    private static final double DROP_CENTER_OFFSET = 0.5;

    /** The currently selected tier index (0-based). */
    private int selectedTier = 0;

    /** Cached reward entries per tier to ensure consistency within a session. */
    private final Map<Integer, PlushItemEntry> cachedRewards = new HashMap<>();

    /** Completion count for each tier. */
    private final Map<Integer, Integer> tierCompletions = new HashMap<>();

    /** Inventory handler for the plush block's item slots. */
    public final ItemStackHandler inventory = new ItemStackHandler(INVENTORY_SIZE) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return DEFAULT_STACK_LIMIT;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), BLOCK_UPDATE_FLAGS);
            }
        }
    };

    /**
     * Constructs a new PlushBlockEntity at the specified position.
     *
     * @param pos        the block position
     * @param blockState the block state
     */
    public PlushBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PLUSH_BLOCK_ENTITY.get(), pos, blockState);
    }

    /**
     * Gets the currently selected tier index.
     *
     * @return the selected tier index (0-based)
     */
    public int getSelectedTier() {
        return selectedTier;
    }

    /**
     * Gets the configuration for the currently selected tier.
     *
     * @return the tier configuration containing items to give and receive
     */
    public PlushTierConfig getCurrentTierConfig() {
        return PlushTierConfigManager.getTierConfig(this.selectedTier);
    }

    /**
     * Rolls a random required item from the current tier's item pool and places it in the requirement slot.
     * This determines what item the player needs to submit for the current tier.
     */
    public void rollRandomRewardIntoSlot0() {
        if (level == null || level.isClientSide()) {
            return;
        }

        PlushTierConfig tierConfig = getCurrentTierConfig();
        List<PlushItemEntry> pool = tierConfig.itemsToGive;

        PlushItemEntry chosen = pickWeightedWithMinWeight(pool);
        ItemStack stack = ItemStackUtils.toStack(chosen);
        inventory.setStackInSlot(SLOT_REQUIREMENT, stack);
    }

    /**
     * Clears the requirement slot contents.
     */
    public void clearContents() {
        inventory.setStackInSlot(SLOT_REQUIREMENT, ItemStack.EMPTY);
    }

    /**
     * Drops all items from the submit slot when the block is broken.
     */
    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for (int i = SLOT_SUBMIT; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    /**
     * Sets the currently selected tier, clamping to valid range.
     * <p>
     * When changing tiers, this will either restore a cached reward for that tier
     * or roll a new one if none is cached.
     *
     * @param tier the tier index to select (will be clamped to 0 to MAX_TIER-1)
     */
    public void setSelectedTier(int tier) {
        tier = Math.max(0, Math.min(tier, MAX_TIER - 1));

        if (this.selectedTier != tier) {
            this.selectedTier = tier;

            if (level != null && !level.isClientSide()) {
                PlushItemEntry existingItem = cachedRewards.get(selectedTier);
                if (existingItem == null) {
                    rollRandomRewardIntoSlot0();
                } else {
                    ItemStack stack = ItemStackUtils.toStack(existingItem);
                    inventory.setStackInSlot(SLOT_REQUIREMENT, stack);
                }
            }

            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), BLOCK_UPDATE_FLAGS);
            }
        }
    }

    /**
     * Handles the submit action when a player confirms their item offering.
     * <p>
     * This method:
     * <ul>
     *   <li>Validates that the offered item matches the required item</li>
     *   <li>Consumes the required amount from the submit slot</li>
     *   <li>Plays a success sound</li>
     *   <li>Drops a random reward from the current tier's reward pool</li>
     *   <li>Increments the tier completion count</li>
     *   <li>Rolls a new required item</li>
     * </ul>
     *
     * @param player the player submitting the item
     */
    public void handleSubmit(Player player) {
        if (level == null || level.isClientSide()) {
            return;
        }

        ItemStack required = inventory.getStackInSlot(SLOT_REQUIREMENT);
        ItemStack offered = inventory.getStackInSlot(SLOT_SUBMIT);

        if (required.isEmpty() || offered.isEmpty()) {
            return;
        }

        if (!ItemStack.isSameItemSameComponents(required, offered)) {
            return;
        }

        int requiredCount = required.getCount();
        if (offered.getCount() < requiredCount) {
            return;
        }

        offered.shrink(requiredCount);
        if (offered.isEmpty()) {
            inventory.setStackInSlot(SLOT_SUBMIT, ItemStack.EMPTY);
        } else {
            inventory.setStackInSlot(SLOT_SUBMIT, offered);
        }

        level.playSound(
                null,
                worldPosition,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                SOUND_VOLUME,
                SOUND_PITCH
        );

        ItemStack reward = getRandomRewardForCurrentTier();
        if (!reward.isEmpty()) {
            double dropX = worldPosition.getX() + DROP_CENTER_OFFSET;
            double dropY = worldPosition.getY() + DROP_Y_OFFSET;
            double dropZ = worldPosition.getZ() + DROP_CENTER_OFFSET;

            Containers.dropItemStack(level, dropX, dropY, dropZ, reward.copy());
        }

        incrementTierCompletions(this.selectedTier);
        rollRandomRewardIntoSlot0();

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), BLOCK_UPDATE_FLAGS);
    }

    /**
     * Gets the number of times the specified tier has been completed.
     *
     * @param tierIndex the tier index (0-based)
     * @return the completion count for the tier
     */
    public int getTierCompletions(int tierIndex) {
        return tierCompletions.getOrDefault(tierIndex, 0);
    }

    /**
     * Increments the completion count for the specified tier.
     *
     * @param tier the tier index to increment (0-based)
     */
    public void incrementTierCompletions(int tier) {
        if (tier >= 0 && tier < MAX_TIER) {
            int current = tierCompletions.getOrDefault(tier, 0);
            tierCompletions.put(tier, current + 1);
            setChanged();
        }
    }

    /**
     * Checks if the player can use the specified tier.
     * <p>
     * Tiers are unlocked based on completing the previous tier a certain number of times.
     * Tier 0 (first tier) is always available.
     *
     * @param tierIndex the tier index to check (0-based)
     * @param player    the player attempting to use the tier, may be null
     * @return true if the tier is unlocked and usable, false otherwise
     */
    public boolean canUseTier(int tierIndex, @Nullable Player player) {
        tierIndex = Math.max(0, Math.min(tierIndex, MAX_TIER - 1));

        int required = PlushTierConfigManager.getRequiredCompletionsForTier(tierIndex);

        if (required <= 0 || tierIndex == 0) {
            return true;
        }

        int prevTierIndex = tierIndex - 1;
        int completedPrev = getTierCompletions(prevTierIndex);

        boolean unlocked = completedPrev >= required;

        if (!unlocked && player != null && !player.level().isClientSide()) {
            int shortfall = required - completedPrev;
            player.displayClientMessage(
                    Component.literal(
                            "Tier " + (tierIndex + 1) + " is locked. " +
                                    "Complete Tier " + tierIndex + " " + shortfall + " more time"
                                    + (shortfall == 1 ? "" : "s") + " to unlock."
                    ),
                    true
            );
        }

        return unlocked;
    }

    /**
     * Gets a random reward from the current tier's reward pool.
     *
     * @return the reward ItemStack, or empty if no valid reward could be selected
     */
    public ItemStack getRandomRewardForCurrentTier() {
        PlushTierConfig tierCfg = PlushTierConfigManager.getTierConfig(this.selectedTier);
        RandomSource random = (level != null ? level.random : RandomSource.create());
        PlushItemEntry entry = WeightedRandomUtils.pickWeighted(tierCfg.itemsToReceive, random);
        return ItemStackUtils.toStack(entry);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(NBT_INVENTORY, inventory.serializeNBT(registries));
        tag.putInt(NBT_SELECTED_TIER, this.selectedTier);

        CompoundTag cacheTag = new CompoundTag();
        for (Map.Entry<Integer, PlushItemEntry> e : cachedRewards.entrySet()) {
            int tierIdx = e.getKey();
            PlushItemEntry entry = e.getValue();
            CompoundTag rt = new CompoundTag();
            rt.putString(NBT_ITEM_ID, entry.id);
            rt.putInt(NBT_ITEM_COUNT, entry.count);
            rt.putInt(NBT_ITEM_WEIGHT, entry.weight);
            cacheTag.put(NBT_TIER_PREFIX + tierIdx, rt);
        }
        tag.put(NBT_CACHED_REWARDS, cacheTag);

        CompoundTag completedTag = new CompoundTag();
        for (Map.Entry<Integer, Integer> e : tierCompletions.entrySet()) {
            int tierIdx = e.getKey();
            int completed = e.getValue();
            completedTag.putInt(NBT_TIER_PREFIX + tierIdx, completed);
        }
        tag.put(NBT_TIER_COMPLETIONS, completedTag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound(NBT_INVENTORY));

        if (tag.contains(NBT_SELECTED_TIER)) {
            this.selectedTier = tag.getInt(NBT_SELECTED_TIER);
        }

        cachedRewards.clear();
        if (tag.contains(NBT_CACHED_REWARDS)) {
            CompoundTag cacheTag = tag.getCompound(NBT_CACHED_REWARDS);
            for (String key : cacheTag.getAllKeys()) {
                if (key.startsWith(NBT_TIER_PREFIX)) {
                    try {
                        int tierIdx = Integer.parseInt(key.substring(NBT_TIER_PREFIX.length()));
                        CompoundTag rt = cacheTag.getCompound(key);
                        String id = rt.getString(NBT_ITEM_ID);
                        int count = rt.getInt(NBT_ITEM_COUNT);
                        int weight = rt.contains(NBT_ITEM_WEIGHT) ? rt.getInt(NBT_ITEM_WEIGHT) : 1;
                        cachedRewards.put(tierIdx, new PlushItemEntry(id, count, weight));
                    } catch (NumberFormatException ignored) {
                        // Skip invalid entries
                    }
                }
            }
        }

        tierCompletions.clear();
        if (tag.contains(NBT_TIER_COMPLETIONS)) {
            CompoundTag completedTag = tag.getCompound(NBT_TIER_COMPLETIONS);
            for (String key : completedTag.getAllKeys()) {
                if (key.startsWith(NBT_TIER_PREFIX)) {
                    try {
                        int tierIdx = Integer.parseInt(key.substring(NBT_TIER_PREFIX.length()));
                        int completed = completedTag.getInt(key);
                        tierCompletions.put(tierIdx, completed);
                    } catch (NumberFormatException ignored) {
                        // Skip invalid entries
                    }
                }
            }
        }
    }

    /**
     * Gets how many times the specified tier has been completed for this block.
     *
     * @param tierIndex the tier index (0-based)
     * @return the number of completions
     */
    public int getTimesCompleted(int tierIndex) {
        return tierCompletions.getOrDefault(tierIndex, 0);
    }

    /**
     * Gets how many times the currently selected tier has been completed.
     *
     * @return the number of completions for the current tier
     */
    public int getTimesCompletedForCurrentTier() {
        return getTimesCompleted(this.selectedTier);
    }

    /**
     * Marks the current tier as completed.
     * <p>
     * This increments the completion count and clears the cached reward,
     * so the next usage of this tier will roll a new reward.
     */
    public void markCurrentTierCompleted() {
        int current = tierCompletions.getOrDefault(selectedTier, 0);
        tierCompletions.put(selectedTier, current + 1);

        cachedRewards.remove(selectedTier);

        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), BLOCK_UPDATE_FLAGS);
        }
    }

    /**
     * Rolls a random reward entry for the current tier using weighted selection.
     * Entries with weight <= 0 are treated as having weight 1.
     *
     * @return the selected entry, or null if no valid entry could be selected
     */
    @Nullable
    private PlushItemEntry rollRandomRewardForCurrentTier() {
        if (level == null) {
            return null;
        }

        PlushTierConfig tierConfig = getCurrentTierConfig();
        List<PlushItemEntry> pool = tierConfig.itemsToGive;

        PlushItemEntry selected = pickWeightedWithMinWeight(pool);
        return WeightedRandomUtils.copyEntry(selected);
    }

    /**
     * Ensures a reward is cached and displayed for the current tier.
     * If no cached reward exists, a new one is rolled.
     */
    public void ensureRewardForCurrentTier() {
        if (level == null || level.isClientSide()) {
            return;
        }

        PlushItemEntry cached = cachedRewards.get(selectedTier);
        if (cached == null) {
            cached = rollRandomRewardForCurrentTier();
            if (cached != null) {
                cachedRewards.put(selectedTier, cached);
                setChanged();
            }
        }

        ItemStack stack = ItemStackUtils.toStack(cached);
        inventory.setStackInSlot(SLOT_REQUIREMENT, stack);

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), BLOCK_UPDATE_FLAGS);
    }

    /**
     * Picks a weighted entry from the list, treating entries with weight <= 0 as weight 1.
     *
     * @param pool the list of entries to select from
     * @return the selected entry, or null if pool is empty
     */
    @Nullable
    private PlushItemEntry pickWeightedWithMinWeight(List<PlushItemEntry> pool) {
        if (pool == null || pool.isEmpty()) {
            return null;
        }

        int totalWeight = 0;
        for (PlushItemEntry entry : pool) {
            int w = Math.max(1, entry.weight);
            totalWeight += w;
        }

        if (totalWeight <= 0 || level == null) {
            return null;
        }

        int roll = level.random.nextInt(totalWeight);
        int cumulative = 0;
        for (PlushItemEntry entry : pool) {
            int w = Math.max(1, entry.weight);
            cumulative += w;
            if (roll < cumulative) {
                return entry;
            }
        }

        return null;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Teto");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (level != null && !level.isClientSide()) {
            ensureRewardForCurrentTier();
        }
        return new PlushMenu(i, inventory, this);
    }

    /**
     * Creates the packet for sending block entity data to the client.
     *
     * @return the update packet
     */
    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    /**
     * Creates the tag to send to the client when the chunk is loaded or updated.
     *
     * @param registries the registry lookup provider
     * @return the compound tag containing the block entity data
     */
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    /**
     * Handles applying the update tag received from the server.
     *
     * @param tag        the compound tag containing the block entity data
     * @param registries the registry lookup provider
     */
    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadAdditional(tag, registries);
    }
}
