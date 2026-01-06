package in.northwestw.shortcircuit;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

public class Constants {
    public static final ResourceKey<Level> CIRCUIT_BOARD_DIMENSION = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(ShortCircuitCommon.MOD_ID, "circuit_board"));
    public static final ResourceKey<Level> RUNTIME_DIMENSION = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(ShortCircuitCommon.MOD_ID, "runtime"));

    public static <K, V> Codec<Map<K, V>> backwardsCompatMapCodec(Codec<K> keyCodec, Codec<V> valueCodec, String keyField, String valueField) {
        Codec<Pair<K, V>> pairCodec = RecordCodecBuilder.create(instance -> instance.group(
                keyCodec.fieldOf(keyField).forGetter(Pair::getLeft),
                valueCodec.fieldOf(valueField).forGetter(Pair::getRight)
        ).apply(instance, Pair::of));
        return pairCodec.listOf().xmap(list -> {
            Map<K, V> map = Maps.newHashMap();
            for (Pair<K, V> pair : list)
                map.put(pair.getLeft(), pair.getRight());
            return map;
        }, map -> map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList());
    }
}
