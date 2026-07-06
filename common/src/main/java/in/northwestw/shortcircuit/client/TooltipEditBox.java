package in.northwestw.shortcircuit.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

//? if >=1.20.1 {
import net.minecraft.client.gui.components.Tooltip;
import java.util.function.Supplier;
//? } else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
*///? }

public class TooltipEditBox extends EditBox {
    private Component tooltip;

    public TooltipEditBox(Font font, int x, int y, int w, int h, Component value) {
        super(font, x, y, w, h, value);
    }

    public TooltipEditBox setTooltip(Component tooltip) {
        //? if >=1.20.1 {
        super.setTooltip(Tooltip.create(tooltip));
         //? } else
        //this.tooltip = tooltip;
        return this;
    }

    //? if <=1.19.2 {
    /*public void renderTooltipOnScreen(Screen screen, PoseStack poseStack, int mouseX, int mouseY) {
        if (this.tooltip == null || !this.isHovered) return;
        screen.renderTooltip(poseStack, this.tooltip, mouseX, mouseY);
    }
    *///? }
}
