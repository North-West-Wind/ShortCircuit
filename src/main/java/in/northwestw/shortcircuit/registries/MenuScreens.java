package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.client.TruthAssignerScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@OnlyIn(Dist.CLIENT)
public class MenuScreens {
    public static void registerMenuScreens(IEventBus bus) {
        bus.addListener(MenuScreens::registerMenuScreen);
    }

    private static void registerMenuScreen(RegisterMenuScreensEvent event) {
        event.register(Menus.TRUTH_ASSIGNER.get(), TruthAssignerScreen::new);
    }
}
