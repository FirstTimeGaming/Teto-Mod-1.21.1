package net.firsttimegaming.tetomod.screen;

import net.firsttimegaming.tetomod.block.ModBlocks;
import net.firsttimegaming.tetomod.block.entity.PlushBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.SlotItemHandler;

import javax.annotation.Nullable;

public class PlushMenu extends AbstractContainerMenu {

    public final PlushBlockEntity blockEntity;
    public final Level level;

    public PlushMenu(int containerId, Inventory inv, FriendlyByteBuf extraData) {
        this(containerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()));
    }

    public PlushMenu(int containerId, Inventory inv, BlockEntity blockEntity) {
        super(ModMenuTypes.PLUSH_MENU.get(), containerId);
        this.blockEntity = (PlushBlockEntity) blockEntity;
        this.level = inv.player.level();

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, 0, 10, 26) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
//                return stack.getDisplayName().getString().contains("Teto");
            }

            public boolean mayPickup(Player playerIn) {
                return false;
            }
        });

        // Slot 1 - submit slot (center small slot under the big area)
        // tweak (x, y) if you want it perfectly aligned with your texture
        this.addSlot(new SlotItemHandler(this.blockEntity.inventory, 1, 81, 130) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return true; // allow putting stuff in to "submit"
            }

            @Override
            public boolean mayPickup(Player playerIn) {
                return true; // you can pick it back up if you change your mind
            }
        });
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == 0) {
            this.blockEntity.rollRandomRewardIntoSlot0();
            return true;
        }

        if (id == 1) {
            // Submit: e.g. consume submit slot item & complete tier
            this.blockEntity.handleSubmit(player);
            return true;
        }

        if (id >= 10 && id < 15) {
            int tier = id - 10;

            if (!this.blockEntity.canUseTier(tier, player)) {
                // Locked: do not change selection
                return true;
            }
            this.blockEntity.setSelectedTier(tier);
            this.blockEntity.setChanged();
            return true;
        }

        return false;
    }

    public static final int INVENTORY_OFFSET_Y = 16*3 + 28;


    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + 1 + j * 18, 84 + INVENTORY_OFFSET_Y + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; i++) {
            this.addSlot(new Slot(playerInventory, i, 8 + 1 + i * 18, 142 + INVENTORY_OFFSET_Y));
        }
    }

    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 2;  // must be the number of slots you have!
    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);
        if (sourceSlot == null || !sourceSlot.hasItem()) return ItemStack.EMPTY;
        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Clicked a vanilla (player) slot → move into TE inventory
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // Only allow quick-move into the *submit* slot (index 1),
            // not the requirement slot (index 0)
            int submitFirst = TE_INVENTORY_FIRST_SLOT_INDEX + 1;
            int submitLastExclusive = TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT;

            if (!moveItemStackTo(sourceStack, submitFirst, submitLastExclusive, false)) {
                return ItemStack.EMPTY;
            }

            // Clicked a TE slot → move back into player inventory (both BE slots allowed to move out)
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            if (!moveItemStackTo(sourceStack,
                    VANILLA_FIRST_SLOT_INDEX,
                    VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT,
                    false)) {
                return ItemStack.EMPTY;
            }
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);
            return ItemStack.EMPTY;
        }

        // finalize
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
