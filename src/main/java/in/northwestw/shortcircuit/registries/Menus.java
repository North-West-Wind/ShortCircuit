package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.menus.TruthAssignerMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class Menus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ShortCircuit.MOD_ID);
    public static final Supplier<MenuType<TruthAssignerMenu>> TRUTH_ASSIGNER = MENUS.register("truth_assigner", () -> new MenuType<>(TruthAssignerMenu::new, FeatureFlags.DEFAULT_FLAGS));

    public static void registerMenus(IEventBus bus) {
        MENUS.register(bus);
    }
}
