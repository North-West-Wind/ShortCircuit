package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.platform.Services;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

import java.util.function.Supplier;

public class Tabs {
    public static final Supplier<CreativeModeTab> SHORT_CIRCUIT_TAB = Services.REGISTRY.registerCreativeModeTab(
            "short_circuit",
            Component.translatable("itemGroup.short_circuit"),
            () -> Items.CIRCUIT.get().getDefaultInstance(),
            Items.CIRCUIT, Items.POKING_STICK, Items.TRUTH_ASSIGNER);

    public static void trigger() { }
}
