package net.firsttimegaming.tetomod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.firsttimegaming.tetomod.TetoMod;
import net.firsttimegaming.tetomod.block.entity.PlushBlockEntity;
import net.firsttimegaming.tetomod.config.PlushItemEntry;
import net.firsttimegaming.tetomod.config.PlushTierConfig;
import net.firsttimegaming.tetomod.config.PlushTierConfigManager;
import net.firsttimegaming.tetomod.util.ItemStackUtils;
import net.firsttimegaming.tetomod.util.WeightedRandomUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Screen for the Plush block's trading interface.
 * <p>
 * Displays the tier selection, reward pool, and submit/refresh controls
 * for the plush trading system.
 */
public class PlushScreen extends AbstractContainerScreen<PlushMenu> {

    // ==================== Class Variables ====================

    /** The GUI background texture resource location. */
    private static final ResourceLocation GUI_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(TetoMod.MOD_ID, "textures/gui/plush/plush_gui.png");

    /** GUI width in pixels. */
    private static final int GUI_WIDTH = 175;

    /** GUI height in pixels. */
    private static final int GUI_HEIGHT = 241;

    /** GUI texture size in pixels. */
    private static final int TEXTURE_SIZE = 256;

    /** Title X offset from left edge. */
    private static final int TITLE_X = 8;

    /** Title Y position. */
    private static final int TITLE_Y = 6;

    /** Text color for labels. */
    private static final int LABEL_COLOR = 0x404040;

    /** Shader color components. */
    private static final float SHADER_COLOR = 1.0F;

    /** Item display X offset. */
    private static final int INFO_X = 30;

    /** Item display Y offset. */
    private static final int INFO_Y = 30;

    /** Reward pool label Y position. */
    private static final int REWARD_LABEL_Y = 47;

    /** Inventory label Y offset from first row. */
    private static final int INV_LABEL_OFFSET = 12;

    /** Dropdown width. */
    private static final int DROPDOWN_WIDTH = 80;

    /** Dropdown height. */
    private static final int DROPDOWN_HEIGHT = 16;

    /** Dropdown X offset from title. */
    private static final int DROPDOWN_X_OFFSET = 61;

    /** Dropdown Y offset from top. */
    private static final int DROPDOWN_Y_OFFSET = 4;

    /** Button width. */
    private static final int BUTTON_SIZE = 20;

    /** Button X offset from submit slot. */
    private static final int BUTTON_OFFSET = 24;

    /** Submit slot GUI X position. */
    private static final int SUBMIT_SLOT_GUI_X = 81;

    /** Submit slot GUI Y position. */
    private static final int SUBMIT_SLOT_GUI_Y = 130;

    /** Upgrade slot GUI X position. */
    private static final int UPGRADE_SLOT_GUI_X = 9;

    /** Upgrade slot GUI Y position. */
    private static final int UPGRADE_SLOT_GUI_Y = 130;

    /** Button Y adjustment. */
    private static final int BUTTON_Y_ADJUST = 2;

    /** Reward pool starting X position. */
    private static final int REWARD_POOL_START_X = 9;

    /** Reward pool starting Y position. */
    private static final int REWARD_POOL_START_Y = 60;

    /** Size of each reward pool item slot. */
    private static final int REWARD_SLOT_SIZE = 18;

    /** Number of columns in the reward pool display. */
    private static final int REWARD_POOL_COLS = 7;

    /** Item hover detection size. */
    private static final int ITEM_HOVER_SIZE = 16;

    /** Percentage multiplier for tooltip. */
    private static final double PERCENTAGE_MULTIPLIER = 100.0;

    /** Maximum tier index for clamping. */
    private static final int MAX_TIER_INDEX = 4;

    /** The refresh button widget. */
    private Button refreshButton;

    /** The tier selection dropdown widget. */
    private DropdownWidget tierDropdown;

    // ==================== Constructor ====================

    /**
     * Constructs a new PlushScreen.
     *
     * @param menu      the container menu
     * @param inventory the player inventory
     * @param title     the screen title
     */
    public PlushScreen(PlushMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
    }

    // ==================== Custom Methods ====================

