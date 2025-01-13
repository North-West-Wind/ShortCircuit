package in.northwestw.shortcircuit;

import in.northwestw.shortcircuit.registries.*;
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
}
