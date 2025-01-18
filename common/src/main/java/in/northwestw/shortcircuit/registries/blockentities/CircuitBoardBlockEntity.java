package in.northwestw.shortcircuit.registries.blockentities;

import com.google.common.collect.Lists;
import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.BlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class CircuitBoardBlockEntity extends BlockEntity {
    private ResourceKey<Level> dimension;
    private BlockPos pos;
    private UUID runtimeUuid;

    public CircuitBoardBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CIRCUIT_BOARD.get(), pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.contains("dim")) this.dimension = ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(tag.getString("dim")));
        if (tag.contains("pos")) {
            int[] arr = tag.getIntArray("pos");
            this.pos = new BlockPos(arr[0], arr[1], arr[2]);
        }
        if (tag.contains("uuid")) this.runtimeUuid = tag.getUUID("uuid");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (this.dimension != null) tag.putString("dim", this.dimension.location().toString());
        if (this.pos != null) tag.putIntArray("pos", Lists.newArrayList(this.pos.getX(), this.pos.getY(), this.pos.getZ()));
        if (this.runtimeUuid != null) tag.putUUID("uuid", this.runtimeUuid);
    }

    public void setConnection(ResourceKey<Level> dimension, BlockPos pos, UUID uuid) {
        this.dimension = dimension;
        this.pos = pos;
        this.runtimeUuid = uuid;
        this.setChanged();
    }

    public void updateCircuitBlock(int signal, RelativeDirection direction) {
        if (this.dimension == null || this.pos == null || this.runtimeUuid == null) return;
        MinecraftServer server = this.level.getServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(this.dimension);
        if (level == null) return;
        BlockEntity be = level.getBlockEntity(this.pos);
        if (!(be instanceof CircuitBlockEntity blockEntity) || !blockEntity.matchRuntimeUuid(this.runtimeUuid)) return;
        if (blockEntity.setPower(signal, direction)) {
            BlockState circuitState = level.getBlockState(this.pos);
            Direction circuitDirection = circuitState.getValue(HorizontalDirectionalBlock.FACING);
            BlockPos updatePos = this.pos.relative(DirectionHelper.relativeDirectionToFacing(direction, circuitDirection));
            level.neighborChanged(updatePos, level.getBlockState(updatePos).getBlock(), null);
            level.updateNeighborsAtExceptFromFacing(updatePos, circuitState.getBlock(), circuitDirection.getOpposite(), null);
        }
    }
}
