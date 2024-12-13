package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.Map;
import java.util.Set;

public class CircuitSavedData extends SavedData {
    private static final double LOG2 = Math.log(2);

    private final Map<Integer, Octolet> octolets;
    private final Set<Integer>[] octoletsBySize; // size order: 4, 8, 16, 32, 64, 128, 256

    public CircuitSavedData() {
        this.octolets = Maps.newLinkedHashMap();
        this.octoletsBySize = new Set[7];
        for (int ii = 0; ii < 7; ii++) {
            this.octoletsBySize[ii] = Sets.newHashSet();
        }
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

    public CircuitSavedData load(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        CircuitSavedData data = new CircuitSavedData();
        for (Tag t : tag.getList("octolets", Tag.TAG_COMPOUND)) {
            CompoundTag tt = (CompoundTag) t;
            int index = tt.getInt("key");
            Octolet octo = Octolet.fromTag(tt.getCompound("value"));
            octolets.put(index, octo);
            int sizeIndex = (int) (Math.log(octo.blockSize) / LOG2) - 2;
            octoletsBySize[sizeIndex].add(index);
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
