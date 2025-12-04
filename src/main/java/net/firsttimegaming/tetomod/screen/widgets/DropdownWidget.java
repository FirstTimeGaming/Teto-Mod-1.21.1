package net.firsttimegaming.tetomod.client.widget;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

public class DropdownWidget extends AbstractWidget {

    private final List<Component> options;
    private final Consumer<Integer> onSelectionChanged;
    private int selectedIndex = 0;
    private boolean open = false;

    public DropdownWidget(int x, int y, int width, int height,
                          List<Component> options,
                          Consumer<Integer> onSelectionChanged) {
        super(x, y, width, height, options.isEmpty() ? Component.empty() : options.get(0));
        this.options = options;
        this.onSelectionChanged = onSelectionChanged;
    }

    private void drawBorder(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        // top
        g.fill(x1, y1, x2, y1 + 1, color);
        // bottom
        g.fill(x1, y2 - 1, x2, y2, color);
        // left
        g.fill(x1, y1, x1 + 1, y2, color);
        // right
        g.fill(x2 - 1, y1, x2, y2, color);
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        if (index >= 0 && index < options.size()) {
            this.selectedIndex = index;
            this.setMessage(options.get(index));
            if (onSelectionChanged != null) {
                onSelectionChanged.accept(index);
            }
        }
    }

    public int getOptionCount() {
        return options.size();
    }

    public boolean isOpen() {
        return open;
    }

    public void close() {
        this.open = false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // main box
        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        int bgColor  = 0xFFCCCCCC; // light grey
        int border   = 0xFF000000; // black
        int hoverCol = 0xFFFFFFFF; // white border on hover

        // background
        guiGraphics.fill(x, y, x + w, y + h, bgColor);
        // border
        guiGraphics.fill(x, y, x + w, y + 1, border);
        guiGraphics.fill(x, y + h - 1, x + w, y + h, border);
        guiGraphics.fill(x, y, x + 1, y + h, border);
        guiGraphics.fill(x + w - 1, y, x + w, y + h, border);

        // hover outline
        if (isMouseOver(mouseX, mouseY)) {
            drawBorder(guiGraphics, x, y, x + w, y + h, hoverCol);
        }

        // draw current text
        if (!options.isEmpty()) {
            String text = this.getMessage().getString();
            guiGraphics.drawString(Minecraft.getInstance().font, text, x + 4, y + (h - 8) / 2, 0xFF000000, false);
        }

        // small ▼ indicator on the right
        guiGraphics.drawString(Minecraft.getInstance().font, "▼", x + w - 8, y + (h - 8) / 2, 0xFF000000, false);

        // if open, draw option list below
        if (open && !options.isEmpty()) {
            int optionHeight = h;
            for (int i = 0; i < options.size(); i++) {
                int oy = y + h + i * optionHeight;
                int ox = x;

                guiGraphics.fill(ox, oy, ox + w, oy + optionHeight, bgColor);
                drawBorder(guiGraphics, ox, oy, ox + w, oy + optionHeight, border);

                // highlight on hover
                if (mouseX >= ox && mouseX < ox + w && mouseY >= oy && mouseY < oy + optionHeight) {
                    guiGraphics.fill(ox + 1, oy + 1, ox + w - 1, oy + optionHeight - 1, 0x40FFFFFF);
                }

                guiGraphics.drawString(Minecraft.getInstance().font, options.get(i), ox + 4, oy + (optionHeight - 8) / 2, 0xFF000000, false);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible || button != 0) return false;

        int x = getX();
        int y = getY();
        int w = width;
        int h = height;

        // click on main box: toggle open/closed
        if (mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h) {
            open = !open;
            return true;
        }

        // if open, check option clicks
        if (open) {
            int optionHeight = h;
            for (int i = 0; i < options.size(); i++) {
                int oy = y + h + i * optionHeight;
                int ox = x;
                if (mouseX >= ox && mouseX < ox + w && mouseY >= oy && mouseY < oy + optionHeight) {
                    setSelectedIndex(i);
                    open = false;
                    return true;
                }
            }

            // click outside closes it
            open = false;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!visible) return false;
        int x = getX();
        int y = getY();
        int w = width;
        int h = height;
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        // TODO: implement narration for accessibility
    }


}
