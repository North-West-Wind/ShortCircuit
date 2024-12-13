package in.northwestw.shortcircuit.data;

import com.google.common.collect.Sets;
import net.minecraft.nbt.*;

import java.util.Set;

// It's call Octolet because it is 2^8 for side
public class Octolet {
    public static final short MAX_SIZE = 256;

    public short blockSize;
    public Set<Integer> occupied;

    public Octolet() {
        this(MAX_SIZE);
    }

    public Octolet(short blockSize) {
        this.blockSize = blockSize;
        this.occupied = Sets.newHashSet();
    }

    public boolean isFull() {
        return occupied.size() == Math.pow(MAX_SIZE / this.blockSize, 3);
    }

    public static Octolet fromTag(CompoundTag tag) {
        Octolet octo = new Octolet();
        octo.load(tag);
        return octo;
    }

    public Octolet load(CompoundTag tag) {
        this.blockSize = tag.getShort("size");
        for (Tag t : tag.getList("occupied", Tag.TAG_INT))
            occupied.add(((IntTag) t).getAsInt());
        return this;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putShort("size", this.blockSize);
        ListTag list = new ListTag();
        occupied.forEach(s -> list.add(IntTag.valueOf(s)));
        tag.put("occupied", list);
        return tag;
    }
}
