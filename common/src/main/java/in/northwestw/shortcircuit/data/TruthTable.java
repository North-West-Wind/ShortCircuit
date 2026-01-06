package in.northwestw.shortcircuit.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.IntFunction;
import java.util.stream.Collectors;

// The goal is to reduce memory & storage by using more brain
public class TruthTable {
    public static final Codec<TruthTable> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BYTE.listOf().fieldOf("input").forGetter(table -> table.inputs.stream().map(RelativeDirection::getId).toList()),
            Codec.BYTE.listOf().fieldOf("output").forGetter(table -> table.outputs.stream().map(RelativeDirection::getId).toList()),
            Codec.INT.listOf().fieldOf("map").forGetter(TruthTable::flattenSignals),
            Codec.INT.fieldOf("defaultValue").forGetter(table -> table.defaultValue),
            Codec.BYTE.fieldOf("bits").forGetter(table -> (byte) table.bits)
    ).apply(instance, TruthTable::new));
    public static final Codec<WithUUID> CODEC_WITH_UUID = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("uuid").forGetter(merged -> merged.uuid),
            Codec.BYTE.listOf().fieldOf("input").forGetter(merged -> merged.table.inputs.stream().map(RelativeDirection::getId).toList()),
            Codec.BYTE.listOf().fieldOf("output").forGetter(merged -> merged.table.outputs.stream().map(RelativeDirection::getId).toList()),
            Codec.INT.listOf().fieldOf("map").forGetter(merged -> merged.table.flattenSignals()),
            Codec.INT.fieldOf("defaultValue").forGetter(merged -> merged.table.defaultValue),
            Codec.BYTE.fieldOf("bits").forGetter(merged -> (byte) merged.table.bits)
    ).apply(instance, WithUUID::new));

    public final List<RelativeDirection> inputs, outputs;
    public final Map<Integer, Integer> signals;
    public final int defaultValue, bits;

    public TruthTable(List<RelativeDirection> inputs, List<RelativeDirection> outputs, Map<Integer, Integer> signals, int defaultValue, int bits) {
        this.inputs = ImmutableList.copyOf(inputs);
        this.outputs = ImmutableList.copyOf(outputs);
        this.signals = ImmutableMap.copyOf(signals);
        this.defaultValue = defaultValue;
        if (bits == 0) this.bits = 4;
        else this.bits = bits;
    }

    public TruthTable(List<Byte> inputs, List<Byte> outputs, List<Integer> pairedSignals, int defaultValue, byte bits) {
        this.inputs = inputs.stream().map(RelativeDirection::fromId).toList();
        this.outputs = outputs.stream().map(RelativeDirection::fromId).toList();
        this.defaultValue = defaultValue;
        if (bits == 0) this.bits = 4;
        else this.bits = bits;
        Map<Integer, Integer> signalMap = Maps.newHashMap();
        for (int ii = 0; ii < pairedSignals.size() / 2; ii++)
            signalMap.put(pairedSignals.get(ii * 2), pairedSignals.get(ii * 2 + 1));
        this.signals = ImmutableMap.copyOf(signalMap);
    }

    public List<Integer> flattenSignals() {
        List<Integer> list = Lists.newArrayList();
        this.signals.forEach((input, output) -> {
            list.add(input);
            list.add(output);
        });
        return list;
    }

    public boolean isSame(List<RelativeDirection> inputs, List<RelativeDirection> outputs, Map<Integer, Integer> signals, int defaultValue, int bits) {
        // ensure all lists and maps have the same size
        if (this.inputs.size() == inputs.size() && this.outputs.size() == outputs.size() && this.signals.size() == signals.size() && this.defaultValue == defaultValue && this.bits == bits) {
            boolean same = true;
            // compare input lists
            for (int ii = 0; ii < inputs.size(); ii++)
                if (this.inputs.get(ii) != inputs.get(ii)) {
                    same = false;
                    break;
                }
            // compare output lists
            if (same) {
                for (int ii = 0; ii < outputs.size(); ii++)
                    if (this.outputs.get(ii) != outputs.get(ii)) {
                        same = false;
                        break;
                    }
                // compare maps
                if (same) {
                    for (Map.Entry<Integer, Integer> signalEntry : signals.entrySet()) {
                        int key = signalEntry.getKey();
                        if (!this.signals.containsKey(key) || !Objects.equals(this.signals.get(key), signalEntry.getValue())) {
                            same = false;
                            break;
                        }
                    }
                    // they are the same
                    return same;
                }
            }
        }
        return false;
    }

    public static class WithUUID {
        public final UUID uuid;
        public final TruthTable table;

        public WithUUID(UUID uuid, TruthTable table) {
            this.uuid = uuid;
            this.table = table;
        }

        public WithUUID(UUID uuid, List<Byte> inputs, List<Byte> outputs, List<Integer> pairedSignals, int defaultValue, byte bits) {
            this(uuid, new TruthTable(inputs, outputs, pairedSignals, defaultValue, bits));
        }
    }
}
