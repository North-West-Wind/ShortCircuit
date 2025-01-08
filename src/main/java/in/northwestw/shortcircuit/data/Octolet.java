package in.northwestw.shortcircuit.data;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.world.level.ChunkPos;

import java.util.Collection;
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
        this.blockSize = tag.getShort("value");
        for (Tag t : tag.getList("occupied", Tag.TAG_INT))
            occupied.add(((IntTag) t).getAsInt());
        for (Tag t : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag pair = (CompoundTag) t;
            this.blocks.put(pair.getUUID("uuid"), pair.getInt("id"));
        }
        return this;
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putShort("value", this.blockSize);
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

    public static BlockPos getOctoletPos(int outerIndex) {
        int quadrant = outerIndex % 4;
        outerIndex /= 4;
        int side = (int) Math.sqrt(outerIndex);
        int index = outerIndex - side * side;
        int ringSize = (side + 1) * (side + 1) - side * side; // must be odd
        int x, z;
        if (index < ringSize / 2) {
            x = index;
            z = side;
        } else if (index > ringSize / 2 + 1) {
            index -= ringSize / 2 + 1;
            x = side;
            z = index;
        } else {
            x = z = side;
        }

        return switch (quadrant) {
            case 0 -> new BlockPos(x * MAX_SIZE, 0, z * MAX_SIZE);
            case 1 -> new BlockPos(-(x + 1) * MAX_SIZE, 0, z * MAX_SIZE);
            case 2 -> new BlockPos(-(x + 1) * MAX_SIZE, 0, -(z + 1) * MAX_SIZE);
            case 3 -> new BlockPos(x * MAX_SIZE, 0, -(z + 1) * MAX_SIZE);
            default -> null;
        };
    }

    public BlockPos getStartingPos(int outerIndex, UUID uuid) {
        if (!this.blocks.containsKey(uuid)) return null;
        BlockPos octoletPos = Octolet.getOctoletPos(outerIndex);
        int index = this.blocks.get(uuid);
        int verticalBlocks = 256 / this.blockSize;
        if (this.blockSize > 16) {
            int chunkPerBlock = this.blockSize / 16;
            int chunkIndex = index / verticalBlocks;
            int sideLength = MAX_SIZE / 16;
            int chunkX = chunkIndex % sideLength;
            int chunkZ = (chunkIndex / sideLength) % sideLength;
            return octoletPos.offset(chunkX * chunkPerBlock * 16, (index % verticalBlocks) * this.blockSize, chunkZ * chunkPerBlock * 16);
        } else {
            int chunkBlocks = (16 / this.blockSize) * (16 / this.blockSize) * 256 / this.blockSize;
            int blockPerChunk = 16 / this.blockSize;
            int chunkIndex = index / chunkBlocks;
            int sideLength = MAX_SIZE / 16;
            int chunkX = chunkIndex % sideLength;
            int chunkZ = (chunkIndex / sideLength) % sideLength;
            int innerIndex = index % chunkBlocks;
            int x = innerIndex % blockPerChunk;
            int z = (innerIndex / blockPerChunk) % blockPerChunk;
            int y = (innerIndex / (blockPerChunk * blockPerChunk)) % blockPerChunk;
            return octoletPos.offset(chunkX * 16 + x * this.blockSize, y * this.blockSize, chunkZ * 16 + z * this.blockSize);
        }
    }

    public Set<ChunkPos> getLoadedChunks() {
        Set<ChunkPos> set = Sets.newHashSet();
        for (int index : this.occupied) set.addAll(this.getBlockChunk(index));
        return set;
    }

    public Set<ChunkPos> getBlockChunk(int index) {
        Set<ChunkPos> set = Sets.newHashSet();
        int sideLength = MAX_SIZE / 16;
        if (this.blockSize > 16) {
            int verticalBlocks = 256 / this.blockSize;
            int chunkPerBlock = this.blockSize / 16;
            index /= verticalBlocks;
            int x = index % sideLength;
            int z = (index / sideLength) % sideLength;
            for (int ii = 0; ii < this.blockSize / 16; ii++)
                for (int jj = 0; jj < this.blockSize / 16; jj++)
                    set.add(new ChunkPos(x * chunkPerBlock + ii, z * chunkPerBlock + jj));
        } else {
            int chunkBlocks = (16 / this.blockSize) * (16 / this.blockSize) * 256 / this.blockSize;
            index /= chunkBlocks;
            int x = index % sideLength;
            int z = (index / sideLength) % sideLength;
            set.add(new ChunkPos(x, z));
        }
        return set;
    }

    public void insertNewBlock(UUID uuid) {
        if (this.blocks.containsKey(uuid)) return;
        for (int ii = 0; ii < this.occupied.size(); ii++) {
            if (!this.occupied.contains(ii)) {
                this.blocks.put(uuid, ii);
                this.occupied.add(ii);
                return;
            }
        }
        int nextIndex = this.occupied.size();
        this.blocks.put(uuid, nextIndex);
        this.occupied.add(nextIndex);
    }

    public void removeBlock(UUID uuid) {
        if (!this.blocks.containsKey(uuid)) return;
        this.occupied.remove(this.blocks.get(uuid));
        this.blocks.remove(uuid);

    }
}
