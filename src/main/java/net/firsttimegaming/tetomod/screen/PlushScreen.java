package net.firsttimegaming.tetomod.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.firsttimegaming.tetomod.TetoMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class PlushScreen extends AbstractContainerScreen<PlushMenu> {

    // TODO: Create GUI texture and replace the placeholder path below
    private static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(TetoMod.MOD_ID, "textures/gui/plush/place_holder_gui.png");

    public PlushScreen(PlushMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, 256, 256);

    }
}
