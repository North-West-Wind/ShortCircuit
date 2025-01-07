package in.northwestw.shortcircuit;

import in.northwestw.shortcircuit.registries.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(ShortCircuit.MOD_ID)
public class ShortCircuit {
    public static final String MOD_ID = "short_circuit";
    public static final Logger LOGGER = LogManager.getLogger();

    public ShortCircuit(IEventBus modEventBus, ModContainer modContainer) {
        BlockEntities.registerBlockEntities(modEventBus);
        Blocks.registerBlocks(modEventBus);
        DataComponents.registerDataComponentTypes(modEventBus);
        EntityRenderers.registerEntityRenderers(modEventBus);
        Items.registerItems(modEventBus);
        Menus.registerMenus(modEventBus);
        MenuScreens.registerMenuScreens(modEventBus);
        Tabs.registerTabs(modEventBus);
    }
}
