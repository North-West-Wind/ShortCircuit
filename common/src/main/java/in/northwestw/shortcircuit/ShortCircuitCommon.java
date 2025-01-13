package in.northwestw.shortcircuit;

import in.northwestw.shortcircuit.registries.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ShortCircuitCommon {
    public static final String MOD_ID = "short_circuit";
    public static final Logger LOGGER = LogManager.getLogger();

    public static void init() {
        BlockEntities.trigger();
        Blocks.trigger();
        Codecs.trigger();
        DataComponents.trigger();
        Items.trigger();
        Menus.trigger();
        SoundEvents.trigger();
        Tabs.trigger();
    }
}
