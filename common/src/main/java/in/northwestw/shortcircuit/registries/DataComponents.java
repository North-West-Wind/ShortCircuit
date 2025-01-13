package in.northwestw.shortcircuit.registries;

import in.northwestw.shortcircuit.platform.Services;
import in.northwestw.shortcircuit.registries.datacomponents.LastPosDataComponent;
import in.northwestw.shortcircuit.registries.datacomponents.ShortDataComponent;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.component.DataComponentType;

import java.util.function.Supplier;

public class DataComponents {
    public static final Supplier<DataComponentType<UUIDDataComponent>> UUID = Services.REGISTRY.registerDataComponent("uuid", UUIDDataComponent::getBuilder);
    public static final Supplier<DataComponentType<ShortDataComponent>> SHORT = Services.REGISTRY.registerDataComponent("short", ShortDataComponent::getBuilder);
    public static final Supplier<DataComponentType<LastPosDataComponent>> LAST_POS = Services.REGISTRY.registerDataComponent("last_pos", LastPosDataComponent::getBuilder);

    public static void trigger() { }
}
