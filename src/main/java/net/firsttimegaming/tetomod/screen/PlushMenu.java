package net.firsttimegaming.tetomod.screen;

import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.block.ModBlocks;
import net.firsttimegaming.tetomod.block.entity.PlushBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * Container menu for the Plush block interface.
 * <p>
 * Handles the interaction between the player inventory and the plush block's
 * inventory slots for the tiered trading system.
 */
public class PlushMenu extends AbstractContainerMenu {

    // ==================== Class Variables ====================

    /** Button ID for rerolling the required item. */
    private static final int BUTTON_REROLL = 0;

    /** Button ID for submitting items. */
    private static final int BUTTON_SUBMIT = 1;

    /** Button ID for upgrading the plush tier. */
    private static final int BUTTON_UPGRADE = 2;

    /** Starting button ID for tier selection (tier 0 = 10, tier 1 = 11, etc.). */
    private static final int BUTTON_TIER_BASE = 10;

    /** Maximum button ID for tier selection (exclusive). */
    private static final int BUTTON_TIER_MAX = 15;

    /** X position for the requirement slot in the GUI. */
    private static final int REQUIREMENT_SLOT_X = 10;

    /** Y position for the requirement slot in the GUI. */
    private static final int REQUIREMENT_SLOT_Y = 26;

    /** X position for the submit slot in the GUI. */
    private static final int SUBMIT_SLOT_X = 81;

    /** Y position for the submit slot in the GUI. */
    private static final int SUBMIT_SLOT_Y = 130;

    /** X position for the upgrade slot in the GUI. */
    private static final int UPGRADE_SLOT_X = 9;

    /** Y position for the upgrade slot in the GUI. */
    private static final int UPGRADE_SLOT_Y = 130;

    /** Number of slots in the player hotbar. */
    private static final int HOTBAR_SLOT_COUNT = 9;

    /** Number of rows in the player inventory (excluding hotbar). */
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;

    /** Number of columns in the player inventory. */
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;

    /** Total number of slots in the player main inventory. */
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;

    /** Total number of vanilla player slots (hotbar + main inventory). */
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;

    /** First slot index for vanilla inventory in the container. */
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;

    /** First slot index for block entity inventory in the container. */
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    /** Number of slots in the block entity inventory. */
    private static final int TE_INVENTORY_SLOT_COUNT = 2;

    /** Starting X position for inventory slots. */
    private static final int INVENTORY_START_X = 9;

    /** Slot size in pixels. */
    private static final int SLOT_SIZE = 18;

    /** Y offset for player inventory from the top of the GUI. */
    public static final int INVENTORY_OFFSET_Y = 16 * 3 + 28;

    /** Y position for the first row of player inventory. */
    private static final int PLAYER_INVENTORY_START_Y = 84 + INVENTORY_OFFSET_Y;

    /** Y position for the player hotbar. */
    private static final int PLAYER_HOTBAR_Y = 142 + INVENTORY_OFFSET_Y;

    /** The block entity this menu is connected to. */
    public final PlushBlockEntity blockEntity;

    /** The level (world) this menu is in. */
    public final Level level;

    /** Data slot for synchronizing the selected tier between server and client. */
    private final DataSlot selectedTierData = new DataSlot() {
        @Override
        public int get() {
            // SERVER -> send to client
            return blockEntity.getSelectedTier();
        }

        @Override
        public void set(int value) {
            // CLIENT -> receive updated value from server
            blockEntity.clientSetSelectedTier(value);
        }
    };

    // ==================== Constructors ====================

