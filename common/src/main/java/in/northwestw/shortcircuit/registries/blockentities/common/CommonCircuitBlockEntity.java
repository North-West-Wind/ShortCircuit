package in.northwestw.shortcircuit.registries.blockentities.common;

import com.mojang.logging.LogUtils;
import in.northwestw.shortcircuit.config.Config;
import in.northwestw.shortcircuit.properties.CrossVersionTag;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.blocks.common.CommonCircuitBlock;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

//? if >=1.21.11 {
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
//? } else {
/*import net.minecraft.nbt.Tag;
*///? }

//? if >=1.21.1 {
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
//? } else {
/*import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.item.ItemStack;
*///? }

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
        this.color = 16;
    }

    @Override
    //? if >=1.21.11 {
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
    //? } elif >=1.21.1 {
    /*protected void loadAdditional(CompoundTag input, HolderLookup.Provider provider) {
        super.loadAdditional(input, provider);
    *///? } else {
    /*public void load(CompoundTag input) {
        super.load(input);
    *///? }
        CrossVersionTag.Reader reader = new CrossVersionTag.Reader(input);
        this.hidden = reader.getBoolean("hidden");
        reader.getUUID("uuid").ifPresent(uuid -> this.uuid = uuid);
        this.name = reader.getString("customName").orElse(null);
        byte color = reader.getByte("color", (byte) -1);
        if (color != -1) this.color = color;
        else this.savedColor = true;
    }

    @Override
    //? if >=1.21.11 {
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
    //? } elif >=1.21.1 {
    /*protected void saveAdditional(CompoundTag output, HolderLookup.Provider provider) {
        super.saveAdditional(output, provider);
    *///? } else {
    /*protected void saveAdditional(CompoundTag output) {
        super.saveAdditional(output);
    *///? }
        CrossVersionTag.Writer writer = new CrossVersionTag.Writer(output);
        writer.putBoolean("hidden", this.hidden);
        if (this.uuid != null) writer.putUUID("uuid", this.uuid);
        if (this.name != null) writer.putString("name", this.name);
        if (!this.savedColor) writer.putByte("color", this.color);
    }

    @Override
    //? if >=1.21.1 {
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }
    //? } else {
    /*public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }
    *///? }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    //? if >=1.21.11 {
    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.applyImplicitComponentIfPresent(components, net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        this.applyImplicitComponentIfPresent(components, DataComponents.SHORT.get());
        this.applyImplicitComponentIfPresent(components, DataComponents.UUID.get());
    }

    protected <T> boolean applyImplicitComponentIfPresent(DataComponentGetter componentGetter, DataComponentType<T> component) {
        T t = componentGetter.get(component);
        return t != null && this.applyImplicitComponent(component, t);
    }
    //? } elif >=1.21.1 {
    /*@Override
    protected void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        this.applyImplicitComponentIfPresent(components, net.minecraft.core.component.DataComponents.CUSTOM_NAME);
        this.applyImplicitComponentIfPresent(components, DataComponents.SHORT.get());
        this.applyImplicitComponentIfPresent(components, DataComponents.UUID.get());
    }

    protected <T> boolean applyImplicitComponentIfPresent(DataComponentInput componentGetter, DataComponentType<T> component) {
        T t = componentGetter.get(component);
        return t != null && this.applyImplicitComponent(component, t);
    }
    *///? }

    //? if >=1.21.1 {
    protected <T> boolean applyImplicitComponent(DataComponentType<T> component, T value) {
        if (component == net.minecraft.core.component.DataComponents.CUSTOM_NAME) {
            this.name = ((Component) value).getString();
            return true;
        } else if (component == DataComponents.SHORT.get()) {
            this.color = (byte) (short) value;
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(CommonCircuitBlock.COLOR, (int) (short) value), Block.UPDATE_CLIENTS);
            return true;
        } else if (component == DataComponents.UUID.get()) {
            this.uuid = ((UUIDDataComponent) value).uuid();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        if (this.uuid != null) components.set(DataComponents.UUID.get(), new UUIDDataComponent(this.uuid));
        components.set(DataComponents.SHORT.get(), this.getBlockState().getValue(CommonCircuitBlock.COLOR).shortValue());
        if (this.name != null) components.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME, Component.literal(this.name));
    }
    //? } else {
    /*@Override
    public void saveToItem(ItemStack stack) {
        super.saveToItem(stack);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("uuid", this.uuid);
        tag.putShort("color", this.getBlockState().getValue(CommonCircuitBlock.COLOR).shortValue());
        stack.setTag(tag);
        if (this.name != null) stack.setHoverName(MutableComponent.create(new LiteralContents(this.name)));
    }
    *///? }

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
        if (!this.savedColor && this.level != null) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(CommonCircuitBlock.COLOR, (int) this.color), Block.UPDATE_CLIENTS);
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
