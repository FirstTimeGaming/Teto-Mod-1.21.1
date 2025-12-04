package net.firsttimegaming.tetomod.block.entity;

import net.firsttimegaming.tetomod.config.PlushItemEntry;
import net.firsttimegaming.tetomod.config.PlushTierConfig;
import net.firsttimegaming.tetomod.config.PlushTierConfigManager;
import net.firsttimegaming.tetomod.screen.PlushMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlushBlockEntity extends BlockEntity implements MenuProvider {

    // how many tiers you have
    public static final int MAX_TIER = 5;

    private int selectedTier = 0; // 0-based or 1-based, your choice

    public int getSelectedTier() {
        return selectedTier;
    }

    // one cached reward per tier index (0..MAX_TIER-1)
    private final Map<Integer, PlushItemEntry> cachedRewards = new HashMap<>();

    // how many times each tier has been completed
    private final Map<Integer, Integer> tierCompletions = new HashMap<>();

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 64;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide())  {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public PlushBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.PLUSH_BLOCK_ENTITY.get(), pos, blockState);
    }

    public PlushTierConfig getCurrentTierConfig() {
        return PlushTierConfigManager.getTierConfig(this.selectedTier);
    }

    /** Roll a random "give" item from the current tier config and put it in slot 0. */
    public void rollRandomRewardIntoSlot0() {
        if (level == null || level.isClientSide()) return;

        PlushTierConfig tierConfig = getCurrentTierConfig();
        List<PlushItemEntry> pool = tierConfig.itemsToGive;
        if (pool == null || pool.isEmpty()) {
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            return;
        }

        // Compute total weight
        int totalWeight = 0;
        for (PlushItemEntry entry : pool) {
            int w = Math.max(1, entry.weight); // ensure at least 1
            totalWeight += w;
        }
        if (totalWeight <= 0) {
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            return;
        }

        // Roll a number in [0, totalWeight)
        int roll = level.random.nextInt(totalWeight);
        PlushItemEntry chosen = null;
        int cumulative = 0;
        for (PlushItemEntry entry : pool) {
            int w = Math.max(1, entry.weight);
            cumulative += w;
            if (roll < cumulative) {
                chosen = entry;
                break;
            }
        }

        if (chosen == null) {
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            return;
        }

        // Convert id → Item
        try {
            ResourceLocation rl = ResourceLocation.parse(chosen.id);
            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                int count = Math.max(1, chosen.count);
                inventory.setStackInSlot(0, new ItemStack(item, count));
            } else {
                inventory.setStackInSlot(0, ItemStack.EMPTY);
            }
        } catch (Exception e) {
            inventory.setStackInSlot(0, ItemStack.EMPTY);
        }
    }

    public void clearContents() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for (int i = 1; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    public void setSelectedTier(int tier) {
        tier = Math.max(0, Math.min(tier, MAX_TIER - 1));

        if (this.selectedTier != tier) {
            this.selectedTier = tier;

            if (level != null && !level.isClientSide()) {
                PlushItemEntry existingItem = cachedRewards.get(selectedTier);
                if (existingItem == null) {
                    // no cached reward yet, roll a new one
                    rollRandomRewardIntoSlot0();
                } else {
                    // use cached reward
                    ItemStack stack = toStack(existingItem);
                    inventory.setStackInSlot(0, stack);
                }
            }

            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }


    public void handleSubmit(Player player) {
        if (level == null || level.isClientSide()) {
            return;
        }

        ItemStack required = inventory.getStackInSlot(0); // what plush wants
        ItemStack offered  = inventory.getStackInSlot(1); // what player offered

        // nothing required or offered
        if (required.isEmpty() || offered.isEmpty()) {
            return;
        }

        // mismatched item (ignore count)
        if (!ItemStack.isSameItemSameComponents(required, offered)) {
            return;
        }

        int requiredCount = required.getCount();
        if (offered.getCount() < requiredCount) {
            // not enough
            return;
        }

        // --- consume required amount from slot 1 ---
        offered.shrink(requiredCount);
        if (offered.isEmpty()) {
            inventory.setStackInSlot(1, ItemStack.EMPTY);
        } else {
            inventory.setStackInSlot(1, offered);
        }

        // --- play XP sound ---
        level.playSound(
                null,
                worldPosition,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );

        // --- get random reward from itemsToGive for this tier ---
        ItemStack reward = getRandomRewardForCurrentTier();
        if (!reward.isEmpty()) {
            // Drop from the block entity position instead of giving to player
            double dropX = worldPosition.getX() + 0.5;
            double dropY = worldPosition.getY() + 1.0;   // slightly above the block
            double dropZ = worldPosition.getZ() + 0.5;

            Containers.dropItemStack(level, dropX, dropY, dropZ, reward.copy());
        }

        // --- increment times completed for this tier ---
        incrementTierCompletions(this.selectedTier);

        // --- reroll new requirement into slot 0 ---
        rollRandomRewardIntoSlot0();

        setChanged();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    public int getTierCompletions(int tierIndex) {
        return tierCompletions.getOrDefault(tierIndex, 0);
    }

    public void incrementTierCompletions(int tier) {
        if (tier >= 0 && tier < MAX_TIER) {
            tierCompletions.get(tier);
            int current = tierCompletions.getOrDefault(tier, 0);
            tierCompletions.put(tier, current + 1);
            setChanged();
        }
    }

    public boolean canUseTier(int tierIndex, @Nullable Player player) {
        // Clamp the tier index
        tierIndex = Math.max(0, Math.min(tierIndex, MAX_TIER - 1));

        int required = PlushTierConfigManager.getRequiredCompletionsForTier(tierIndex);

        // Tier 0 or anything with required <= 0 is always usable
        if (required <= 0 || tierIndex == 0) {
            return true;
        }

        // We defined locks in terms of completions of the *previous* tier
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

    public ItemStack getRandomRewardForCurrentTier() {
        PlushTierConfig tierCfg = PlushTierConfigManager.getTierConfig(this.selectedTier);
        PlushItemEntry entry = pickWeighted(tierCfg.itemsToReceive);
        return toStack(entry);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("SelectedTier", this.selectedTier);

        // Save cached rewards
        CompoundTag cacheTag = new CompoundTag();
        for (Map.Entry<Integer, PlushItemEntry> e : cachedRewards.entrySet()) {
            int tierIndex = e.getKey();
            PlushItemEntry entry = e.getValue();
            CompoundTag rt = new CompoundTag();
            rt.putString("id", entry.id);
            rt.putInt("count", entry.count);
            rt.putInt("weight", entry.weight);
            cacheTag.put("tier_" + tierIndex, rt);
        }
        tag.put("CachedRewards", cacheTag);

        // Save completions
        CompoundTag completedTag = new CompoundTag();
        for (Map.Entry<Integer, Integer> e : tierCompletions.entrySet()) {
            int tierIndex = e.getKey();
            int completed = e.getValue();
            completedTag.putInt("tier_" + tierIndex, completed);
        }
        tag.put("TierCompletions", completedTag);

    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));

        if (tag.contains("SelectedTier")) {
            this.selectedTier = tag.getInt("SelectedTier");
        }

        // Load cached rewards
        cachedRewards.clear();
        if (tag.contains("CachedRewards")) {
            CompoundTag cacheTag = tag.getCompound("CachedRewards");
            for (String key : cacheTag.getAllKeys()) {
                if (key.startsWith("tier_")) {
                    try {
                        int tierIndex = Integer.parseInt(key.substring("tier_".length()));
                        CompoundTag rt = cacheTag.getCompound(key);
                        String id = rt.getString("id");
                        int count = rt.getInt("count");
                        int weight = rt.contains("weight") ? rt.getInt("weight") : 1;
                        cachedRewards.put(tierIndex, new PlushItemEntry(id, count, weight));
                    } catch (NumberFormatException ignored) {}
                }
            }
        }

        // Load completions
        tierCompletions.clear();
        if (tag.contains("TierCompletions")) {
            CompoundTag completedTag = tag.getCompound("TierCompletions");
            for (String key : completedTag.getAllKeys()) {
                if (key.startsWith("tier_")) {
                    try {
                        int tierIndex = Integer.parseInt(key.substring("tier_".length()));
                        int completed = completedTag.getInt(key);
                        tierCompletions.put(tierIndex, completed);
                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    /** Convert a PlushItemEntry into an ItemStack (or EMPTY on error). */
    private ItemStack toStack(PlushItemEntry entry) {
        if (entry == null) return ItemStack.EMPTY;
        try {
            ResourceLocation rl = ResourceLocation.parse(entry.id);
            Item item = BuiltInRegistries.ITEM.get(rl);
            if (item != null && item != net.minecraft.world.item.Items.AIR) {
                int count = Math.max(1, entry.count);
                return new ItemStack(item, count);
            }
        } catch (Exception ignored) {}
        return ItemStack.EMPTY;
    }

    /** Get how many times the given tier (0-based) has been completed for this block. */
    public int getTimesCompleted(int tierIndex) {
        return tierCompletions.getOrDefault(tierIndex, 0);
    }

    /** Get how many times the currently selected tier has been completed. */
    public int getTimesCompletedForCurrentTier() {
        return getTimesCompleted(this.selectedTier);
    }

    @Nullable
    private PlushItemEntry pickWeighted(java.util.List<PlushItemEntry> list) {
        if (list == null || list.isEmpty()) return null;
        RandomSource random = (level != null ? level.random : RandomSource.create());

        int totalWeight = 0;
        for (PlushItemEntry e : list) {
            if (e != null && e.weight > 0) {
                totalWeight += e.weight;
            }
        }
        if (totalWeight <= 0) return null;

        int roll = random.nextInt(totalWeight); // [0, totalWeight)
        int accum = 0;
        for (PlushItemEntry e : list) {
            if (e == null || e.weight <= 0) continue;
            accum += e.weight;
            if (roll < accum) {
                return e;
            }
        }
        return null; // shouldn't happen
    }



    /**
     * Call this when the current tier is "completed":
     * - increments completion count
     * - clears the cached reward for this tier
     * - next usage of this tier will roll a new reward
     */
    public void markCurrentTierCompleted() {
        int current = tierCompletions.getOrDefault(selectedTier, 0);
        tierCompletions.put(selectedTier, current + 1);

        // Clear cached reward so next time we need a fresh roll
        cachedRewards.remove(selectedTier);

        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    private PlushItemEntry rollRandomRewardForCurrentTier() {
        if (level == null) return null;

        PlushTierConfig tierConfig = getCurrentTierConfig();
        List<PlushItemEntry> pool = tierConfig.itemsToGive;
        if (pool == null || pool.isEmpty()) return null;

        int totalWeight = 0;
        for (PlushItemEntry e : pool) {
            int w = Math.max(1, e.weight);
            totalWeight += w;
        }
        if (totalWeight <= 0) return null;

        int roll = level.random.nextInt(totalWeight);
        int cumulative = 0;
        for (PlushItemEntry e : pool) {
            int w = Math.max(1, e.weight);
            cumulative += w;
            if (roll < cumulative) {
                // store the *full* entry so we can reconstruct the same stack later
                return new PlushItemEntry(e.id, e.count, e.weight);
            }
        }
        return null;
    }

    public void ensureRewardForCurrentTier() {
        if (level == null || level.isClientSide()) return;

        PlushItemEntry cached = cachedRewards.get(selectedTier);
        if (cached == null) {
            cached = rollRandomRewardForCurrentTier();
            if (cached != null) {
                cachedRewards.put(selectedTier, cached);
                setChanged();
            }
        }

        ItemStack stack = toStack(cached);
        inventory.setStackInSlot(0, stack);

        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }




    @Override
    public Component getDisplayName() {
        return Component.literal("Teto");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        if (level != null && !level.isClientSide()) {
            // Only generate a reward if we don't already have one cached for this tier
            ensureRewardForCurrentTier();
        }
        return new PlushMenu(i, inventory, this);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        // sends the BE data to the client
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // what gets sent to the client when the chunk is loaded / updated
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        // how the client applies that data
        loadAdditional(tag, registries);
    }
}
