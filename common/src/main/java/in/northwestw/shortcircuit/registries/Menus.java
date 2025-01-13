package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.platform.Services;
import in.northwestw.shortcircuit.registries.menus.TruthAssignerMenu;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

import java.util.function.Supplier;

public class Menus {
    public static final Supplier<MenuType<TruthAssignerMenu>> TRUTH_ASSIGNER = Services.REGISTRY.registerMenu("truth_assigner", TruthAssignerMenu::new, FeatureFlags.DEFAULT_FLAGS);

    public static void trigger() { }
}
