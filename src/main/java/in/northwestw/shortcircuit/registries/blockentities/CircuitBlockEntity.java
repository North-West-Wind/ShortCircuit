package in.northwestw.shortcircuit.registries.blockentities;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.data.CircuitSavedData;
import in.northwestw.shortcircuit.data.Octolet;
import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.blocks.CircuitBlock;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class CircuitBlockEntity extends BlockEntity {
    private UUID uuid, runtimeUuid;
    private short blockSize, ticks;
    private boolean hidden;
    private byte[] powers;
    public Map<BlockPos, BlockState> blocks; // 8x8x8

    public CircuitBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CIRCUIT.get(), pos, state);
        this.blocks = Maps.newHashMap();
        this.runtimeUuid = UUID.randomUUID();
        this.powers = new byte[6];
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        if (t instanceof CircuitBlockEntity blockEntity && blockEntity.shouldTick())
            blockEntity.updateInnerBlocks();
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
                    if (!blockState.isEmpty()) {
                        // ShortCircuit.LOGGER.debug("{} at {}, {}, {}", blockState, ii, jj, kk);
                        this.blocks.put(new BlockPos(ii - 1, jj - 1, kk - 1), blockState);
                    }
                }
            }
        }
        level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    public boolean isValid() {
        return this.uuid != null;
    }

    public void resetRuntime() {
        this.runtimeUuid = UUID.randomUUID();
    }

    public RuntimeReloadResult reloadRuntime() {
        return this.reloadRuntime(Sets.newHashSet());
    }

    public RuntimeReloadResult reloadRuntime(Set<UUID> recurrence) {
        if (this.uuid == null) return RuntimeReloadResult.FAIL_NOT_EXIST;
        MinecraftServer server = this.level.getServer();
        if (server == null) return RuntimeReloadResult.FAIL_NO_SERVER;
        ServerLevel circuitBoardLevel = level.getServer().getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        ServerLevel runtimeLevel = level.getServer().getLevel(Constants.RUNTIME_DIMENSION);
        if (circuitBoardLevel == null || runtimeLevel == null) return RuntimeReloadResult.FAIL_NO_SERVER;
        CircuitSavedData boardData = CircuitSavedData.getCircuitBoardData(circuitBoardLevel);
        CircuitSavedData runtimeData = CircuitSavedData.getRuntimeData(runtimeLevel);
        BlockPos boardPos = boardData.getCircuitStartingPos(this.uuid);
        if (boardPos == null) return RuntimeReloadResult.FAIL_NOT_EXIST; // circuit doesn't exist yet. use the poking stick on it
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
        for (int ii = 0; ii < this.blockSize; ii++) {
            for (int jj = 0; jj < this.blockSize; jj++) {
                for (int kk = 0; kk < this.blockSize; kk++) {
                    BlockPos oldPos = boardPos.offset(ii, jj, kk);
                    BlockPos newPos = runtimePos.offset(ii, jj, kk);
                    runtimeLevel.setBlock(newPos, circuitBoardLevel.getBlockState(oldPos), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS); // no neighbor update to prevent things from breaking
                    BlockEntity oldBlockEntity = circuitBoardLevel.getBlockEntity(oldPos);
                    if (oldBlockEntity != null) {
                        CompoundTag save = oldBlockEntity.saveCustomOnly(circuitBoardLevel.registryAccess());
                        BlockEntity be = runtimeLevel.getBlockEntity(newPos);
                        be.loadCustomOnly(save, runtimeLevel.registryAccess());
                        if (be instanceof CircuitBoardBlockEntity blockEntity) {
                            blockEntity.setConnection(this.level.dimension(), this.getBlockPos(), this.runtimeUuid);
                        } else if (be instanceof CircuitBlockEntity blockEntity) {
                            if (recurrence.contains(blockEntity.getUuid())) {
                                this.removeRuntime();
                                return RuntimeReloadResult.FAIL_RECURRENCE;
                            } else {
                                blockEntity.resetRuntime();
                                RuntimeReloadResult result = blockEntity.reloadRuntime(recurrence);
                                if (!result.isGood()) {
                                    this.removeRuntime();
                                    return result;
                                }
                            }
                        }
                    }
                }
            }
        }
        this.getInputSignals();
        this.updateInnerBlocks();
        return RuntimeReloadResult.SUCCESS;
    }

    public void updateRuntimeBlock(int signal, RelativeDirection direction) {
        if (this.runtimeUuid == null) return;
        MinecraftServer server = level.getServer();
        if (server == null) return;
        ServerLevel runtimeLevel = server.getLevel(Constants.RUNTIME_DIMENSION);
        if (runtimeLevel == null) return;
        CircuitSavedData data = CircuitSavedData.getRuntimeData(runtimeLevel);
        int octoletIndex = data.octoletIndexForSize(blockSize);
        if (!data.octolets.containsKey(octoletIndex)) data.addOctolet(octoletIndex, new Octolet(blockSize));
        BlockPos startingPos = data.getCircuitStartingPos(this.runtimeUuid);
        if (startingPos == null) return;
        for (int ii = 0; ii < this.blockSize; ii++) {
            for (int jj = 0; jj < this.blockSize; jj++) {
                BlockPos pos = this.twoDimensionalRelativeDirectionOffset(startingPos, ii, jj, direction);
                BlockState state = runtimeLevel.getBlockState(pos);
                if (state.is(Blocks.CIRCUIT_BOARD) && state.getValue(CircuitBoardBlock.MODE) == CircuitBoardBlock.Mode.INPUT)
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
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID("uuid")) this.uuid = tag.getUUID("uuid");
        this.runtimeUuid = tag.getUUID("runtimeUuid");
        this.blockSize = tag.getShort("blockSize");
        this.hidden = tag.getBoolean("hidden");
        this.powers = tag.getByteArray("powers");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (this.uuid != null) tag.putUUID("uuid", this.uuid);
        tag.putUUID("runtimeUuid", this.runtimeUuid);
        tag.putShort("blockSize", this.blockSize);
        tag.putBoolean("hidden", this.hidden);
        tag.putByteArray("powers", this.powers);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        ListTag list = new ListTag();
        for (Map.Entry<BlockPos, BlockState> entry : this.blocks.entrySet()) {
            CompoundTag tuple = new CompoundTag();
            tuple.put("pos", NbtUtils.writeBlockPos(entry.getKey()));
            tuple.put("block", NbtUtils.writeBlockState(entry.getValue()));
            list.add(tuple);
        }
        tag.put("blocks", list);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        super.onDataPacket(net, pkt, lookupProvider);
        if (this.level == null) return;
        CompoundTag tag = pkt.getTag();
        Map<BlockPos, BlockState> blocks = Maps.newHashMap();
        for (Tag t : tag.getList("blocks", Tag.TAG_COMPOUND)) {
            CompoundTag tuple = (CompoundTag) t;
            Optional<BlockPos> opt = NbtUtils.readBlockPos(tuple, "pos");
            if (opt.isEmpty()) continue;
            BlockPos pos = opt.get();
            BlockState state = NbtUtils.readBlockState(this.level.holderLookup(Registries.BLOCK), tuple.getCompound("block"));
            blocks.put(pos, state);
        }
        this.blocks = blocks;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.UUID, new UUIDDataComponent(this.uuid));
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
        this.setChanged();
    }

    public short getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(short blockSize) {
        this.blockSize = blockSize;
        this.setChanged();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        this.setChanged();
        if (!this.hidden && !this.level.isClientSide) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    public boolean matchRuntimeUuid(UUID uuid) {
        return this.runtimeUuid.equals(uuid);
    }

    public void setPower(int power, RelativeDirection direction) {
        byte oldPower = this.powers[direction.getId()];
        if (oldPower == power) return;
        this.powers[direction.getId()] = (byte) power;
        BlockState state = this.getBlockState();
        boolean powered = false;
        for (byte pow : this.powers) {
            if (pow > 0) {
                powered = true;
                break;
            }
        }
        state = state.setValue(CircuitBlock.POWERED, powered);
        this.level.setBlockAndUpdate(this.getBlockPos(), state);
        this.setChanged();
        this.updateInnerBlocks();
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

    public void getInputSignals() {
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();
        if (this.level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            for (Direction direction : Direction.values()) {
                RelativeDirection relDir = DirectionHelper.directionToRelativeDirection(state.getValue(HorizontalDirectionalBlock.FACING), direction);
                int signal = level.getSignal(pos.relative(direction), direction);
                blockEntity.updateRuntimeBlock(signal, relDir);
            }
        }
    }

    public enum RuntimeReloadResult {
        SUCCESS("action.circuit.reload.success", true),
        FAIL_NO_SERVER("action.circuit.reload.fail.no_server", false),
        FAIL_NOT_EXIST("action.circuit.reload.fail.not_exist", false),
        FAIL_RECURRENCE("action.circuit.reload.fail.recurrence", false);

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
