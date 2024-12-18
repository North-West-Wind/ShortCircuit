package in.northwestw.shortcircuit.registries.blockentities;

import com.google.common.collect.Maps;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.data.CircuitSavedData;
import in.northwestw.shortcircuit.data.Octolet;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;

public class CircuitBlockEntity extends BlockEntity {
    public UUID uuid;
    private short ticks;
    public final Map<Vec3, BlockState> blocks; // 8x8x8

    public CircuitBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CIRCUIT_BLOCK.get(), pos, state);
        this.ticks = 0;
        this.blocks = Maps.newHashMap();
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos pos, BlockState state, T t) {
        CircuitBlockEntity blockEntity = (CircuitBlockEntity) t;
        if (!blockEntity.shouldTick()) return;

        // ticking to set up blocks for rendering
        MinecraftServer server = level.getServer();
        if (server == null) return;
        ServerLevel circuitBoardLevel = level.getServer().getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        DimensionDataStorage storage = circuitBoardLevel.getDataStorage();
        CircuitSavedData data = storage.computeIfAbsent(new SavedData.Factory<>(CircuitSavedData::new, CircuitSavedData::load), "circuit_pos");
        Octolet octolet = data.getParentOctolet(blockEntity.uuid);
        BlockPos startingPos = data.getCircuitStartingPos(blockEntity.uuid).offset(1, 1, 1);
        blockEntity.blocks.clear();
        int innerSize = octolet.blockSize - 2;
        for (int ii = 0; ii < innerSize; ii++) {
            for (int jj = 0; jj < innerSize; jj++) {
                for (int kk = 0; kk < innerSize; kk++) {
                    BlockState blockState = circuitBoardLevel.getBlockState(startingPos.offset(ii, jj, kk));
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

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.uuid = tag.getUUID("uuid");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putUUID("uuid", this.uuid);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        components.set(DataComponents.UUID, new UUIDDataComponent(this.uuid));
    }
}
