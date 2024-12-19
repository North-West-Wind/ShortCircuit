package in.northwestw.shortcircuit.registries.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ShortDataComponent(short value) {
    private static final Codec<ShortDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.SHORT.fieldOf("value").forGetter(ShortDataComponent::value)
            ).apply(instance, ShortDataComponent::new));
    private static final StreamCodec<ByteBuf, ShortDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.SHORT, ShortDataComponent::value,
            ShortDataComponent::new
    );

    public static DataComponentType.Builder<ShortDataComponent> getBuilder(DataComponentType.Builder<ShortDataComponent> builder) {
        return builder.persistent(CODEC).networkSynchronized(STREAM_CODEC);
    }
}
