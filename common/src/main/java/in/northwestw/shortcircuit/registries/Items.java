package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.platform.Services;
import in.northwestw.shortcircuit.registries.items.LabellingStickItem;
import in.northwestw.shortcircuit.registries.items.PokingStickItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.function.Supplier;

public class Items {
    public static final Supplier<BlockItem> CIRCUIT = registerSimpleBlockItem("circuit", Blocks.CIRCUIT);
    public static final Supplier<BlockItem> CIRCUIT_BOARD = registerSimpleBlockItem("circuit_board", Blocks.CIRCUIT_BOARD);
    public static final Supplier<BlockItem> TRUTH_ASSIGNER = registerSimpleBlockItem("truth_assigner", Blocks.TRUTH_ASSIGNER);
    public static final Supplier<BlockItem> INTEGRATED_CIRCUIT = registerSimpleBlockItem("integrated_circuit", Blocks.INTEGRATED_CIRCUIT);

    public static final Supplier<Item> POKING_STICK = Services.REGISTRY.registerItem("poking_stick", PokingStickItem::new, new Item.Properties().stacksTo(1));
    public static final Supplier<Item> LABELLING_STICK = Services.REGISTRY.registerItem("labelling_stick", LabellingStickItem::new, new Item.Properties().stacksTo(1));

    private static Supplier<BlockItem> registerSimpleBlockItem(String name, Supplier<Block> supplier) {
        return Services.REGISTRY.registerItem(name, properties -> new BlockItem(supplier.get(), properties), new Item.Properties());
    }

    public static void trigger() { }
}
