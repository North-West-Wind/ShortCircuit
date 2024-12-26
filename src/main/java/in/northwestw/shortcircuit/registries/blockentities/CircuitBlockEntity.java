package in.northwestw.shortcircuit.registries.blockentities;

import com.google.common.collect.Maps;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.data.CircuitSavedData;
import in.northwestw.shortcircuit.data.Octolet;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

public class CircuitBlockEntity extends BlockEntity {
    private UUID uuid, runtimeUuid;
    private short ticks, blockSize;
    private boolean hidden;
    public final Map<Vec3, BlockState> blocks; // 8x8x8

    public CircuitBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CIRCUIT.get(), pos, state);
        this.ticks = 0;
        this.blocks = Maps.newHashMap();
        this.runtimeUuid = UUID.randomUUID();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        CircuitBlockEntity blockEntity = (CircuitBlockEntity) t;
        if (!blockEntity.shouldTick()) return;

        // ticking to set up blocks for rendering
        MinecraftServer server = level.getServer();
        if (server == null) return;
        ServerLevel runtimeLevel = level.getServer().getLevel(Constants.RUNTIME_DIMENSION);
        if (runtimeLevel == null) return;
        CircuitSavedData data = CircuitSavedData.getRuntimeData(runtimeLevel);
        Octolet octolet = data.getParentOctolet(blockEntity.runtimeUuid);
        if (octolet == null) return;
        BlockPos startingPos = data.getCircuitStartingPos(blockEntity.runtimeUuid).offset(1, 1, 1);
        blockEntity.setBlockSize(octolet.blockSize);
        blockEntity.blocks.clear();
        int innerSize = octolet.blockSize - 2;
        for (int ii = 0; ii < innerSize; ii++) {
            for (int jj = 0; jj < innerSize; jj++) {
                for (int kk = 0; kk < innerSize; kk++) {
                    BlockState blockState = runtimeLevel.getBlockState(startingPos.offset(ii, jj, kk));
                    blockEntity.blocks.put(new Vec3(ii, jj, kk), blockState);
                }
            }
        }
    }

    public boolean shouldTick() {
        this.ticks = (short) ((this.ticks + 1) % 100); // tick only every 5 seconds to reduce lag
        return this.ticks == 1;
    }

    public boolean isValid() {
        return this.uuid != null;
    }

    public void reloadRuntime() {
        if (this.uuid == null) return;
        MinecraftServer server = this.level.getServer();
        if (server == null) return;
        ServerLevel circuitBoardLevel = level.getServer().getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        ServerLevel runtimeLevel = level.getServer().getLevel(Constants.RUNTIME_DIMENSION);
        if (circuitBoardLevel == null || runtimeLevel == null) return;
        CircuitSavedData boardData = CircuitSavedData.getCircuitBoardData(circuitBoardLevel);
        CircuitSavedData runtimeData = CircuitSavedData.getRuntimeData(runtimeLevel);
        BlockPos boardPos = boardData.getCircuitStartingPos(this.uuid);
        if (boardPos == null) return; // circuit doesn't exist yet. use the poking stick on it
        Octolet octolet = runtimeData.getParentOctolet(this.runtimeUuid);
        if (octolet == null) {
            int octoletIndex = runtimeData.octoletIndexForSize(blockSize);
            if (!runtimeData.octolets.containsKey(octoletIndex)) runtimeData.addOctolet(octoletIndex, new Octolet(blockSize));
            runtimeData.addCircuit(this.runtimeUuid, octoletIndex);
        }
        BlockPos runtimePos = runtimeData.getCircuitStartingPos(this.runtimeUuid);
        for (int ii = 0; ii < this.blockSize; ii++) {
            for (int jj = 0; jj < this.blockSize; jj++) {
                for (int kk = 0; kk < this.blockSize; kk++) {
                    BlockPos oldPos = boardPos.offset(ii, jj, kk);
                    BlockPos newPos = runtimePos.offset(ii, jj, kk);
                    runtimeLevel.setBlockAndUpdate(newPos, circuitBoardLevel.getBlockState(oldPos));
                    CompoundTag save = circuitBoardLevel.getBlockEntity(oldPos).saveCustomOnly(circuitBoardLevel.registryAccess());
                    BlockEntity be = runtimeLevel.getBlockEntity(newPos);
                    be.loadCustomOnly(save, runtimeLevel.registryAccess());
                    if (be instanceof CircuitBoardBlockEntity) {
                        ((CircuitBoardBlockEntity) be).setConnection(this.level.dimension(), this.getBlockPos(), this.runtimeUuid);
                    }
                }
            }
        }
        this.ticks = 0;
    }

    public void updateRuntimeBlock(int signal, CircuitBoardBlock.RelativeDirection direction) {
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

    private BlockPos twoDimensionalRelativeDirectionOffset(BlockPos pos, int ii, int jj, CircuitBoardBlock.RelativeDirection direction) {
        return switch (direction) {
            case UP -> pos.offset(ii, this.blockSize - 1, jj);
            case DOWN -> pos.offset(ii, 0, jj);
            case LEFT -> pos.offset(ii, jj, 0);
            case RIGHT -> pos.offset(ii, jj, this.blockSize - 1);
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (this.uuid != null) tag.putUUID("uuid", this.uuid);
        tag.putUUID("runtimeUuid", this.runtimeUuid);
        tag.putShort("blockSize", this.blockSize);
        tag.putBoolean("hidden", this.hidden);
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
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        this.setChanged();
    }

    public boolean matchRuntimeUuid(UUID uuid) {
        return this.runtimeUuid.equals(uuid);
    }
}