    /**
     * Constructs a PlushMenu from network data.
     * Used when opening the menu on the client side.
     *
     * @param containerId the container ID
     * @param inv         the player inventory
     * @param extraData   the network buffer containing the block position
     */
    public PlushMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));

        TetoMod.LOGGER.info(
                "[CLIENT-PlushMenu.ctor-BUF] containerId={} pos={} beTier={}",
                containerId,
                this.blockEntity.getBlockPos(),
                this.blockEntity.getSelectedTier()
        );
    }

    /**
     * Constructs a PlushMenu with the given block entity.
     *
     * @param containerId the container ID
     * @param inv         the player inventory
     * @param blockEntity the plush block entity
     */
    public PlushMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ModMenuTypes.PLUSH_MENU.get(), containerId);
        this.blockEntity = (PlushBlockEntity) blockEntity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, PlushBlockEntity.SLOT_REQUIREMENT, REQUIREMENT_SLOT_X, REQUIREMENT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }

            @Override
            public boolean mayPickup(Player playerIn) {
                return false;
            }
        });

        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, PlushBlockEntity.SLOT_SUBMIT, SUBMIT_SLOT_X, SUBMIT_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }

            @Override
            public boolean mayPickup(Player playerIn) {
                return true;
            }
        });

        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, PlushBlockEntity.SLOT_UPGRADE, UPGRADE_SLOT_X, UPGRADE_SLOT_Y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true;
            }

            @Override
            public boolean mayPickup(Player playerIn) {
                return true;
            }
        });

        this.addDataSlot(selectedTierData);

        TetoMod.LOGGER.info(
                "[{}-PlushMenu.ctor-BE] containerId={} pos={} beTier={}",
                inv.player.level().isClientSide() ? "CLIENT" : "SERVER",
                containerId,
                this.blockEntity.getBlockPos(),
                this.blockEntity.getSelectedTier()
        );
    }

    // ==================== Custom Methods ====================

    /**
     * Adds the player's main inventory slots to the container.
     *
     * @param playerInventory the player inventory
     */
    private void addPlayerInventory(Inventory playerInventory) {
        for (int row = 0; row < PLAYER_INVENTORY_ROW_COUNT; row++) {
            for (int col = 0; col < PLAYER_INVENTORY_COLUMN_COUNT; col++) {
                int slotIndex = col + row * PLAYER_INVENTORY_COLUMN_COUNT + HOTBAR_SLOT_COUNT;
                int x = INVENTORY_START_X + col * SLOT_SIZE;
                int y = PLAYER_INVENTORY_START_Y + row * SLOT_SIZE;
                this.addSlot(new Slot(playerInventory, slotIndex, x, y));
            }
        }
    }

    /**
     * Adds the player's hotbar slots to the container.
     *
     * @param playerInventory the player inventory
     */
    private void addPlayerHotbar(Inventory playerInventory) {
        for (int col = 0; col < HOTBAR_SLOT_COUNT; col++) {
            int x = INVENTORY_START_X + col * SLOT_SIZE;
            this.addSlot(new Slot(playerInventory, col, x, PLAYER_HOTBAR_Y));
        }
    }

    // ==================== Overridden Methods ====================

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BUTTON_REROLL) {
            this.blockEntity.tryReroll(player);
            return true;
        }

        if (id == BUTTON_SUBMIT) {
            this.blockEntity.handleSubmit(player);
            return true;
        }

        if (id == BUTTON_UPGRADE) {
            this.blockEntity.handleUpgrade(player);
            return true;
        }

        if (id >= BUTTON_TIER_BASE && id < BUTTON_TIER_MAX) {
            int tier = id - BUTTON_TIER_BASE;

            if (!this.blockEntity.canUseTier(tier, player)) {
                return true;
            }
            this.blockEntity.setSelectedTier(tier);
            this.blockEntity.setChanged();
            return true;
        }

        return false;
    }

    public int getSelectedTier() {
        return selectedTierData.get();
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            int submitFirst = TE_INVENTORY_FIRST_SLOT_INDEX + 1;
            int submitLastExclusive = TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT;

            if (!moveItemStackTo(sourceStack, submitFirst, submitLastExclusive, false)) {
                return ItemStack.EMPTY;
            }
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack,
                    VANILLA_FIRST_SLOT_INDEX,
                    VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else {
            TetoMod.LOGGER.debug("Unexpected slot index in quickMoveStack: {}", pIndex);
            return ItemStack.EMPTY;
        }

        if (sourceStack.getCount() == 0) {
            sourceSlot.set(ItemStack.EMPTY);
        } else {
            sourceSlot.setChanged();
        }
        sourceSlot.onTake(playerIn, sourceStack);
        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), player, ModBlocks.TETO_BLOCK.get());
    }
}