    /**
     * Handles the refresh button click.
     */
    private void onRefreshClicked() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 0);
        }
    }

    /**
     * Handles the submit button click.
     */
    private void onSubmitClicked() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 1);
        }
    }

    /**
     * Handles tier selection from the dropdown.
     *
     * @param index the selected tier index (0-based)
     */
    private void onTierSelected(int index) {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 10 + index);
        }
    }

    /**
     * Renders the reward pool items and their tooltips.
     *
     * @param guiGraphics the graphics context
     * @param mouseX      the mouse X position
     * @param mouseY      the mouse Y position
     */
    private void renderRewardPool(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int tierIndex = this.menu.blockEntity.getSelectedTier();

        PlushTierConfig tierConfig = PlushTierConfigManager.getTierConfig(tierIndex);
        List<PlushItemEntry> rewards = tierConfig.itemsToReceive;
        if (rewards == null || rewards.isEmpty()) {
            return;
        }

        int startX = this.leftPos + REWARD_POOL_START_X;
        int startY = this.topPos + REWARD_POOL_START_Y;

        int totalWeight = WeightedRandomUtils.calculateTotalWeight(rewards);
        if (totalWeight <= 0) {
            return;
        }

        for (int i = 0; i < rewards.size(); i++) {
            PlushItemEntry entry = rewards.get(i);

            ItemStack stack = ItemStackUtils.toStack(entry);
            if (stack.isEmpty()) {
                continue;
            }

            int col = i % REWARD_POOL_COLS;
            int row = i / REWARD_POOL_COLS;

            int x = startX + col * REWARD_SLOT_SIZE;
            int y = startY + row * REWARD_SLOT_SIZE;

            guiGraphics.renderItem(stack, x, y);
            guiGraphics.renderItemDecorations(this.font, stack, x, y);

            if (mouseX >= x && mouseX < x + ITEM_HOVER_SIZE &&
                    mouseY >= y && mouseY < y + ITEM_HOVER_SIZE) {

                double chance = (entry.weight * PERCENTAGE_MULTIPLIER) / totalWeight;
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

    /**
     * Renders the tier preview tooltip when hovering over the dropdown.
     *
     * @param guiGraphics the graphics context
     * @param mouseX      the mouse X position
     * @param mouseY      the mouse Y position
     */
    private void renderTierPreviewTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.tierDropdown == null) {
            return;
        }

        int dx = this.tierDropdown.getX();
        int dy = this.tierDropdown.getY();
        int w = this.tierDropdown.getWidth();
        int h = this.tierDropdown.getHeight();

        int hoveredTierIndex = -1;

        // Figure out which tier index is hovered
        if (this.tierDropdown.isOpen()) {
            int optionHeight = h;
            for (int i = 0; i < this.tierDropdown.getOptionCount(); i++) {
                int oy = dy + h + i * optionHeight;
                if (mouseX >= dx && mouseX < dx + w && mouseY >= oy && mouseY < oy + optionHeight) {
                    hoveredTierIndex = i;
                    break;
                }
            }
        } else {
            if (mouseX >= dx && mouseX < dx + w && mouseY >= dy && mouseY < dy + h) {
                hoveredTierIndex = this.tierDropdown.getSelectedIndex();
            }
        }

        if (hoveredTierIndex < 0) {
            return;
        }

        PlushTierConfig tierCfg = PlushTierConfigManager.getTierConfig(hoveredTierIndex);
        if (tierCfg == null) {
            return;
        }

        // New: unlock requirement is an item, not completions
        PlushItemEntry unlockReq = PlushTierConfigManager.getUnlockRequirementForTier(hoveredTierIndex);
        boolean unlocked = isTierUnlockedClient(hoveredTierIndex);

        List<Component> lines = new ArrayList<>();

        // Header: Tier N - Unlocked / Locked
        Component header = Component.literal("Tier " + (hoveredTierIndex + 1) + " - ")
                .append(unlocked
                        ? Component.literal("Unlocked").withStyle(ChatFormatting.GREEN)
                        : Component.literal("Locked").withStyle(ChatFormatting.RED));
        lines.add(header);

        // Unlock info
        if (hoveredTierIndex == 0 || unlockReq == null) {
            // Tier 1 (or tiers with no requirement) = always available
            lines.add(Component.literal("Unlock: Available by default")
                    .withStyle(ChatFormatting.GRAY));
        } else {
            lines.add(Component.empty());
            lines.add(Component.literal("Unlock requirement:")
                    .withStyle(ChatFormatting.YELLOW));

            ItemStack unlockStack = ItemStackUtils.toStack(unlockReq);
            String unlockName = unlockStack.isEmpty()
                    ? unlockReq.id
                    : unlockStack.getHoverName().getString();

            lines.add(
                    Component.literal(unlockReq.count + "x " + unlockName)
                            .withStyle(unlocked ? ChatFormatting.GRAY : ChatFormatting.RED)
            );

            if (!unlocked) {
                lines.add(
                        Component.literal("Place this in the upgrade slot and press Upgrade.")
                                .withStyle(ChatFormatting.DARK_GRAY)
                );
            }
        }

        // Spacer
        lines.add(Component.empty());

        // Required items (the quest pool)
        lines.add(Component.literal("Required items:")
                .withStyle(ChatFormatting.YELLOW));

        if (tierCfg.itemsToGive == null || tierCfg.itemsToGive.isEmpty()) {
            lines.add(Component.literal("None").withStyle(ChatFormatting.GRAY));
        } else {
            for (PlushItemEntry e : tierCfg.itemsToGive) {
                ItemStack stack = ItemStackUtils.toStack(e);
                String name = stack.isEmpty() ? e.id : stack.getHoverName().getString();
                lines.add(
                        Component.literal(e.count + "x " + name)
                                .withStyle(ChatFormatting.GRAY)
                );
            }
        }

        // Convert to visual lines and render
        List<FormattedCharSequence> visual = lines.stream()
                .map(Component::getVisualOrderText)
                .toList();

        guiGraphics.renderTooltip(this.font, visual, mouseX, mouseY);
    }


    /**
     * Checks if a tier is unlocked on the client side.
     *
     * @param tierIndex the tier index to check
     * @return true if the tier is unlocked
     */
    private boolean isTierUnlockedClient(int tierIndex) {
        tierIndex = Math.max(0, Math.min(tierIndex, PlushBlockEntity.MAX_TIER - 1));
        boolean unlocked = this.menu.blockEntity.isTierUnlocked(tierIndex);

        TetoMod.LOGGER.info(
                "[CLIENT-PlushScreen.isTierUnlockedClient] tier={} unlockedFromBE={}",
                tierIndex, unlocked
        );

        return unlocked;
    }


    private boolean isMouseOverRefresh(int mouseX, int mouseY) {
        if (refreshButton == null) return false;
        return mouseX >= refreshButton.getX() &&
                mouseX < refreshButton.getX() + refreshButton.getWidth() &&
                mouseY >= refreshButton.getY() &&
                mouseY < refreshButton.getY() + refreshButton.getHeight();
    }

    private void onUpgradeClicked() {
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            // button id 2 is upgrade
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, 2);
        }
    }

    private void rebuildTierDropdownOptions() {
        if (this.tierDropdown == null) {
            return;
        }

        int tierCount = 5; // or your constant
        List<Component> labels = new ArrayList<>();

        int beTier = this.menu.blockEntity.getSelectedTier();
        TetoMod.LOGGER.info(
                "[CLIENT-PlushScreen.rebuildTierDropdownOptions] pos={} beTier={}",
                this.menu.blockEntity.getBlockPos(),
                beTier
        );

        for (int i = 0; i < tierCount; i++) {
            boolean unlocked = isTierUnlockedClient(i);

            TetoMod.LOGGER.info(
                    "[CLIENT-PlushScreen.rebuildTierDropdownOptions] tier={} unlocked={}",
                    i, unlocked
            );

            Component label = unlocked
                    ? Component.literal("Tier " + (i + 1))
                    : Component.literal("Tier " + (i + 1) + " (Locked)");

            labels.add(label);
        }

        this.tierDropdown.setOptions(labels);
        this.tierDropdown.setSelectedIndex(beTier);
    }



    // ==================== Overridden Methods ====================

    private int lastLoggedBeTier = -999;
    private int lastLoggedDropdownIndex = -999;

    @Override
    protected void init() {
        super.init();

        int left = this.leftPos;
        int top = this.topPos;

        String titleText = this.title.getString();
        int titleWidth = this.font.width(titleText);

        int dropdownX = left + TITLE_X + titleWidth + DROPDOWN_X_OFFSET;
        int dropdownY = top + DROPDOWN_Y_OFFSET;

        this.tierDropdown = new DropdownWidget(
                dropdownX,
                dropdownY,
                DROPDOWN_WIDTH,
                DROPDOWN_HEIGHT,
                List.of(
                        Component.literal("Tier 1"),
                        Component.literal("Tier 2"),
                        Component.literal("Tier 3"),
                        Component.literal("Tier 4"),
                        Component.literal("Tier 5")
                ),
                this::onTierSelected
        );

        int beTier = this.menu.getSelectedTier();
        beTier = Math.max(0, Math.min(beTier, MAX_TIER_INDEX));
        this.tierDropdown.setSelectedIndex(beTier);

        int submitSlotX = left + SUBMIT_SLOT_GUI_X;
        int submitSlotY = top + SUBMIT_SLOT_GUI_Y - BUTTON_Y_ADJUST;

        this.refreshButton = Button.builder(Component.literal("R"), b -> onRefreshClicked())
                .bounds(submitSlotX - BUTTON_OFFSET, submitSlotY, BUTTON_SIZE, BUTTON_SIZE)
                .build();

        this.addRenderableWidget(this.refreshButton);

        this.addRenderableWidget(
                Button.builder(Component.literal("✓"), b -> onSubmitClicked())
                        .bounds(submitSlotX + BUTTON_SIZE, submitSlotY, BUTTON_SIZE, BUTTON_SIZE)
                        .build()
        );

        int upgradeSlotX = left + UPGRADE_SLOT_GUI_X;
        int upgradeSlotY = top + UPGRADE_SLOT_GUI_Y;

        this.addRenderableWidget(
                Button.builder(Component.literal("▲"), b -> onUpgradeClicked())
                        .bounds(upgradeSlotX + 20, upgradeSlotY - 2, 20, 20)
                        .build()
        );

        this.addRenderableWidget(this.tierDropdown);

        rebuildTierDropdownOptions();

        if (this.tierDropdown != null) {
            TetoMod.LOGGER.info(
                    "[CLIENT-PlushScreen.init] pos={} beTier={} dropdownIndex={} label='{}'",
                    this.menu.blockEntity.getBlockPos(),
                    this.menu.blockEntity.getSelectedTier(),
                    this.tierDropdown.getSelectedIndex(),
                    this.tierDropdown.getMessage().getString()
            );
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        int serverTier = this.menu.getSelectedTier();

        if (this.tierDropdown != null && this.tierDropdown.getSelectedIndex() != serverTier) {
            this.tierDropdown.setSelectedIndex(serverTier);

            rebuildTierDropdownOptions();

            int beTier = this.menu.blockEntity.getSelectedTier();
            int dropdownIndex = this.tierDropdown.getSelectedIndex();
            if (beTier != lastLoggedBeTier || dropdownIndex != lastLoggedDropdownIndex) {
                lastLoggedBeTier = beTier;
                lastLoggedDropdownIndex = dropdownIndex;

                TetoMod.LOGGER.info(
                        "[CLIENT-PlushScreen.tick] pos={} beTier={} dropdownIndex={} label='{}'",
                        this.menu.blockEntity.getBlockPos(),
                        beTier,
                        dropdownIndex,
                        this.tierDropdown.getMessage().getString()
                );
            }
        }

        if (this.refreshButton != null) {
            long remaining = this.menu.blockEntity.getRerollCooldownRemainingTicks();
            this.refreshButton.active = (remaining <= 0);
        }
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(SHADER_COLOR, SHADER_COLOR, SHADER_COLOR, SHADER_COLOR);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - this.imageWidth) / 2;
        int y = (height - this.imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, TITLE_X, TITLE_Y, LABEL_COLOR, false);

        int menuSlotIndex = this.menu.slots.size() - 3;
        ItemStack testItem = this.menu.getSlot(menuSlotIndex).getItem();

        Component info;
        if (testItem.isEmpty()) {
            info = Component.literal("No Item Selected");
        } else {
            info = Component.literal(testItem.getCount() + "x " + testItem.getHoverName().getString());
        }
        guiGraphics.drawString(this.font, info, INFO_X, INFO_Y, LABEL_COLOR, false);

        String rewardText = "Reward Pool";
        int rewardX = (this.imageWidth - this.font.width(rewardText)) / 2;
        guiGraphics.drawString(this.font, rewardText, rewardX, REWARD_LABEL_Y, LABEL_COLOR, false);

        int firstInvRowY = 84 + PlushMenu.INVENTORY_OFFSET_Y;
        int invLabelY = firstInvRowY - INV_LABEL_OFFSET;
        guiGraphics.drawString(this.font, this.playerInventoryTitle, TITLE_X, invLabelY, LABEL_COLOR, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        if (this.tierDropdown != null) {
            this.tierDropdown.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (this.refreshButton != null &&
                this.isMouseOverRefresh(mouseX, mouseY)) {

            long remaining = this.menu.blockEntity.getRerollCooldownRemainingTicks();

            if (remaining <= 0) {
                guiGraphics.renderTooltip(
                        this.font,
                        Component.literal("Reroll requirement"),
                        mouseX, mouseY
                );
            } else {
                long seconds = remaining / 20L;
                long minutes = seconds / 60L;
                long secR = seconds % 60L;

                guiGraphics.renderTooltip(
                        this.font,
                        Component.literal(
                                "Reroll on cooldown (" +
                                        minutes + "m " + secR + "s remaining)"
                        ),
                        mouseX, mouseY
                );
            }
        }

        renderRewardPool(guiGraphics, mouseX, mouseY);
        renderTierPreviewTooltip(guiGraphics, mouseX, mouseY);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
