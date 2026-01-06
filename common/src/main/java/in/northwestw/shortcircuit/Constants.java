package in.northwestw.shortcircuit;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class Constants {
    public static final ResourceKey<Level> CIRCUIT_BOARD_DIMENSION = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(ShortCircuitCommon.MOD_ID, "circuit_board"));
    public static final ResourceKey<Level> RUNTIME_DIMENSION = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(ShortCircuitCommon.MOD_ID, "runtime"));

    public static <K, V> Codec<List<Pair<K, V>>> pairMapCodec(Codec<K> keyCodec, Codec<V> valueCodec, String keyField, String valueField) {
        Codec<Pair<K, V>> pairCodec = RecordCodecBuilder.create(instance -> instance.group(
                keyCodec.fieldOf(keyField).forGetter(Pair::getLeft),
                valueCodec.fieldOf(valueField).forGetter(Pair::getRight)
        ).apply(instance, Pair::of));
        return pairCodec.listOf();
    }
}
