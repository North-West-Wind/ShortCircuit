package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.ShortCircuit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CircuitSavedData extends SavedData {
    private static final double LOG2 = Math.log(2);

    public final Map<Integer, Octolet> octolets;
    public final Set<Integer>[] octoletsBySize; // value order: 4, 8, 16, 32, 64, 128, 256
    public final Map<UUID, Integer> circuits;

    public CircuitSavedData() {
        this.octolets = Maps.newHashMap();
        this.octoletsBySize = new Set[7];
        for (int ii = 0; ii < 7; ii++) {
            this.octoletsBySize[ii] = Sets.newHashSet();
        }
        this.circuits = Maps.newHashMap();
    }

    public int octoletIndexForSize(short blockSize) {
        int sizeIndex = (int) (Math.log(blockSize) / LOG2) - 2;
        for (int octoIndex : this.octoletsBySize[sizeIndex]) {
            Octolet octo = this.octolets.get(octoIndex);
            if (!octo.isFull()) return octoIndex;
        }
        for (int ii = 0; ii < this.octolets.size(); ii++) {
            if (!this.octolets.containsKey(ii)) return ii;
        }
        return this.octolets.size();
    }

    public Octolet getParentOctolet(UUID uuid) {
        if (!this.circuits.containsKey(uuid)) return null;
        return this.octolets.get(this.circuits.get(uuid));
    }

    public BlockPos getCircuitStartingPos(UUID uuid) {
        if (!this.circuits.containsKey(uuid)) return null;
        int outerIndex = this.circuits.get(uuid);
        Octolet octolet = this.octolets.get(outerIndex);
        //ShortCircuit.LOGGER.debug("Octolet {} starts at {}", outerIndex, Octolet.getOctoletPos(outerIndex));
        return octolet.getStartingPos(outerIndex, uuid);
    }

    public void addOctolet(int index, Octolet octolet) {
        this.octolets.put(index, octolet);
        int sizeIndex = (int) (Math.log(octolet.blockSize) / LOG2) - 2;
        this.octoletsBySize[sizeIndex].add(index);
        this.setDirty();
    }

    public void addCircuit(UUID uuid, int octoletIndex) {
        Octolet octolet = this.octolets.get(octoletIndex);
        octolet.insertNewBlock(uuid);
        this.circuits.put(uuid, octoletIndex);
        this.setDirty();
    }

    public void removeCircuit(UUID uuid) {
        if (!this.circuits.containsKey(uuid)) return;
        int outerIndex = this.circuits.get(uuid);
        Octolet octolet = this.octolets.get(outerIndex);
        octolet.removeBlock(uuid);
    }

    public static CircuitSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        CircuitSavedData data = new CircuitSavedData();
        for (Tag t : tag.getList("octolets", Tag.TAG_COMPOUND)) {
            CompoundTag tt = (CompoundTag) t;
            data.addOctolet(tt.getInt("key"), Octolet.fromTag(tt.getCompound("value")));
        }
        for (Tag t : tag.getList("circuits", Tag.TAG_COMPOUND)) {
            CompoundTag tt = (CompoundTag) t;
            data.circuits.put(tt.getUUID("key"), tt.getInt("value"));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        this.octolets.forEach((integer, octolet) -> {
            CompoundTag pair = new CompoundTag();
            pair.putInt("key", integer);
            CompoundTag octoTag = new CompoundTag();
            pair.put("value", octolet.save(octoTag));
            list.add(pair);
        });
        tag.put("octolets", list);
        ListTag circuitList = new ListTag();
        this.circuits.forEach((uuid, index) -> {
            CompoundTag pair = new CompoundTag();
            pair.putUUID("key", uuid);
            pair.putInt("value", index);
            circuitList.add(pair);
        });
        tag.put("circuits", circuitList);
        return tag;
    }

    public static CircuitSavedData getCircuitBoardData(ServerLevel level) {
        ServerLevel circuitBoardLevel = level.getServer().getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        DimensionDataStorage storage = circuitBoardLevel.getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(CircuitSavedData::new, CircuitSavedData::load), "circuit_pos");
    }

    public static CircuitSavedData getRuntimeData(ServerLevel level) {
        ServerLevel runtimeLevel = level.getServer().getLevel(Constants.RUNTIME_DIMENSION);
        DimensionDataStorage storage = runtimeLevel.getDataStorage();
        return storage.computeIfAbsent(new SavedData.Factory<>(CircuitSavedData::new, CircuitSavedData::load), "circuit_pos");
    }
}
