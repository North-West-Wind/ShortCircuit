package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.ShortCircuit;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class Tabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ShortCircuit.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHORT_CIRCUIT_TAB = CREATIVE_MODE_TABS.register("short_circuit", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.short_circuit"))
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> Items.CIRCUIT.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(Items.CIRCUIT.get());
                output.accept(Items.POKING_STICK.get());
            }).build());

    public static void registerTabs(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
