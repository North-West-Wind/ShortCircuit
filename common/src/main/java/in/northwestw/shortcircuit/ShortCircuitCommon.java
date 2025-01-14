package in.northwestw.shortcircuit;

import in.northwestw.shortcircuit.registries.*;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShortCircuitCommon {
    public static final String MOD_ID = "short_circuit";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        // blocks must be registered before block entities for fabric
        Blocks.trigger();
        BlockEntities.trigger();
        Codecs.trigger();
        DataComponents.trigger();
        Items.trigger();
        Menus.trigger();
        SoundEvents.trigger();
        Tabs.trigger();
    }

    public static ResourceLocation rl(String name) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, name);
    }
}
