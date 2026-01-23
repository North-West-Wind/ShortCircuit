package in.northwestw.shortcircuit.registries.blockentities.common;

import com.mojang.logging.LogUtils;
import in.northwestw.shortcircuit.config.Config;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.UUID;

public class CommonCircuitBlockEntity extends BlockEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    protected UUID uuid;
    protected boolean hidden;
    protected String name;
    private byte color;
    private boolean savedColor;
    private final int[] sameTickUpdates;

    public CommonCircuitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.sameTickUpdates = new int[6];
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.hidden = input.getBooleanOr("hidden", false);
        input.getIntArray("uuid").ifPresentOrElse(arr -> this.uuid = UUIDUtil.uuidFromIntArray(arr), () -> this.uuid = null);
        input.getString("customName").ifPresentOrElse(name -> this.name = name, () -> this.name = null);
        byte color = input.getByteOr("color", (byte) -1);
        // backwards compatible with before v1.0.9
        if (color != -1) this.color = color;
        else this.savedColor = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("hidden", this.hidden);
        if (this.uuid != null) output.putIntArray("uuid", UUIDUtil.uuidToIntArray(this.uuid));
        if (this.name != null) output.putString("name", this.name);
        if (!this.savedColor) output.putByte("color", this.color);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.applyImplicitComponentIfPresent(components, net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        this.applyImplicitComponentIfPresent(components, DataComponents.SHORT.get());
        this.applyImplicitComponentIfPresent(components, DataComponents.UUID.get());
    }

    protected <T> boolean applyImplicitComponent(DataComponentType<T> component, T value) {
        if (component == net.minecraft.core.component.DataComponents.CUSTOM_NAME) {
            this.name = ((Component) value).getString();
            return true;
        } else if (component == DataComponents.SHORT.get()) {
            this.color = (byte) value;
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(CircuitProperties.COLOR, (int) value), Block.UPDATE_CLIENTS);
            return true;
        } else if (component == DataComponents.UUID.get()) {
            this.uuid = ((UUIDDataComponent) value).uuid();
            return true;
        } else {
            return false;
        }
    }

    protected <T> boolean applyImplicitComponentIfPresent(DataComponentGetter componentGetter, DataComponentType<T> component) {
        T t = componentGetter.get(component);
        return t != null && this.applyImplicitComponent(component, t);
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.uuid != null) components.set(DataComponents.UUID.get(), new UUIDDataComponent(this.uuid));
        components.set(DataComponents.SHORT.get(), this.getBlockState().getValue(CircuitProperties.COLOR).shortValue());
        if (this.name != null) components.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(this.name));
    }

    public boolean isValid() {
        return this.uuid != null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
        this.setChanged();
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
        this.setChanged();
        if (!this.hidden && !this.level.isClientSide()) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void setName(String name) {
        this.name = name;
        this.setChanged();
    }

    public void tick() {
        boolean reTick = this.maxUpdateReached();
        Arrays.fill(this.sameTickUpdates, 0);
        if (reTick) {
            // couldn't finish update last tick due to limit, so we try again
            this.updateInputs();
        }
        if (!this.savedColor) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(CircuitProperties.COLOR, (int) this.color), Block.UPDATE_CLIENTS);
            this.savedColor = true;
        }
    }

    public void updateInputs() {

    }

    protected boolean maxUpdateReached() {
        return Config.SAME_SIDE_TICK_LIMIT > 0 && Arrays.stream(this.sameTickUpdates).anyMatch(t -> t >= Config.SAME_SIDE_TICK_LIMIT);
    }

    protected void sideUpdated(RelativeDirection direction) {
        this.sameTickUpdates[direction.getId()]++;
    }
}
