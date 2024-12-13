package in.northwestw.shortcircuit.registries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class DataComponents {
    public static final Codec<UUIDDataComponent> UUID_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("msb").forGetter(UUIDDataComponent::mostSignificantBits),
                    Codec.LONG.fieldOf("lsb").forGetter(UUIDDataComponent::leastSignificantBits)
            ).apply(instance, UUIDDataComponent::fromLongs));
    public static final StreamCodec<ByteBuf, UUIDDataComponent> UUID_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, UUIDDataComponent::mostSignificantBits,
            ByteBufCodecs.VAR_LONG, UUIDDataComponent::leastSignificantBits,
            UUIDDataComponent::fromLongs
    );

    private static final DeferredRegister.DataComponents REGISTRAR = DeferredRegister.createDataComponents(ShortCircuit.MOD_ID);
    public static final Supplier<DataComponentType<UUIDDataComponent>> UUID = REGISTRAR.registerComponentType(
            "uuid",
            builder -> builder.persistent(UUID_CODEC).networkSynchronized(UUID_STREAM_CODEC)
    );

    public static void registerDataComponentTypes(IEventBus modEventBus) {
        REGISTRAR.register(modEventBus);
    }
}
