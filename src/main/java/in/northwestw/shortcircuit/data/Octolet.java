package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

// It's call Octolet because it is 2^8 for side
public class Octolet {
    public static final short MAX_SIZE = 256;

    public short blockSize;
    public Set<Integer> occupied;
    public Map<UUID, Integer> blocks;

    public Octolet() {
        this(MAX_SIZE);
    }

    public Octolet(short blockSize) {
        this.blockSize = blockSize;
        this.occupied = Sets.newHashSet();
        this.blocks = Maps.newHashMap();
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
        for (Tag t : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag pair = (CompoundTag) t;
            this.blocks.put(pair.getUUID("uuid"), pair.getInt("id"));
        }
        return this;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putShort("size", this.blockSize);
        ListTag list = new ListTag();
        occupied.forEach(s -> list.add(IntTag.valueOf(s)));
        tag.put("occupied", list);
        ListTag blockList = new ListTag();
        this.blocks.forEach((uuid, id) -> {
            CompoundTag pair = new CompoundTag();
            pair.putUUID("uuid", uuid);
            pair.putInt("id", id);
            blockList.add(pair);
        });
        tag.put("blocks", blockList);
        return tag;
    }

    public BlockPos getStartingPos(int outerIndex, UUID uuid) {
        if (!this.blocks.containsKey(uuid)) return null;
        // dry running with 2 rings. assume outerIndex is in range [4, 11]
        int diameter = (((int) Math.sqrt(outerIndex)) / 2 + 1) * 2; // sqrt returns either 2 or 3. diameter is 4 (side length)
        int quadrantSize = (diameter * diameter - (diameter - 2) * (diameter - 2)) / 4; // ring size / 4. 16 - 4 = 12. 12 / 4 = 3
        int ringIndex = outerIndex - (diameter - 2) * (diameter - 2); // index minus inner square. -= 4, new range [0, 11]
        byte quadrant = (byte) (ringIndex / quadrantSize); // 2d plane quadrant. [0, 3], [0, 2] -> 0, [3, 5] -> 1 ...
        int awayFromCorner = (ringIndex % quadrantSize) - quadrantSize / 2; // offset from corner block
        BlockPos quadrantCorner, octoletPos;
        switch (quadrant) {
            case 0: // + +
                quadrantCorner = new BlockPos(MAX_SIZE * diameter, 0, MAX_SIZE * diameter);
                // if offset is +, it's to the left of corner
                // if offset is -, it's to the down of corner
                octoletPos = quadrantCorner.offset(MAX_SIZE * -Math.max(0, awayFromCorner), 0, MAX_SIZE * Math.min(0, awayFromCorner));
                break;
            case 1: // - +
                quadrantCorner = new BlockPos(MAX_SIZE * -(diameter + 1), 0, MAX_SIZE * diameter);
                // if offset is +, it's to the down of corner
                // if offset is -, it's to the right of corner
                octoletPos = quadrantCorner.offset(MAX_SIZE * -Math.min(0, awayFromCorner), 0, MAX_SIZE * Math.max(0, awayFromCorner));
                break;
            case 2: // - -
                quadrantCorner = new BlockPos(MAX_SIZE * -(diameter + 1), 0, MAX_SIZE * -(diameter + 1));
                // if offset is +, it's to the right of corner
                // if offset is -, it's to the up of corner
                octoletPos = quadrantCorner.offset(MAX_SIZE * Math.max(0, awayFromCorner), 0, MAX_SIZE * -Math.min(0, awayFromCorner));
                break;
            case 3: // + -
                quadrantCorner = new BlockPos(MAX_SIZE * diameter, 0, MAX_SIZE * -(diameter + 1));
                // if offset is +, it's to the up of corner
                // if offset is -, it's to the left of corner
                octoletPos = quadrantCorner.offset(MAX_SIZE * Math.min(0, awayFromCorner), 0, MAX_SIZE * Math.max(0, awayFromCorner));
                break;
            default:
                // this should not be possible. something is wrong with the math
                octoletPos = null;
        }
        return octoletPos;
    }
}
