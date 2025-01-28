package in.northwestw.shortcircuit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TooltipEditBox extends EditBox {
    private Component tooltip;

    public TooltipEditBox(Font font, int x, int y, int w, int h, Component value) {
        super(font, x, y, w, h, value);
    }

    public TooltipEditBox setTooltip(Component tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public void renderTooltipOnScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        if (this.tooltip == null || !this.isHovered) return;
        screen.renderTooltip(poseStack, this.tooltip, mouseX, mouseY);
    }
}
