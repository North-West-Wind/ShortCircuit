package in.northwestw.shortcircuit.data;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import org.antlr.v4.runtime.misc.Triple;

import java.util.*;
import java.util.stream.Collectors;

public class TruthTableSavedData extends SavedData {
    private final Map<UUID, Triple<List<RelativeDirection>, List<RelativeDirection>, Map<Integer, Integer>>> truthTables;

    public TruthTableSavedData() {
        this.truthTables = Maps.newHashMap();
    }

    public static TruthTableSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        TruthTableSavedData data = new TruthTableSavedData();
        for (Tag tt : tag.getList("tables", Tag.TAG_COMPOUND)) {
            CompoundTag tuple = (CompoundTag) tt;
            UUID uuid = tuple.getUUID("uuid");
            List<RelativeDirection> input = Lists.newArrayList();
            for (byte id : tuple.getByteArray("input"))
                input.add(RelativeDirection.fromId(id));
            List<RelativeDirection> output = Lists.newArrayList();
            for (byte id : tuple.getByteArray("output"))
                output.add(RelativeDirection.fromId(id));
            Map<Integer, Integer> signalMap = Maps.newHashMap();
            int[] mergedMap = tuple.getIntArray("map");
            for (int ii = 0; ii < mergedMap.length / 2; ii++)
                signalMap.put(mergedMap[ii * 2], mergedMap[ii * 2 + 1]);
            data.truthTables.put(uuid, new Triple<>(input, output, signalMap));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        this.truthTables.forEach((uuid, triple) -> {
            CompoundTag tuple = new CompoundTag();
            tuple.putUUID("uuid", uuid);
            tuple.putByteArray("input", triple.a.stream().map(RelativeDirection::getId).toList());
            tuple.putByteArray("output", triple.b.stream().map(RelativeDirection::getId).toList());
            List<Integer> mergedMap = Lists.newArrayList();
            triple.c.forEach((input, output) -> {
                mergedMap.add(input);
                mergedMap.add(output);
            });
            tuple.putIntArray("map", mergedMap);
            list.add(tuple);
        });
        tag.put("tables", list);
        return tag;
    }

    public Map<RelativeDirection, Integer> getSignals(UUID uuid, Map<RelativeDirection, Integer> inputs) {
        Map<RelativeDirection, Integer> signals = Maps.newHashMap();
        if (!this.truthTables.containsKey(uuid)) return signals;
        Triple<List<RelativeDirection>, List<RelativeDirection>, Map<Integer, Integer>> triple = this.truthTables.get(uuid);
        int input = 0;
        for (RelativeDirection dir : triple.a) {
            input <<= 4;
            input |= inputs.getOrDefault(dir, 0);
        }
        int output = triple.c.getOrDefault(input, 0);
        for (RelativeDirection dir: triple.b) {
            signals.put(dir, output & 0xF);
            output >>= 4;
        }
        return signals;
    }

    public UUID insertTruthTable(UUID uuid, List<RelativeDirection> inputs, List<RelativeDirection> outputs, Map<Integer, Integer> signals) {
        // find if the truth table repeats
        for (Map.Entry<UUID, Triple<List<RelativeDirection>, List<RelativeDirection>, Map<Integer, Integer>>> entry : this.truthTables.entrySet()) {
            Triple<List<RelativeDirection>, List<RelativeDirection>, Map<Integer, Integer>> triple = entry.getValue();
            // ensure all lists and maps have the same size
            if (triple.a.size() == inputs.size() && triple.b.size() == outputs.size() && triple.c.size() == signals.size()) {
                boolean same = true;
                // compare input lists
                for (int ii = 0; ii < inputs.size(); ii++)
                    if (triple.a.get(ii) != inputs.get(ii)) {
                        same = false;
                        break;
                    }
                // compare output lists
                if (same) {
                    for (int ii = 0; ii < outputs.size(); ii++)
                        if (triple.b.get(ii) != outputs.get(ii)) {
                            same = false;
                            break;
                        }
                    // compare maps
                    if (same) {
                        for (Map.Entry<Integer, Integer> signalEntry : signals.entrySet()) {
                            int key = signalEntry.getKey();
                            if (!triple.c.containsKey(key) || !Objects.equals(triple.c.get(key), signalEntry.getValue())) {
                                same = false;
                                break;
                            }
                        }
                        // they are the same
                        if (same) return entry.getKey();
                    }
                }
            }
        }
        this.truthTables.put(uuid, new Triple<>(ImmutableList.copyOf(inputs), ImmutableList.copyOf(outputs), ImmutableMap.copyOf(signals)));
        this.setDirty();
        return uuid;
    }

    public static TruthTableSavedData getTruthTableData(ServerLevel level) {
        ServerLevel circuitBoardLevel = level.getServer().getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        DimensionDataStorage storage = circuitBoardLevel.getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(TruthTableSavedData::new, TruthTableSavedData::load), "truth_table");
    }
}
