package in.northwestw.shortcircuit.data;

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

import java.util.List;
import java.util.Map;
import java.util.UUID;
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

    public static TruthTableSavedData getTruthTableData(ServerLevel level) {
        ServerLevel circuitBoardLevel = level.getServer().getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        DimensionDataStorage storage = circuitBoardLevel.getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(TruthTableSavedData::new, TruthTableSavedData::load), "truth_table");
    }
}
