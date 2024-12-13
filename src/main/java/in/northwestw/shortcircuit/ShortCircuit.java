package in.northwestw.shortcircuit;

import in.northwestw.shortcircuit.registries.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(ShortCircuit.MOD_ID)
public class ShortCircuit {
    public static final String MOD_ID = "short_circuit";

    public ShortCircuit(IEventBus modEventBus, ModContainer modContainer) {
        BlockEntities.registerBlockEntities(modEventBus);
        Blocks.registerBlocks(modEventBus);
        DataComponents.registerDataComponentTypes(modEventBus);
        Items.registerItems(modEventBus);
        Tabs.registerTabs(modEventBus);
    }
}
