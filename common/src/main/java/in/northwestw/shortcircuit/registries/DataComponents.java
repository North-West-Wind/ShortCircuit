package in.northwestw.shortcircuit.registries;

import com.mojang.serialization.Codec;
import in.northwestw.shortcircuit.platform.Services;
import in.northwestw.shortcircuit.registries.datacomponents.LastPosDataComponent;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.function.Supplier;

public class DataComponents {
    public static final Supplier<DataComponentType<UUIDDataComponent>> UUID = Services.REGISTRY.registerDataComponent("uuid", UUIDDataComponent::getBuilder);
    public static final Supplier<DataComponentType<Short>> SHORT = createShortDataComponent("short");
    public static final Supplier<DataComponentType<LastPosDataComponent>> LAST_POS = Services.REGISTRY.registerDataComponent("last_pos", LastPosDataComponent::getBuilder);
    public static final Supplier<DataComponentType<Boolean>> BIT = Services.REGISTRY.registerDataComponent("bit", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    private static Supplier<DataComponentType<Short>> createShortDataComponent(String name) {
        return Services.REGISTRY.registerDataComponent(name, builder -> builder.persistent(Codec.SHORT).networkSynchronized(ByteBufCodecs.SHORT));
    }

    public static void trigger() { }
}
