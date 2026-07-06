package in.northwestw.shortcircuit.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

//? if >=1.20.1 {
import net.minecraft.client.gui.components.Tooltip;
import java.util.function.Supplier;
//? } else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;
*///? }

//? if >=1.21.11 {
public class TooltipButton extends Button.Plain {
//? } else
//public class TooltipButton extends Button {
    private Component tooltip;

    public TooltipButton(int x, int y, int w, int h, Component message, OnPress onPress) {
        //? if >=1.20.1 {
        super(x, y, w, h, message, onPress, Supplier::get);
        //? } else
        //super(x, y, w, h, message, onPress);
    }

    public TooltipButton setTooltip(Component tooltip) {
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