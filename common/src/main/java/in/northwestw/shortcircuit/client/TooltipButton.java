package in.northwestw.shortcircuit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TooltipButton extends Button {
    private Component tooltip;

    public TooltipButton(int x, int y, int w, int h, Component message, OnPress onPress) {
        super(x, y, w, h, message, onPress);
    }

    public TooltipButton setTooltip(Component tooltip) {
        this.tooltip = tooltip;
        return this;
    }

    public void renderTooltipOnScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        if (this.tooltip == null || !this.isHovered) return;
        screen.renderTooltip(poseStack, this.tooltip, mouseX, mouseY);
    }
}
