package in.northwestw.shortcircuit.registries.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.UUID;

public record UUIDDataComponent(UUID uuid) {
    private static final Codec<UUIDDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.LONG.fieldOf("msb").forGetter(UUIDDataComponent::mostSignificantBits),
                    Codec.LONG.fieldOf("lsb").forGetter(UUIDDataComponent::leastSignificantBits)
            ).apply(instance, UUIDDataComponent::fromLongs));
    private static final StreamCodec<ByteBuf, UUIDDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, UUIDDataComponent::mostSignificantBits,
            ByteBufCodecs.VAR_LONG, UUIDDataComponent::leastSignificantBits,
            UUIDDataComponent::fromLongs
    );

    public static DataComponentType.Builder<UUIDDataComponent> getBuilder(DataComponentType.Builder<UUIDDataComponent> builder) {
        return builder.persistent(CODEC).networkSynchronized(STREAM_CODEC);
    }

    public static UUIDDataComponent fromLongs(long msb, long lsb) {
        return new UUIDDataComponent(new UUID(msb, lsb));
    }

    public long mostSignificantBits() {
        return this.uuid.getMostSignificantBits();
    }

    public long leastSignificantBits() {
        return this.uuid.getLeastSignificantBits();
    }
}