package in.northwestw.shortcircuit.registries.blockentities;

import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.UUID;

public class CircuitBoardBlockEntity extends BlockEntity {
    private ResourceKey<Level> dimension;
    private BlockPos pos;
    private UUID runtimeUuid;

    public CircuitBoardBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CIRCUIT_BOARD.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getString("dim").ifPresent(dim -> this.dimension = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dim)));
        input.getIntArray("pos").ifPresent(pos -> this.pos = new BlockPos(pos[0], pos[1], pos[2]));
        input.getIntArray("uuid").ifPresent(uuid -> this.runtimeUuid = UUIDUtil.uuidFromIntArray(uuid));
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (this.dimension != null) output.putString("dim", this.dimension.identifier().toString());
        if (this.pos != null) output.putIntArray("pos", new int[] { this.pos.getX(), this.pos.getY(), this.pos.getZ() });
        if (this.runtimeUuid != null) output.putIntArray("uuid", UUIDUtil.uuidToIntArray(this.runtimeUuid));
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
            level.neighborChanged(updatePos, circuitState.getBlock(), null);
            Block updateBlock = level.getBlockState(updatePos).getBlock();
            if (updateBlock != Blocks.CIRCUIT.get() && updateBlock != Blocks.INTEGRATED_CIRCUIT.get())
                level.updateNeighborsAtExceptFromFacing(updatePos, updateBlock, circuitDirection.getOpposite(), null);
        }
    }
}
