package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class CircuitSavedData extends SavedData {
    private static final double LOG2 = Math.log(2);

    private final Map<Integer, Octolet> octolets;
    private final Set<Integer>[] octoletsBySize; // size order: 4, 8, 16, 32, 64, 128, 256
    private final Map<UUID, Integer> circuits;

    public CircuitSavedData() {
        this.octolets = Maps.newHashMap();
        this.octoletsBySize = new Set[7];
        for (int ii = 0; ii < 7; ii++) {
            this.octoletsBySize[ii] = Sets.newHashSet();
        }
        this.circuits = Maps.newHashMap();
    }

    public int nextIndexForSize(short blockSize) {
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
        return octolet.getStartingPos(outerIndex, uuid);
    }

    public void addOctolet(int index, Octolet octolet) {
        this.octolets.put(index, octolet);
        int sizeIndex = (int) (Math.log(octolet.blockSize) / LOG2) - 2;
        this.octoletsBySize[sizeIndex].add(index);
    }

    public static CircuitSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        CircuitSavedData data = new CircuitSavedData();
        for (Tag t : tag.getList("octolets", Tag.TAG_COMPOUND)) {
            CompoundTag tt = (CompoundTag) t;
            data.addOctolet(tt.getInt("key"), Octolet.fromTag(tt.getCompound("value")));
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
        return tag;
    }
}
