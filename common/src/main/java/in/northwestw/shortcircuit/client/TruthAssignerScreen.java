package in.northwestw.shortcircuit.client;

import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.registries.Items;
import in.northwestw.shortcircuit.registries.menus.TruthAssignerMenu;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;

//? if >=1.21.11 {
import net.minecraft.client.renderer.RenderPipelines;
//? } elif >=1.20.1 {
/*import net.minecraft.client.renderer.RenderType;
*///? }

//? if >=1.20.1 {
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.Tooltip;
//? } else {
/*import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
*///? }

public class TruthAssignerScreen extends AbstractContainerScreen<TruthAssignerMenu> implements ContainerListener {
    private static final Identifier BASE_BACKGROUND = ShortCircuitCommon.rl("textures/gui/container/truth_assigner.png");
    //? if >=1.21.1 {
    private static final Identifier BURN_PROGRESS_SPRITE = Identifier.withDefaultNamespace("container/furnace/burn_progress");
    //? } else
    //private static final Identifier BURN_PROGRESS_SPRITE = new Identifier("textures/gui/container/furnace.png");

    private TooltipEditBox maxDelay;
    private TooltipButton wait, start, bits;
    private HoverTooltip error, currentInput;

    public TruthAssignerScreen(TruthAssignerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        this.maxDelay = new TooltipEditBox(this.font, i + 103, j + 14, 30, 16, Component.translatable("container.short_circuit.truth_assigner.max_delay"));
        this.maxDelay.setTooltip(Component.translatable("container.short_circuit.truth_assigner.max_delay.desc"));
        this.maxDelay.setResponder(this::onMaxDelayChange);
        this.maxDelay.setValue(Integer.toString(this.menu.getMaxDelay()));

        this.bits = new TooltipButton(i + 133, j + 14, 30, 16, this.bitsTranslatable(), this::onBitsPress);
        this.updateBits();
        this.wait = new TooltipButton(i + 103, j + 35, 60, 16, this.waitTranslatable(), this::onWaitPress);
        this.updateWait();
        this.start = new TooltipButton(i + 103, j + 56, 60, 16, Component.translatable("container.short_circuit.truth_assigner.start"), this::onStartPress);
        this.start.setTooltip(Component.translatable("container.short_circuit.truth_assigner.start.desc"));

        this.error = new HoverTooltip(i, j - 24, this.imageWidth, 16, Component.empty(), this.font);
        this.currentInput = new HoverTooltip(i + 37, j + 34, 24, 16, Component.empty(), this.font);
        this.updateCurrentInput();

        this.updateFields();

        this.addRenderableWidget(this.maxDelay);
        this.addRenderableWidget(this.bits);
        this.addRenderableWidget(this.wait);
        this.addRenderableWidget(this.start);
        this.addRenderableWidget(this.error);
        this.addRenderableWidget(this.currentInput);

        this.menu.addSlotListener(this);
    }

    @Override
    public void removed() {
        super.removed();
        this.menu.removeSlotListener(this);
    }

