package in.northwestw.shortcircuit.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public class HoverTooltip extends AbstractWidget {
    private Component tooltip;

    public HoverTooltip(int x, int y, int w, int h, Component tooltip) {
        super(x, y, w, h, Component.empty());
        this.tooltip = tooltip;
    }

    @Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void setTooltip(@Nullable Component tooltip) {
        this.tooltip = tooltip;
    }

    public void renderTooltipOnScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        if (this.tooltip == null || !this.isHovered) return;
        screen.renderTooltip(poseStack, this.tooltip, mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack poseStack, int x, int y, float partialTicks) {
        // do not render button
    }
}
