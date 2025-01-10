package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.items.PokingStickItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Items {
    private static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ShortCircuit.MOD_ID);

    public static final DeferredItem<BlockItem> CIRCUIT = ITEMS.registerSimpleBlockItem(Blocks.CIRCUIT);
    public static final DeferredItem<BlockItem> CIRCUIT_BOARD = ITEMS.registerSimpleBlockItem(Blocks.CIRCUIT_BOARD);
    public static final DeferredItem<BlockItem> TRUTH_ASSIGNER = ITEMS.registerSimpleBlockItem(Blocks.TRUTH_ASSIGNER);
    public static final DeferredItem<BlockItem> INTEGRATED_CIRCUIT = ITEMS.registerSimpleBlockItem(Blocks.INTEGRATED_CIRCUIT);

    public static final DeferredItem<Item> POKING_STICK = ITEMS.registerItem("poking_stick", PokingStickItem::new, new Item.Properties().stacksTo(1));

    public static void registerItems(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
