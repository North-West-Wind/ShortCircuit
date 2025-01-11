package in.northwestw.shortcircuit.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Map;
import java.util.Objects;

// The goal is to reduce memory & storage by using more brain
public class TruthTable {
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

    public static TruthTable load(CompoundTag tag) {
        List<RelativeDirection> input = Lists.newArrayList();
        for (byte id : tag.getByteArray("input"))
            input.add(RelativeDirection.fromId(id));
        List<RelativeDirection> output = Lists.newArrayList();
        for (byte id : tag.getByteArray("output"))
            output.add(RelativeDirection.fromId(id));
        Map<Integer, Integer> signalMap = Maps.newHashMap();
        int[] mergedMap = tag.getIntArray("map");
        for (int ii = 0; ii < mergedMap.length / 2; ii++)
            signalMap.put(mergedMap[ii * 2], mergedMap[ii * 2 + 1]);
        return new TruthTable(input, output, signalMap, tag.getInt("defaultValue"), tag.getByte("bits"));
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putByteArray("input", this.inputs.stream().map(RelativeDirection::getId).toList());
        tag.putByteArray("output", this.outputs.stream().map(RelativeDirection::getId).toList());
        List<Integer> mergedMap = Lists.newArrayList();
        this.signals.forEach((input, output) -> {
            mergedMap.add(input);
            mergedMap.add(output);
        });
        tag.putIntArray("map", mergedMap);
        tag.putInt("defaultValue", this.defaultValue);
        tag.putByte("bits", (byte) this.bits);
        return tag;
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
}
