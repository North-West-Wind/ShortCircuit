package in.northwestw.shortcircuit.registries.blockentities;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import in.northwestw.shortcircuit.data.TruthTableSavedData;
import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.blocks.IntegratedCircuitBlock;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class IntegratedCircuitBlockEntity extends BlockEntity {
    private UUID uuid;
    private final Map<RelativeDirection, Integer> inputs;
    private Map<RelativeDirection, Integer> outputs;
    private final boolean[] changed;

    public IntegratedCircuitBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.INTEGRATED_CIRCUIT.get(), pos, state);
        this.inputs = Maps.newHashMap();
        this.outputs = Maps.newHashMap();
        this.changed = new boolean[6];
    }

    public boolean isValid() {
        return this.uuid == null;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        if (tag.hasUUID("uuid")) this.uuid = tag.getUUID("uuid");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        if (this.uuid != null) tag.putUUID("uuid", this.uuid);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, registries);
        return tag;
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

    public void setInput(RelativeDirection direction, int signal) {
        this.inputs.put(direction, signal);
    }

    public void setInputAndUpdate(RelativeDirection direction, int signal) {
        this.setInput(direction, signal);
        this.updateOutput();
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(IntegratedCircuitBlock.POWERED, this.outputs.values().stream().anyMatch(power -> power > 0)), Block.UPDATE_CLIENTS);
        for (int ii = 0; ii < this.changed.length; ii++)
            if (this.changed[ii]) {
                BlockPos pos = this.getBlockPos().relative(DirectionHelper.relativeDirectionToFacing(RelativeDirection.fromId((byte) ii), this.getBlockState().getValue(HorizontalDirectionalBlock.FACING)));
                this.level.neighborChanged(pos, this.level.getBlockState(pos).getBlock(), this.getBlockPos());
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
        }
    }

    private void clearChanged() {
        Arrays.fill(this.changed, false);
    }

    public void getInputSignals() {
        BlockPos pos = this.getBlockPos();
        BlockState state = this.getBlockState();
        for (Direction direction : Direction.values()) {
            RelativeDirection relDir = DirectionHelper.directionToRelativeDirection(state.getValue(HorizontalDirectionalBlock.FACING), direction);
            int signal = level.getSignal(pos.relative(direction), direction);
            this.setInput(relDir, signal);
        }
        this.updateOutput();
    }
}
