package in.northwestw.shortcircuit.registries.items;

import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.Optional;

public class TooltipBlockItem extends BlockItem {
    public TooltipBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ItemTooltipComponent tooltip = new ItemTooltipComponent();

        if (stack.has(DataComponents.UUID.get())) {
            tooltip.add(Component.translatable("tooltip.short_circuit.circuit", stack.get(DataComponents.UUID.get()).uuid().toString()).withColor(0x7f7f7f));
        }
        if (stack.has(DataComponents.SHORT.get())) {
            DyeColor color = DyeColor.byId(stack.get(DataComponents.SHORT.get()));
            tooltip.add(Component.translatable("tooltip.short_circuit.circuit.color", Component.translatable("color.minecraft." + color.getName())).withColor(color.getTextColor()));
        }
        return tooltip.needed();
    }

    static class ItemTooltipComponent implements TooltipComponent, ClientTooltipComponent {
        private final List<Component> components;

        public ItemTooltipComponent() {
            this.components = Lists.newArrayList();
        }

        public void add(Component component) {
            this.components.add(component);
        }

        public Optional<TooltipComponent> needed() {
            if (this.components.isEmpty()) return Optional.empty();
            else return Optional.of(this);
        }

        @Override
        public int getHeight(Font font) {
            return font.lineHeight * this.components.size();
        }

        @Override
        public int getWidth(Font font) {
            return this.components.stream().mapToInt(font::width).max().orElse(0);
        }

        @Override
        public boolean showTooltipWithItemInHand() {
            return false;
        }

        @Override
        public void renderText(GuiGraphics graphics, Font font, int x, int y) {
            graphics.setComponentTooltipForNextFrame(font, this.components, x, y);
        }

        @Override
        public void renderImage(Font font, int x, int y, int width, int height, GuiGraphics guiGraphics) { }
    }
}