    @Override
    //~ if <=1.19.2 'GuiGraphics' -> 'PoseStack'
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        //? if >=1.21.11 {
        graphics.blit(RenderPipelines.GUI_TEXTURED, BASE_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.isWorking()) {
            // if we are working, color the arrow
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, BURN_PROGRESS_SPRITE, 24, 16, 0, 0, this.leftPos + 37, this.topPos + 34, 24, 16);
        }
        //? } elif >=1.21.4 {
        /*graphics.blit(RenderType::guiTextured, BASE_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.isWorking()) {
            // if we are working, color the arrow
            graphics.blitSprite(RenderType::guiTextured, BURN_PROGRESS_SPRITE, 24, 16, 0, 0, this.leftPos + 37, this.topPos + 34, 24, 16);
        }
        *///? } elif >=1.21.1 {
        /*graphics.blit(BASE_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.isWorking()) {
            // if we are working, color the arrow
            graphics.blitSprite(BURN_PROGRESS_SPRITE, 24, 16, 0, 0, this.leftPos + 37, this.topPos + 34, 24, 16);
        }
        *///? } elif >=1.20.1 {
        /*graphics.blit(BASE_BACKGROUND, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight, 256, 256);
        if (this.menu.isWorking()) {
            // if we are working, color the arrow
            graphics.blit(BURN_PROGRESS_SPRITE, this.leftPos + 37, this.topPos + 34, 176, 14, 25, 16);
        }
        *///? } else {
        /*RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, BASE_BACKGROUND);
        this.blit(graphics, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (this.menu.isWorking()) {
            // if we are working, color the arrow
            RenderSystem.setShaderTexture(0, BURN_PROGRESS_SPRITE);
            this.blit(graphics, this.leftPos + 37, this.topPos + 34, 176, 14, 25, 16);
        }
        *///? }
    }

    @Override
    //~ if <=1.19.2 'GuiGraphics' -> 'PoseStack'
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        //? if <=1.20.1 {
        /*this.renderBackground(graphics);
        *///? }
        super.render(graphics, mouseX, mouseY, partialTick);
        //? if <=1.19.2 {
        /*this.maxDelay.renderTooltipOnScreen(this, graphics, mouseX, mouseY);
        this.wait.renderTooltipOnScreen(this, graphics, mouseX, mouseY);
        this.start.renderTooltipOnScreen(this, graphics, mouseX, mouseY);
        this.bits.renderTooltipOnScreen(this, graphics, mouseX, mouseY);
        this.currentInput.renderTooltipOnScreen(this, graphics, mouseX, mouseY);
        *///? }
    }

    private void onMaxDelayChange(String changed) {
        try {
            if (!changed.isEmpty()) {
                int delay = Integer.parseInt(changed);
                if (this.menu.setMaxDelay(delay)) {
                    // modified, super cheesey, see TruthAssignerMenu#clickMenuButton
                    this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, delay);
                }
            }
        } catch (NumberFormatException e) {
            this.maxDelay.setValue(Integer.toString(this.menu.getMaxDelay()));
        }
    }

    private void onBitsPress(Button  button) {
        this.menu.setNextBits();
        this.updateBits();
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -3);
    }

    private void onWaitPress(Button button) {
        this.menu.setWait(!this.menu.shouldWait());
        this.updateWait();
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -1);
    }

    private void onStartPress(Button button) {
        this.menu.start();
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, -2);
        this.updateFields();
    }

    private Component bitsTranslatable() {
        return Component.translatable("container.short_circuit.truth_assigner.bits", this.menu.getBits());
    }

    private String waitTranslationKey() {
        return "container.short_circuit.truth_assigner.wait" + (this.menu.shouldWait() ? ".on" : "");
    }

    private Component waitTranslatable() {
        return Component.translatable(this.waitTranslationKey());
    }

    private void updateFields() {
        this.start.active = !this.menu.isWorking() && !this.menu.isEmpty() && this.menu.getError() == 0;
        this.bits.active = !this.menu.isWorking();
        this.wait.active = !this.menu.isWorking();
        this.maxDelay.setEditable(!this.menu.isWorking());
        this.updateError();
        this.updateCurrentInput();
    }

    @Override
    public void slotChanged(AbstractContainerMenu menu, int index, ItemStack stack) {
        if (index == 0) {
            if (stack.isEmpty() || !stack.is(Items.CIRCUIT.get())) this.start.active = false;
            else this.updateFields();
        }
    }

    @Override
    public void dataChanged(AbstractContainerMenu menu, int index, int value) {
        if (index == 0 || index == 3) { // the "working" index
            this.updateFields();
        } else if (index == 1) {
            this.updateWait();
        } else if (index == 2) {
            this.maxDelay.setValue(Integer.toString(value));
        } else if (index == 4) {
            this.updateCurrentInput();
        } else if (index == 5) {
            this.updateBits();
        }
    }

    private void updateBits() {
        this.bits.setMessage(this.bitsTranslatable());
        this.bits.setTooltip(Component.translatable("container.short_circuit.truth_assigner.bits.desc", (int) Math.pow(2, this.menu.getBits())));
    }

    private void updateWait() {
        this.wait.setMessage(this.waitTranslatable());
        this.wait.setTooltip(Component.translatable(this.waitTranslationKey() + ".desc"));
    }

    private void updateError() {
        int errorCode = this.menu.getError();
        if (errorCode == 0) this.error.setMessage(Component.empty());
        else {
            //? if >=1.21.1 {
            this.error.setMessage(Component.translatable("container.short_circuit.truth_assigner.error." + errorCode).withColor(0xff0000));
            //? } else
            //this.error.setMessage(Component.translatable("container.short_circuit.truth_assigner.error." + errorCode).withStyle(Style.EMPTY.withColor(0xff0000)));
        }
    }

    private void updateCurrentInput() {
        if (!this.menu.isWorking()) this.currentInput.setTooltip((Component) null);
        else this.currentInput.setTooltip(Component.translatable("container.short_circuit.truth_assigner.current_input.desc", this.menu.getCurrentInput()));
    }
}
