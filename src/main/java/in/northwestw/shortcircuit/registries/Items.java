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

    public static final DeferredItem<BlockItem> CIRCUIT = ITEMS.registerSimpleBlockItem(Blocks.CIRCUIT, new Item.Properties());

    public static final DeferredItem<Item> POKING_STICK = ITEMS.registerItem("poking_stick", PokingStickItem::new);

    public static void registerItems(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
