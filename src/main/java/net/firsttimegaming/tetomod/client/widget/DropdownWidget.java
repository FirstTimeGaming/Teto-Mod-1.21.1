package net.firsttimegaming.tetomod.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * A dropdown widget for selecting from a list of options.
 * <p>
 * Displays the currently selected option and expands to show all options when clicked.
 */
public class DropdownWidget extends AbstractWidget {

    /** Background color for the dropdown (light grey). */
    private static final int COLOR_BACKGROUND = 0xFFCCCCCC;

    /** Border color (black). */
    private static final int COLOR_BORDER = 0xFF000000;

    /** Hover highlight color (white). */
    private static final int COLOR_HOVER = 0xFFFFFFFF;

    /** Text color (black). */
    private static final int COLOR_TEXT = 0xFF000000;

    /** Hover overlay color (semi-transparent white). */
    private static final int COLOR_OPTION_HOVER = 0x40FFFFFF;

    /** Border thickness in pixels. */
    private static final int BORDER_THICKNESS = 1;

    /** Text padding from the left edge. */
    private static final int TEXT_PADDING = 4;

    /** Font height for vertical centering calculations. */
    private static final int FONT_HEIGHT = 8;

    /** Dropdown indicator symbol. */
    private static final String DROPDOWN_INDICATOR = "▼";

    /** Offset from the right edge for the dropdown indicator. */
    private static final int INDICATOR_OFFSET = 8;

    /** The list of selectable options. */
    private final List<Component> options;

    /** Callback when the selection changes. */
    private final Consumer<Integer> onSelectionChanged;

    /** The currently selected option index. */
    private int selectedIndex = 0;

    /** Whether the dropdown is currently expanded. */
    private boolean open = false;

    /**
     * Constructs a new DropdownWidget.
     *
     * @param x                   the X position
     * @param y                   the Y position
     * @param width               the width
     * @param height              the height
     * @param options             the list of selectable options
     * @param onSelectionChanged  callback when selection changes
     */
    public DropdownWidget(int x, int y, int width, int height,
                          List<Component> options,
                          Consumer<Integer> onSelectionChanged) {
        super(x, y, width, height, options.isEmpty() ? Component.empty() : options.get(0));
        this.options = options;
        this.onSelectionChanged = onSelectionChanged;
    }

    /**
     * Draws a border around the specified rectangle.
     *
     * @param g     the graphics context
     * @param x1    left edge
     * @param y1    top edge
     * @param x2    right edge
     * @param y2    bottom edge
     * @param color the border color
     */
    private void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y1 + BORDER_THICKNESS, color);
        g.fill(x1, y2 - BORDER_THICKNESS, x2, y2, color);
        g.fill(x1, y1, x1 + BORDER_THICKNESS, y2, color);
        g.fill(x2 - BORDER_THICKNESS, y1, x2, y2, color);
    }

    /**
     * Gets the currently selected option index.
     *
     * @return the selected index
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    /**
     * Sets the selected option index.
     *
     * @param index the index to select
     */
    public void setSelectedIndex(int index) {
        if (index >= 0 && index < options.size()) {
            this.selectedIndex = index;
            this.setMessage(options.get(index));
            if (onSelectionChanged != null) {
                onSelectionChanged.accept(index);
            }
        }
    }

    /**
     * Gets the number of options in the dropdown.
     *
     * @return the option count
     */
    public int getOptionCount() {
        return options.size();
    }

    /**
     * Checks if the dropdown is currently expanded.
     *
     * @return true if open, false otherwise
     */
    public boolean isOpen() {
        return open;
    }

    /**
     * Closes the dropdown.
     */
    public void close() {
        this.open = false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        guiGraphics.fill(x, y, x + w, y + h, COLOR_BACKGROUND);
        drawBorder(guiGraphics, x, y, x + w, y + h, COLOR_BORDER);

        if (isMouseOver(mouseX, mouseY)) {
            drawBorder(guiGraphics, x, y, x + w, y + h, COLOR_HOVER);
        }

        if (!options.isEmpty()) {
            String text = this.getMessage().getString();
            int textY = y + (h - FONT_HEIGHT) / 2;
            guiGraphics.drawString(Minecraft.getInstance().font, text, x + TEXT_PADDING, textY, COLOR_TEXT, false);
        }

        int indicatorY = y + (h - FONT_HEIGHT) / 2;
        guiGraphics.drawString(Minecraft.getInstance().font, DROPDOWN_INDICATOR, x + w - INDICATOR_OFFSET, indicatorY, COLOR_TEXT, false);

        if (open && !options.isEmpty()) {
            int optionHeight = h;
            for (int i = 0; i < options.size(); i++) {
                int oy = y + h + i * optionHeight;

                guiGraphics.fill(x, oy, x + w, oy + optionHeight, COLOR_BACKGROUND);
                drawBorder(guiGraphics, x, oy, x + w, oy + optionHeight, COLOR_BORDER);

                if (mouseX >= x && mouseX < x + w && mouseY >= oy && mouseY < oy + optionHeight) {
                    guiGraphics.fill(x + BORDER_THICKNESS, oy + BORDER_THICKNESS,
                            x + w - BORDER_THICKNESS, oy + optionHeight - BORDER_THICKNESS, COLOR_OPTION_HOVER);
                }

                int optionTextY = oy + (optionHeight - FONT_HEIGHT) / 2;
                guiGraphics.drawString(Minecraft.getInstance().font, options.get(i), x + TEXT_PADDING, optionTextY, COLOR_TEXT, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || button != 0) {
            return false;
        }

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            open = !open;
            return true;
        }

        if (open) {
            int optionHeight = h;
            for (int i = 0; i < options.size(); i++) {
                int oy = y + h + i * optionHeight;
                if (mouseX >= x && mouseX < x + w && mouseY >= oy && mouseY < oy + optionHeight) {
                    setSelectedIndex(i);
                    open = false;
                    return true;
                }
            }

            open = false;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!visible) {
            return false;
        }
        int x = getX();
        int y = getY();
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.getMessage());
        if (!options.isEmpty() && selectedIndex >= 0 && selectedIndex < options.size()) {
            narrationElementOutput.add(NarratedElementType.USAGE,
                    Component.literal("Selected: " + options.get(selectedIndex).getString() +
                            ". " + (open ? "Press to close dropdown" : "Press to open dropdown")));
        }
    }
}
