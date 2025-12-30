package in.northwestw.shortcircuit.registries.blockentities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import in.northwestw.shortcircuit.data.TruthTableSavedData;
import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.blockentities.common.CommonCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.IntegratedCircuitBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CommandBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class IntegratedCircuitBlockEntity extends CommonCircuitBlockEntity {
    private static final Block[] POSSIBLE_INNER_BLOCKS = new Block[] { Blocks.COMMAND_BLOCK, Blocks.CHAIN_COMMAND_BLOCK, Blocks.REPEATING_COMMAND_BLOCK };
    private final Map<RelativeDirection, Integer> inputs;
    private Map<RelativeDirection, Integer> outputs;
    private final boolean[] changed;
    // used for rendering
    public final List<BlockState> blocks;

    public IntegratedCircuitBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.INTEGRATED_CIRCUIT.get(), pos, state);
        this.inputs = Maps.newHashMap();
        this.outputs = Maps.newHashMap();
        this.changed = new boolean[6];
        this.hidden = true;
        this.blocks = Lists.newArrayList();
    }

    private void loadSignalMap(CompoundTag tag, String key, Map<RelativeDirection, Integer> map) {
        if (tag.contains(key, Tag.TAG_LIST))
            for (Tag t : tag.getList(key, Tag.TAG_COMPOUND)) {
                CompoundTag pair = (CompoundTag) t;
                map.put(RelativeDirection.fromId(pair.getByte("key")), (int) pair.getByte("value"));
            }
    }

    private void saveSignalMap(CompoundTag tag, String key, Map<RelativeDirection, Integer> map) {
        ListTag list = new ListTag();
        for (Map.Entry<RelativeDirection, Integer> entry : map.entrySet()) {
            CompoundTag pair = new CompoundTag();
            pair.putByte("key", entry.getKey().getId());
            pair.putByte("value", entry.getValue().byteValue());
            list.add(pair);
        }
        tag.put(key, list);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        UUID oldUuid = this.uuid;
        super.loadAdditional(tag, provider);
        this.loadSignalMap(tag, "inputs", this.inputs);
        this.loadSignalMap(tag, "outputs", this.outputs);
        // upgrade to v1.0.2, default hidden to true
        if (!tag.contains("hidden")) this.hidden = true;

        this.blocks.clear();
        if (oldUuid != this.uuid && this.uuid != null) {
            RandomSource random = new XoroshiroRandomSource(this.uuid.getLeastSignificantBits(), this.uuid.getMostSignificantBits());
            Direction[] directions = Direction.values();
            for (int ii = 0; ii < 8; ii++)
                this.blocks.add(POSSIBLE_INNER_BLOCKS[random.nextInt(POSSIBLE_INNER_BLOCKS.length)].defaultBlockState().setValue(CommandBlock.FACING, directions[random.nextInt(directions.length)]));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        this.saveSignalMap(tag, "inputs", this.inputs);
        this.saveSignalMap(tag, "outputs", this.outputs);
    }

    public int getPower(Direction direction) {
        switch (direction) {
            // this is so stupid. why is the direction of signals flipped!?
            case UP: return this.outputs.getOrDefault(RelativeDirection.DOWN, 0);
            case DOWN: return this.outputs.getOrDefault(RelativeDirection.UP, 0);
        }
        int data2d = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING).get2DDataValue();
        int offset = direction.get2DDataValue() - data2d;
        if (offset < 0) offset += 4;
        return switch (offset) {
            case 0 -> this.outputs.getOrDefault(RelativeDirection.BACK, 0);
            case 1 -> this.outputs.getOrDefault(RelativeDirection.LEFT, 0);
            case 2 -> this.outputs.getOrDefault(RelativeDirection.FRONT, 0);
            case 3 -> this.outputs.getOrDefault(RelativeDirection.RIGHT, 0);
            default -> 0;
        };
    }

    private void updateChangedNeighbors() {
        BlockState state = this.getBlockState();
        Direction direction = state.getValue(HorizontalDirectionalBlock.FACING);
        for (int ii = 0; ii < this.changed.length; ii++)
            if (this.changed[ii]) {
                BlockPos pos = this.getBlockPos().relative(DirectionHelper.relativeDirectionToFacing(RelativeDirection.fromId((byte) ii), direction));
                this.level.neighborChanged(pos, this.level.getBlockState(pos).getBlock(), null);
                this.level.updateNeighborsAtExceptFromFacing(this.getBlockPos(), state.getBlock(), direction.getOpposite(), null);
            }
    }

    private void updateOutput() {
        if (this.level instanceof ServerLevel level) {
            TruthTableSavedData data = TruthTableSavedData.getTruthTableData(level);
            Map<RelativeDirection, Integer> oldOutputs = ImmutableMap.copyOf(this.outputs);
            this.outputs = data.getSignals(this.uuid, this.inputs);
            this.clearChanged();
            for (RelativeDirection key : oldOutputs.keySet()) {
                if (!this.outputs.containsKey(key) || !this.outputs.get(key).equals(oldOutputs.get(key))) // removed value or changed value
                    this.changed[key.getId()] = true;
            }
            for (RelativeDirection key : this.outputs.keySet()) {
                if (!oldOutputs.containsKey(key)) // new value
                    this.changed[key.getId()] = true;
            }
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(IntegratedCircuitBlock.POWERED, this.outputs.values().stream().anyMatch(power -> power > 0)), Block.UPDATE_CLIENTS);
            this.updateChangedNeighbors();
        }
    }

    private void clearChanged() {
        Arrays.fill(this.changed, false);
    }

    @Override
    public void updateInputs() {
        // stop infinite updates when a side has ticked over n times
        if (this.maxUpdateReached()) return;
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();
        for (Direction direction : Direction.values()) {
            RelativeDirection relDir = DirectionHelper.directionToRelativeDirection(state.getValue(HorizontalDirectionalBlock.FACING), direction);
            int signal = level.getSignal(pos.relative(direction), direction);
            if (this.inputs.getOrDefault(relDir, 0) != signal)
                this.sideUpdated(relDir);
            this.inputs.put(relDir, signal);
        }
        this.updateOutput();
    }
}
