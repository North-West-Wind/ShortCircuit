package in.northwestw.shortcircuit.registries.blockentities;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.config.Config;
import in.northwestw.shortcircuit.data.CircuitSavedData;
import in.northwestw.shortcircuit.data.Octolet;
import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.blockentities.common.CircuitProperties;
import in.northwestw.shortcircuit.registries.blockentities.common.CommonCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.CircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

public class CircuitBlockEntity extends CommonCircuitBlockEntity {
    private static final long MAX_TAG_BYTE_SIZE = 2097152L; // copied from NbtAccounter
    private UUID runtimeUuid, ownerUuid;
    private short blockSize, ticks;
    private boolean fake;
    private byte[] powers, inputs;
    public Map<BlockPos, BlockState> blocks; // 8x8x8
    // chunking
    private long chunkedOffset;
    private boolean chunked;

    public CircuitBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CIRCUIT.get(), pos, state);
        this.blocks = Maps.newTreeMap();
        this.runtimeUuid = UUID.randomUUID();
        this.powers = new byte[6];
        this.inputs = new byte[6];
    }

    @Override
    public void tick() {
        super.tick();
        if (this.shouldTick())
            this.updateInnerBlocks();
        // This should trigger getUpdateTag, which will figure out if inner blocks are chunked
        if (this.chunked && this.chunkedOffset != 0)
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    public boolean shouldTick() {
        this.ticks = (short) ((this.ticks + 1) % 100);
        return this.ticks == 1;
    }

    public void updateInnerBlocks() {
        if (this.hidden) return;
        MinecraftServer server = this.level.getServer();
        if (server == null) return;
        ServerLevel runtimeLevel = this.level.getServer().getLevel(Constants.RUNTIME_DIMENSION);
        if (runtimeLevel == null) return;
        CircuitSavedData data = CircuitSavedData.getRuntimeData(runtimeLevel);
        Octolet octolet = data.getParentOctolet(this.runtimeUuid);
        if (octolet == null) return;
        BlockPos startingPos = data.getCircuitStartingPos(this.runtimeUuid);
        this.blocks.clear();
        for (int ii = 1; ii < octolet.blockSize - 1; ii++) {
            for (int jj = 1; jj < octolet.blockSize - 1; jj++) {
                for (int kk = 1; kk < octolet.blockSize - 1; kk++) {
                    BlockState blockState = runtimeLevel.getBlockState(startingPos.offset(ii, jj, kk));
                    if (!blockState.isAir()) {
                        // ShortCircuit.LOGGER.debug("{} at {}, {}, {}", blockState, ii, jj, kk);
                        this.blocks.put(new BlockPos(ii - 1, jj - 1, kk - 1), blockState);
                    }
                }
            }
        }
        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && !this.fake;
    }

    public boolean isFake() {
        return fake;
    }

    public void resetRuntime() {
        this.runtimeUuid = UUID.randomUUID();
    }

    public RuntimeReloadResult reloadRuntime() {
        return this.reloadRuntime(Sets.newHashSet());
    }

    public RuntimeReloadResult reloadRuntime(Set<UUID> recurrence) {
        return this.reloadRuntimeAndModeMap(recurrence).getLeft();
    }

    public Pair<RuntimeReloadResult, Map<RelativeDirection, CircuitBoardBlock.Mode>> reloadRuntimeAndModeMap(Set<UUID> recurrence) {
        if (this.uuid == null) return this.emptyMapResult(RuntimeReloadResult.FAIL_NOT_EXIST);
        MinecraftServer server = this.level.getServer();
        if (server == null) return this.emptyMapResult(RuntimeReloadResult.FAIL_NO_SERVER);
        ServerLevel circuitBoardLevel = level.getServer().getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        ServerLevel runtimeLevel = level.getServer().getLevel(Constants.RUNTIME_DIMENSION);
        if (circuitBoardLevel == null || runtimeLevel == null) return this.emptyMapResult(RuntimeReloadResult.FAIL_NO_SERVER);
        CircuitSavedData boardData = CircuitSavedData.getCircuitBoardData(circuitBoardLevel);
        CircuitSavedData runtimeData = CircuitSavedData.getRuntimeData(runtimeLevel);
        BlockPos boardPos = boardData.getCircuitStartingPos(this.uuid);
        if (boardPos == null) return this.emptyMapResult(RuntimeReloadResult.FAIL_NOT_EXIST); // circuit doesn't exist yet. use the poking stick on it
        this.blockSize = boardData.getParentOctolet(this.uuid).blockSize;
        Octolet octolet = runtimeData.getParentOctolet(this.runtimeUuid);
        int octoletIndex = runtimeData.octoletIndexForSize(blockSize);
        if (octolet == null) {
            if (!runtimeData.octolets.containsKey(octoletIndex)) runtimeData.addOctolet(octoletIndex, new Octolet(this.blockSize));
            runtimeData.addCircuit(this.runtimeUuid, octoletIndex);
            octolet = runtimeData.octolets.get(octoletIndex);
        }
        recurrence.add(this.uuid);
        BlockPos start = Octolet.getOctoletPos(octoletIndex);
        for (ChunkPos pos : octolet.getLoadedChunks())
            runtimeLevel.setChunkForced(start.getX() / 16 + pos.x, start.getZ() / 16 + pos.z, true);
        BlockPos runtimePos = runtimeData.getCircuitStartingPos(this.runtimeUuid);
        Map<RelativeDirection, CircuitBoardBlock.Mode> modeMap = Maps.newHashMap();
        List<BlockPos> outputBlockPos = Lists.newArrayList();
        for (int ii = 0; ii < this.blockSize; ii++) {
            for (int jj = 0; jj < this.blockSize; jj++) {
                for (int kk = 0; kk < this.blockSize; kk++) {
                    BlockPos oldPos = boardPos.offset(ii, jj, kk);
                    BlockPos newPos = runtimePos.offset(ii, jj, kk);
                    BlockState oldState = circuitBoardLevel.getBlockState(oldPos);
                    runtimeLevel.setBlock(newPos, oldState, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS); // no neighbor update to prevent things from breaking
                    BlockEntity oldBlockEntity = circuitBoardLevel.getBlockEntity(oldPos);
                    if (oldBlockEntity != null) {
                        CompoundTag save = oldBlockEntity.saveWithoutMetadata();
                        BlockEntity be = runtimeLevel.getBlockEntity(newPos);
                        be.load(save);
                        if (be instanceof CircuitBoardBlockEntity blockEntity) {
                            RelativeDirection dir = oldState.getValue(CircuitBoardBlock.DIRECTION);
                            CircuitBoardBlock.Mode mode = oldState.getValue(CircuitBoardBlock.MODE);
                            if (modeMap.containsKey(dir)) {
                                CircuitBoardBlock.Mode existingMode = modeMap.get(dir);
                                if (mode != CircuitBoardBlock.Mode.NONE && existingMode != mode) {
                                    this.removeRuntime();
                                    return this.emptyMapResult(RuntimeReloadResult.FAIL_MULTI_MODE);
                                }
                            } else if (mode != CircuitBoardBlock.Mode.NONE)
                                modeMap.put(dir, mode);
                            blockEntity.setConnection(this.level.dimension(), this.getBlockPos(), this.runtimeUuid);
                            if (mode == CircuitBoardBlock.Mode.OUTPUT) outputBlockPos.add(newPos);
                        } else if (be instanceof CircuitBlockEntity blockEntity) {
                            if (recurrence.contains(blockEntity.getUuid())) {
                                this.removeRuntime();
                                return this.emptyMapResult(RuntimeReloadResult.FAIL_RECURRENCE);
                            } else {
                                blockEntity.resetRuntime();
                                RuntimeReloadResult result = blockEntity.reloadRuntime(recurrence);
                                if (!result.isGood()) {
                                    this.removeRuntime();
                                    return this.emptyMapResult(result);
                                }
                            }
                        }
                    }
                }
            }
        }
        // tick everything inside once
        for (int ii = 1; ii < this.blockSize - 1; ii++) {
            for (int jj = 1; jj < this.blockSize - 1; jj++) {
                for (int kk = 1; kk < this.blockSize - 1; kk++) {
                    BlockPos pos = runtimePos.offset(ii, jj, kk);
                    BlockState state = runtimeLevel.getBlockState(pos);
                    if (!state.isAir()) runtimeLevel.blockUpdated(pos, state.getBlock());
                }
            }
        }
        this.updateInputs();
        outputBlockPos.forEach(pos -> {
            BlockState state = runtimeLevel.getBlockState(pos);
            runtimeLevel.neighborChanged(pos, state.getBlock(), pos.relative(DirectionHelper.circuitBoardFixedDirection(state.getValue(CircuitBoardBlock.DIRECTION))));
        });
        this.chunkedOffset = 0;
        this.chunked = false;
        this.updateInnerBlocks();
        return Pair.of(RuntimeReloadResult.SUCCESS, modeMap);
    }

    private Pair<RuntimeReloadResult, Map<RelativeDirection, CircuitBoardBlock.Mode>> emptyMapResult(RuntimeReloadResult result) {
        return Pair.of(result, Maps.newHashMap());
    }

    public void updateRuntimeBlock(int signal, RelativeDirection direction) {
        MinecraftServer server = level.getServer();
        if (server == null) return;
        ServerLevel runtimeLevel = server.getLevel(Constants.RUNTIME_DIMENSION);
        if (runtimeLevel == null) return;
        CircuitSavedData data = CircuitSavedData.getRuntimeData(runtimeLevel);
        if (!data.circuits.containsKey(this.runtimeUuid)) return;
        int octoletIndex = data.octoletIndexForSize(blockSize);
        if (!data.octolets.containsKey(octoletIndex)) data.addOctolet(octoletIndex, new Octolet(blockSize));
        BlockPos startingPos = data.getCircuitStartingPos(this.runtimeUuid);
        if (startingPos == null) return;
        for (int ii = 0; ii < this.blockSize; ii++) {
            for (int jj = 0; jj < this.blockSize; jj++) {
                BlockPos pos = this.twoDimensionalRelativeDirectionOffset(startingPos, ii, jj, direction);
                BlockState state = runtimeLevel.getBlockState(pos);
                if (state.is(Blocks.CIRCUIT_BOARD.get()) && state.getValue(CircuitBoardBlock.MODE) == CircuitBoardBlock.Mode.INPUT)
                    runtimeLevel.setBlockAndUpdate(pos, state.setValue(CircuitBoardBlock.POWER, signal));
            }
        }
    }

    public void removeRuntime() {
        if (this.uuid == null) return;
        MinecraftServer server = this.level.getServer();
        if (server == null) return;
        ServerLevel runtimeLevel = level.getServer().getLevel(Constants.RUNTIME_DIMENSION);
        if (runtimeLevel == null) return;
        CircuitSavedData runtimeData = CircuitSavedData.getRuntimeData(runtimeLevel);
        Octolet octolet = runtimeData.getParentOctolet(this.runtimeUuid);
        if (octolet != null && octolet.blocks.containsKey(this.runtimeUuid)) {
            // we need to remove all recurrence, so may as well remove the blocks
            BlockPos runtimePos = runtimeData.getCircuitStartingPos(this.runtimeUuid);
            for (int ii = 0; ii < this.blockSize; ii++) {
                for (int jj = 0; jj < this.blockSize; jj++) {
                    for (int kk = 0; kk < this.blockSize; kk++) {
                        BlockPos pos = runtimePos.offset(ii, jj, kk);
                        if (runtimeLevel.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity)
                            blockEntity.removeRuntime();
                        runtimeLevel.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS); // no neighbor update to prevent things from breaking
                    }
                }
            }
            Set<ChunkPos> chunks = octolet.getBlockChunk(octolet.blocks.get(this.runtimeUuid));
            BlockPos start = Octolet.getOctoletPos(runtimeData.circuits.get(this.runtimeUuid));
            runtimeData.removeCircuit(this.runtimeUuid);
            Set<ChunkPos> newChunks = octolet.getLoadedChunks();
            for (ChunkPos chunk : chunks) {
                if (!newChunks.contains(chunk))
                    runtimeLevel.setChunkForced(start.getX() / 16 + chunk.x, start.getZ() / 16 + chunk.z, false);
            }
        }
    }

    private BlockPos twoDimensionalRelativeDirectionOffset(BlockPos pos, int ii, int jj, RelativeDirection direction) {
        return switch (direction) {
            case UP -> pos.offset(ii, this.blockSize - 1, jj);
            case DOWN -> pos.offset(ii, 0, jj);
            case RIGHT -> pos.offset(ii, jj, 0);
            case LEFT -> pos.offset(ii, jj, this.blockSize - 1);
            case FRONT -> pos.offset(0, ii, jj);
            case BACK -> pos.offset(this.blockSize - 1, ii, jj);
        };
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        this.runtimeUuid = tag.getUUID("runtimeUuid");
        this.blockSize = tag.getShort("blockSize");
        this.fake = tag.getBoolean("fake");
        this.powers = tag.getByteArray("powers");
        if (this.powers.length != 6) this.powers = new byte[6];
        this.inputs = tag.getByteArray("inputs");
        if (this.inputs.length != 6) this.inputs = new byte[6];
        if (!this.hidden) this.loadExtraFromData(tag);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putUUID("runtimeUuid", this.runtimeUuid);
        tag.putShort("blockSize", this.blockSize);
        tag.putBoolean("fake", this.fake);
        tag.putByteArray("powers", this.powers);
        tag.putByteArray("inputs", this.inputs);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        if (this.hidden) return tag;
        ListTag list = new ListTag(), testList = new ListTag();
        long size = tag.sizeInBytes() + (48 + 28 + 2 * 6 + 36) + (48 + 28 + 2 * 7 + 36 + 9);
        boolean broke = false, oldChunked = this.chunked;
        int ii = 0;
        for (Map.Entry<BlockPos, BlockState> entry : this.blocks.entrySet()) {
            if (ii++ < this.chunkedOffset) continue;
            CompoundTag tuple = new CompoundTag();
            tuple.put("pos", NbtUtils.writeBlockPos(entry.getKey()));
            tuple.put("block", NbtUtils.writeBlockState(entry.getValue()));
            testList.add(tuple);

            this.chunkedOffset++;
            if (testList.sizeInBytes() + size >= MAX_TAG_BYTE_SIZE) {
                this.chunked = true;
                broke = true;
                break;
            } else {
                list.add(tuple);
            }
        }
        // keep chunked true. will reset on reload
        if (!broke)
            this.chunkedOffset = 0;
        tag.put("blocks", list);
        // chunked only changes from false to true after first update of reload. we want to clear out old blocks
        tag.putBoolean("chunked", oldChunked == this.chunked && this.chunked);
        return tag;
    }

    public void loadExtraFromData(CompoundTag tag) {
        if (this.level == null) return;
        this.chunked = tag.getBoolean("chunked");
        Map<BlockPos, BlockState> blocks = Maps.newHashMap();
        for (Tag t : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag tuple = (CompoundTag) t;
            if (!tuple.contains("pos", Tag.TAG_COMPOUND)) continue;
            BlockPos pos = NbtUtils.readBlockPos(tuple.getCompound("pos"));
            BlockState state = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), tuple.getCompound("block"));
            blocks.put(pos, state);
        }
        if (!this.chunked) this.blocks = blocks;
        else this.blocks.putAll(blocks);
    }

    public UUID getRuntimeUuid() {
        return runtimeUuid;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
    }

    public short getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(short blockSize) {
        // If block size is cheesed, set to smallest
        if (blockSize > Config.MAX_CIRCUIT_SIZE) blockSize = 4;
        this.blockSize = blockSize;
        this.setChanged();
    }

    public void setFake(boolean fake) {
        this.fake = fake;
        this.setChanged();
    }

    public boolean matchRuntimeUuid(UUID uuid) {
        return this.runtimeUuid.equals(uuid);
    }

    public boolean setPower(int power, RelativeDirection direction) {
        byte oldPower = this.powers[direction.getId()];
        if (oldPower == power) return false;
        this.powers[direction.getId()] = (byte) power;
        BlockState state = this.getBlockState();
        boolean powered = false;
        for (byte pow : this.powers) {
            if (pow > 0) {
                powered = true;
                break;
            }
        }
        state = state.setValue(CircuitProperties.POWERED, powered);
        this.level.setBlock(this.getBlockPos(), state, Block.UPDATE_CLIENTS);
        this.setChanged();
        this.updateInnerBlocks();
        return true;
    }

    public int getPower(Direction direction) {
        switch (direction) {
            // this is so stupid. why is the direction of signals flipped!?
            case UP: return this.powers[RelativeDirection.DOWN.getId()];
            case DOWN: return this.powers[RelativeDirection.UP.getId()];
        }
        int data2d = this.getBlockState().getValue(HorizontalDirectionalBlock.FACING).get2DDataValue();
        int offset = direction.get2DDataValue() - data2d;
        if (offset < 0) offset += 4;
        return switch (offset) {
            case 0 -> this.powers[RelativeDirection.BACK.getId()];
            case 1 -> this.powers[RelativeDirection.LEFT.getId()];
            case 2 -> this.powers[RelativeDirection.FRONT.getId()];
            case 3 -> this.powers[RelativeDirection.RIGHT.getId()];
            default -> 0;
        };
    }

    public int getRelativePower(RelativeDirection direction) {
        return this.powers[direction.getId()];
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
            if (this.inputs[relDir.getId()] != signal) {
                this.sideUpdated(relDir);
                this.inputs[relDir.getId()] = (byte) signal;
                this.updateRuntimeBlock(signal, relDir);
            }
        }
    }

    public enum RuntimeReloadResult {
        SUCCESS("action.circuit.reload.success", true),
        FAIL_NO_SERVER("action.circuit.reload.fail.no_server", false),
        FAIL_NOT_EXIST("action.circuit.reload.fail.not_exist", false),
        FAIL_RECURRENCE("action.circuit.reload.fail.recurrence", false),
        FAIL_MULTI_MODE("action.circuit.reload.fail.multi_mode", false);

        final String translationKey;
        final boolean good;

        RuntimeReloadResult(String translationKey, boolean good) {
            this.translationKey = translationKey;
            this.good = good;
        }

        public String getTranslationKey() {
            return translationKey;
        }

        public boolean isGood() {
            return good;
        }
    }
}
