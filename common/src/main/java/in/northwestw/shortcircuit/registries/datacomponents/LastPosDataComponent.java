package in.northwestw.shortcircuit.registries.datacomponents;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record LastPosDataComponent(Identifier rl, Vec3 pos) {
    private static final Codec<LastPosDataComponent> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("dimension").forGetter(component -> component.rl.toString()),
                    Codec.DOUBLE.fieldOf("x").forGetter(component -> component.pos().x),
                    Codec.DOUBLE.fieldOf("y").forGetter(component -> component.pos().y),
                    Codec.DOUBLE.fieldOf("z").forGetter(component -> component.pos().z)
            ).apply(instance, LastPosDataComponent::new));
    private static final StreamCodec<ByteBuf, LastPosDataComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, component -> component.rl.toString(),
            ByteBufCodecs.DOUBLE, component -> component.pos().x,
            ByteBufCodecs.DOUBLE, component -> component.pos().y,
            ByteBufCodecs.DOUBLE, component -> component.pos().z,
            LastPosDataComponent::new
    );

    private LastPosDataComponent(String rl, double x, double y, double z) {
        this(Identifier.parse(rl), new Vec3(x, y, z));
    }

    public static DataComponentType.Builder<LastPosDataComponent> getBuilder(DataComponentType.Builder<LastPosDataComponent> builder) {
        return builder.persistent(CODEC).networkSynchronized(STREAM_CODEC);
    }
}
