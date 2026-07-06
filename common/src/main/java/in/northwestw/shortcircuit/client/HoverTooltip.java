package in.northwestw.shortcircuit.client;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

//? if >=1.20.1 {
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
//? } else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
*///? }

//? if >=1.20.1 {
public class HoverTooltip extends StringWidget {
//? } else
//public class HoverTooltip extends AbstractWidget {
    private Component tooltip;

    public HoverTooltip(int x, int y, int w, int h, Component tooltip, Font font) {
        //? if >=1.20.1 {
        super(x, y, w, h, Component.empty(), font);
        //? } else
        //super(x, y, w, h, Component.empty());
        this.tooltip = tooltip;
    }

    public void setTooltip(@Nullable Component tooltip) {
        //? if >=1.20.1 {
        super.setTooltip(Tooltip.create(tooltip));
         //? } else
        //this.tooltip = tooltip;
    }

    //? <=1.19.2 {
    /*@Override
    public void updateNarration(NarrationElementOutput narrationElementOutput) {

    }

    public void renderTooltipOnScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        if (this.tooltip == null || !this.isHovered) return;
        screen.renderTooltip(poseStack, this.tooltip, mouseX, mouseY);
    }

    @Override
    public void renderButton(PoseStack poseStack, int x, int y, float partialTicks) {
        // do not render button
    }
    *///? }
}