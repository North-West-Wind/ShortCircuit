package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.datacomponents.LastPosDataComponent;
import in.northwestw.shortcircuit.registries.datacomponents.ShortDataComponent;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DataComponents {
    private static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(ShortCircuit.MOD_ID);
    public static final Supplier<DataComponentType<UUIDDataComponent>> UUID = REGISTRAR.registerComponentType("uuid", UUIDDataComponent::getBuilder);
    public static final Supplier<DataComponentType<ShortDataComponent>> SHORT = REGISTRAR.registerComponentType("short", ShortDataComponent::getBuilder);
    public static final Supplier<DataComponentType<LastPosDataComponent>> LAST_POS = REGISTRAR.registerComponentType("last_pos", LastPosDataComponent::getBuilder);

    public static void registerDataComponentTypes(IEventBus modEventBus) {
        REGISTRAR.register(modEventBus);
    }
}
