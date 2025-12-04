package net.firsttimegaming.tetomod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.block.entity.PlushBlockEntity;
import net.firsttimegaming.tetomod.client.widget.DropdownWidget;
import net.firsttimegaming.tetomod.config.PlushItemEntry;
import net.firsttimegaming.tetomod.config.PlushTierConfig;
import net.firsttimegaming.tetomod.config.PlushTierConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PlushScreen extends AbstractContainerScreen<PlushMenu> {

    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TetoMod.MOD_ID, "textures/gui/plush/place_holder_gui.png");

    private DropdownWidget tierDropdown;

    public PlushScreen(PlushMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        // these are correct for your tall 256x256 texture
        this.imageWidth = 175;
        this.imageHeight = 241;
    }

    @Override
    protected void init() {
        super.init();

        int left = this.leftPos;
        int top = this.topPos;

        // --- Dropdown right of the title ("Teto") ---
        String titleText = this.title.getString();
        int titleWidth = this.font.width(titleText);

        int dropdownX = left + 8 + titleWidth + 61;
        int dropdownY = top + 4;

        this.tierDropdown = new DropdownWidget(
                dropdownX,
                dropdownY,
                80,
                16,
                List.of(
                        Component.literal("Tier 1"),
                        Component.literal("Tier 2"),
                        Component.literal("Tier 3"),
                        Component.literal("Tier 4"),
                        Component.literal("Tier 5")
                ),
                this::onTierSelected
        );

        // 🔹 Set dropdown to the BE's current tier (clamped just in case)
        int beTier = this.menu.blockEntity.getSelectedTier();
        if (beTier < 0) beTier = 0;
        if (beTier >= 5) beTier = 4;
        this.tierDropdown.setSelectedIndex(beTier);

        // --- Buttons around the submit slot ---
        // submit slot in PlushMenu is at GUI coords (80, 72)
        int submitSlotGuiX = 81;
        int submitSlotGuiY = 130;
        int submitSlotX = left + submitSlotGuiX;
        int submitSlotY = top + submitSlotGuiY - 2;

        // Refresh button to the left
        this.addRenderableWidget(
                Button.builder(Component.literal("R"), b -> onRefreshClicked())
                        .bounds(submitSlotX - 24, submitSlotY, 20, 20)
                        .build()
        );

        // Submit button (checkmark) to the right
        this.addRenderableWidget(
                Button.builder(Component.literal("✓"), b -> onSubmitClicked())
                        .bounds(submitSlotX + 20, submitSlotY, 20, 20)
                        .build()
        );

        // Render after so its on top
        this.addRenderableWidget(this.tierDropdown);

    }

    // ----- button callbacks -----
    private void onRefreshClicked() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

    private void onSubmitClicked() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
        }
    }

    private void onTierSelected(int index) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            // 10..14 are tier 1..5 in PlushMenu.clickMenuButton
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 10 + index);
        }
    }

    /**
     * Draws the "Reward Pool" ghost items (itemsToReceive for the selected tier)
     * inside the big grey box, and shows a tooltip with chance on hover.
     */
    private void renderRewardPool(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Which tier are we on? (block entity keeps this in sync)
        int tierIndex = this.menu.blockEntity.getSelectedTier();

        PlushTierConfig tierConfig = PlushTierConfigManager.getTierConfig(tierIndex);
        List<PlushItemEntry> rewards = tierConfig.itemsToReceive;
        if (rewards == null || rewards.isEmpty()) {
            return;
        }

        // Layout inside the big grey "reward pool" rectangle.
        // Tweak these if you want items closer/further from the edges.
        int startX = this.leftPos + 9;   // left padding inside box
        int startY = this.topPos + 60;   // top padding inside box
        int slotSize = 18;               // like normal slot size
        int cols = 7;                    // how many item "slots" per row

        // total weight for percentage calculation
        int totalWeight = 0;
        for (PlushItemEntry e : rewards) {
            totalWeight += Math.max(e.weight, 0);
        }
        if (totalWeight <= 0) {
            return;
        }

        for (int i = 0; i < rewards.size(); i++) {
            PlushItemEntry entry = rewards.get(i);

            // make an ItemStack for rendering
            ItemStack stack = entryToStack(entry);
            if (stack.isEmpty()) continue;

            int col = i % cols;
            int row = i / cols;

            int x = startX + col * slotSize;
            int y = startY + row * slotSize;

            // draw the item like a normal slot content
            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(this.font, stack, x, y);

            // hover detection & tooltip
            if (mouseX >= x && mouseX < x + 16 &&
                    mouseY >= y && mouseY < y + 16) {

                double chance = (entry.weight * 100.0) / totalWeight;
                String chanceText = String.format(Locale.ROOT, "%.1f%% chance", chance);

                List<Component> tooltip = new ArrayList<>();
                tooltip.add(stack.getHoverName());
                tooltip.add(Component.literal("x" + entry.count)
                        .withStyle(ChatFormatting.GRAY));
                tooltip.add(Component.literal(chanceText)
                        .withStyle(ChatFormatting.DARK_GREEN));

                List<FormattedCharSequence> visualLines = tooltip.stream()
                        .map(Component::getVisualOrderText)
                        .toList();

                guiGraphics.renderTooltip(this.font, visualLines, mouseX, mouseY);
            }
        }
    }

    private void renderTierPreviewTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.tierDropdown == null) return;

        int dx = this.tierDropdown.getX();
        int dy = this.tierDropdown.getY();
        int w  = this.tierDropdown.getWidth();
        int h  = this.tierDropdown.getHeight();

        int hoveredTierIndex = -1;

        // If dropdown is open, check which option row is hovered
        if (this.tierDropdown.isOpen()) {
            int optionHeight = h;
            for (int i = 0; i < this.tierDropdown.getOptionCount(); i++) {
                int oy = dy + h + i * optionHeight;
                int ox = dx;
                if (mouseX >= ox && mouseX < ox + w && mouseY >= oy && mouseY < oy + optionHeight) {
                    hoveredTierIndex = i;
                    break;
                }
            }
        } else {
            // Closed: hovering the main box previews the currently selected tier
            if (mouseX >= dx && mouseX < dx + w && mouseY >= dy && mouseY < dy + h) {
                hoveredTierIndex = this.tierDropdown.getSelectedIndex();
            }
        }

        if (hoveredTierIndex < 0) return;

        // Get config for that tier
        PlushTierConfig tierCfg = PlushTierConfigManager.getTierConfig(hoveredTierIndex);
        if (tierCfg == null) return;

        // Lock info
        int required = PlushTierConfigManager.getRequiredCompletionsForTier(hoveredTierIndex);
        boolean unlocked = isTierUnlockedClient(hoveredTierIndex);

        int prevTierIndex = Math.max(0, hoveredTierIndex - 1);
        int completedPrev = this.menu.blockEntity.getTierCompletions(prevTierIndex);

        List<Component> lines = new ArrayList<>();

        // Header: Tier X - Unlocked / Locked
        Component header = Component.literal("Tier " + (hoveredTierIndex + 1) + " - ")
                .append(unlocked
                        ? Component.literal("Unlocked").withStyle(ChatFormatting.GREEN)
                        : Component.literal("Locked").withStyle(ChatFormatting.RED));
        lines.add(header);

        // Progress line for tiers > 1 (lock depends on previous tier)
        if (hoveredTierIndex > 0 && required > 0) {
            lines.add(
                    Component.literal("Progress: ")
                            .append(Component.literal(completedPrev + "/" + required)
                                    .withStyle(unlocked ? ChatFormatting.GREEN : ChatFormatting.YELLOW))
            );

            if (!unlocked) {
                lines.add(
                        Component.literal("Complete Tier " + hoveredTierIndex + " to unlock.")
                                .withStyle(ChatFormatting.GRAY)
                );
            }
        }

        // Blank line spacer
        lines.add(Component.empty());

        // Requirements header
        lines.add(Component.literal("Required items:").withStyle(ChatFormatting.YELLOW));

        if (tierCfg.itemsToGive == null || tierCfg.itemsToGive.isEmpty()) {
            lines.add(Component.literal("None").withStyle(ChatFormatting.GRAY));
        } else {
            for (PlushItemEntry e : tierCfg.itemsToGive) {
                ItemStack stack = entryToStack(e);
                String name = stack.isEmpty() ? e.id : stack.getHoverName().getString();
                lines.add(
                        Component.literal(e.count + "x " + name)
                                .withStyle(ChatFormatting.GRAY)
                );
            }
        }

        // Convert to visual order and draw
        List<FormattedCharSequence> visual = lines.stream()
                .map(Component::getVisualOrderText)
                .toList();

        guiGraphics.renderTooltip(this.font, visual, mouseX, mouseY);
    }

    private boolean isTierUnlockedClient(int tierIndex) {
        // clamp
        tierIndex = Math.max(0, Math.min(tierIndex, PlushBlockEntity.MAX_TIER - 1));

        int required = PlushTierConfigManager.getRequiredCompletionsForTier(tierIndex);

        // Tier 0 or anything with required <= 0 is always usable
        if (required <= 0 || tierIndex == 0) {
            return true;
        }

        // We defined locks in terms of completions of the *previous* tier
        int prevTierIndex = tierIndex - 1;
        int completedPrev = this.menu.blockEntity.getTierCompletions(prevTierIndex);

        return completedPrev >= required;
    }

    /**
     * Convert a config entry to an ItemStack for client-side display.
     */
    private ItemStack entryToStack(PlushItemEntry entry) {
        if (entry == null || entry.id == null || entry.id.isEmpty()) {
            return ItemStack.EMPTY;
        }

        Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.id));
        if (item == Items.AIR) {
            return ItemStack.EMPTY;
        }

        int count = Math.max(1, entry.count);
        return new ItemStack(item, count);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.tierDropdown != null) {
            int beTier = this.menu.blockEntity.getSelectedTier();
            if (this.tierDropdown.getSelectedIndex() != beTier) {
                this.tierDropdown.setSelectedIndex(beTier);
            }
        }
    }

    // ----- background -----
    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - this.imageWidth) / 2;
        int y = (height - this.imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
    }

    // ----- labels (no super call = no duplicate titles/inventory) -----
    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title once at top-left
        guiGraphics.drawString(this.font, this.title, 8, 6, 0x404040, false);

        // Info about the plush in the last slot (same logic you had)
        int menuSlotIndex = this.menu.slots.size() - 2;
        ItemStack testItem = this.menu.getSlot(menuSlotIndex).getItem();

        Component info;
        if (testItem.isEmpty()) {
            info = Component.literal("No Item Selected");
        } else {
            info = Component.literal(testItem.getCount() + "x " + testItem.getHoverName().getString());
        }
        guiGraphics.drawString(this.font, info, 30, 30, 0x404040, false);

        String rewardText = "Reward Pool";
        int rewardX = (this.imageWidth - this.font.width(rewardText)) / 2;
        int rewardY = 47; // tweak up/down as needed to sit nicely above the big grey area
        guiGraphics.drawString(this.font, rewardText, rewardX, rewardY, 0x404040, false);

        // Selected tier text under reward area
        int tier = this.menu.blockEntity.getSelectedTier();
//        guiGraphics.drawString(
//                this.font,
//                Component.literal("Selected tier: " + (tier + 1)),
//                8,
//                62,
//                0x404040,
//                false
//        );

        // Inventory label once, just above first row of player slots
        int firstInvRowY = 84 + PlushMenu.INVENTORY_OFFSET_Y; // same as addPlayerInventory
        int invLabelY = firstInvRowY - 12;
        guiGraphics.drawString(this.font, this.playerInventoryTitle, 8, invLabelY, 0x404040, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        // Render again so it's above the dropdown
        if (this.tierDropdown != null) {
            this.tierDropdown.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        // draw ghost reward items & their tooltips
        renderRewardPool(guiGraphics, mouseX, mouseY);

        // tier requirements preview when hovering dropdown
        renderTierPreviewTooltip(guiGraphics, mouseX, mouseY);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
