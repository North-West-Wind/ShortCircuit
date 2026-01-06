package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TruthTableSavedData extends SavedData {
    public static final Codec<TruthTableSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.withAlternative(
                    Codec.unboundedMap(UUIDUtil.CODEC, TruthTable.CODEC),
                    TruthTable.CODEC_WITH_UUID.listOf().xmap(list -> {
                        Map<UUID, TruthTable> map = Maps.newHashMap();
                        for (TruthTable.WithUUID withUUID : list)
                            map.put(withUUID.uuid, withUUID.table);
                        return map;
                    }, map -> map.entrySet().stream().map(entry -> new TruthTable.WithUUID(entry.getKey(), entry.getValue())).toList())
            ).fieldOf("tables").forGetter(data -> data.truthTables)
    ).apply(instance, TruthTableSavedData::new));
    public static final SavedDataType<TruthTableSavedData> TYPE = new SavedDataType<>("truth_table", TruthTableSavedData::new, CODEC, null);
    private final Map<UUID, TruthTable> truthTables;

    public TruthTableSavedData() {
        this(Maps.newHashMap());
    }

    public TruthTableSavedData(Map<UUID, TruthTable> truthTables) {
        this.truthTables = truthTables;
    }

    public Map<RelativeDirection, Integer> getSignals(UUID uuid, Map<RelativeDirection, Integer> inputs) {
        Map<RelativeDirection, Integer> signals = Maps.newHashMap();
        if (!this.truthTables.containsKey(uuid)) return signals;
        TruthTable table = this.truthTables.get(uuid);
        int input = 0;
        for (RelativeDirection dir : table.inputs) {
            input <<= table.bits;
            // merge 4-bit into amount specified by table.bits
            // i haven't had time to look into the mathematical relationships yet
            int val = inputs.getOrDefault(dir, 0);
            if (table.bits == 4) input |= val;
            else if (table.bits == 2) input |= (((val >> 2) > 1 ? 1 : 0) << 1) | ((val & 0x3) > 1 ? 1 : 0);
            else input |= val > 0 ? 1 : 0;
        }
        int output = table.signals.getOrDefault(input, table.defaultValue);
        for (RelativeDirection dir: table.outputs.reversed()) {
            signals.put(dir, output & 0xF);
            output >>= 4;
        }
        return signals;
    }

    public UUID insertTruthTable(UUID uuid, List<RelativeDirection> inputs, List<RelativeDirection> outputs, Map<Integer, Integer> signals, int bits) {
        // optimization
        Map<Integer, Integer> reverseMapCount = Maps.newHashMap();
        for (int output: signals.values()) {
            int count = reverseMapCount.getOrDefault(output, 0);
            reverseMapCount.put(output, count + 1);
        }
        int defaultValue = reverseMapCount.entrySet().stream().max(Comparator.comparingInt(Map.Entry::getValue)).get().getKey();
        Map<Integer, Integer> optimizedMap = Maps.newHashMap();
        for (Map.Entry<Integer, Integer> entry : signals.entrySet())
            if (entry.getValue() != defaultValue)
                optimizedMap.put(entry.getKey(), entry.getValue());
        
        // find if the truth table repeats
        for (Map.Entry<UUID, TruthTable> entry : this.truthTables.entrySet()) {
            if (entry.getValue().isSame(inputs, outputs, optimizedMap, defaultValue, bits))
                return entry.getKey();
        }
        this.truthTables.put(uuid, new TruthTable(inputs, outputs, optimizedMap, defaultValue, bits));
        this.setDirty();
        return uuid;
    }

    public static TruthTableSavedData getTruthTableData(ServerLevel level) {
        ServerLevel circuitBoardLevel = level.getServer().getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        DimensionDataStorage storage = circuitBoardLevel.getDataStorage();
        return storage.computeIfAbsent(TYPE);
    }
}
