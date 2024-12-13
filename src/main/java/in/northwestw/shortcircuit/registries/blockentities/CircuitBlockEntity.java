package in.northwestw.shortcircuit.registries.blockentities;

import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class CircuitBlockEntity extends BlockEntity {
    public UUID uuid;

    public CircuitBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.CIRCUIT_BLOCK.get(), pos, state);
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
