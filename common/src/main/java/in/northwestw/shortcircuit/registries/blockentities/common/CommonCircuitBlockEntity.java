package in.northwestw.shortcircuit.registries.blockentities.common;

import in.northwestw.shortcircuit.config.Config;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.CircuitBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class CommonCircuitBlockEntity extends BlockEntity {
    protected UUID uuid;
    protected boolean hidden;
    protected Component name;
    protected DyeColor color;
    private final int[] sameTickUpdates;

    public CommonCircuitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.sameTickUpdates = new int[6];
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("uuid")) this.uuid = tag.getUUID("uuid");
        else this.uuid = null;
        this.hidden = tag.getBoolean("hidden");
        if (tag.contains("customName", Tag.TAG_STRING)) this.name = Component.Serializer.fromJson(tag.getString("customName"));
        if (tag.contains("color", Tag.TAG_BYTE)) this.color = DyeColor.byId(tag.getByte("color"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.uuid != null) tag.putUUID("uuid", this.uuid);
        tag.putBoolean("hidden", this.hidden);
        if (this.name != null) tag.putString("customName", Component.Serializer.toJson(this.name));
        if (this.color != null) tag.putByte("color", (byte) this.color.getId());
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void saveToItem(ItemStack stack) {
        super.saveToItem(stack);
        CompoundTag tag = stack.getOrCreateTag();
        tag.putUUID("uuid", this.uuid);
        if (this.color != null) tag.putShort("color", (short) this.color.getId());
        stack.setTag(tag);
        if (this.name != null) stack.setHoverName(this.name);
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
        if (!this.hidden && !this.level.isClientSide) this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void setName(Component name) {
        this.name = name;
        this.setChanged();
    }

    private BooleanProperty colorBooleanProperty() {
        return this instanceof CircuitBlockEntity ? CircuitBlock.COLORED : IntegratedCircuitBlock.COLORED;
    }

    public void cycleColor(boolean backwards) {
        BooleanProperty property = this.colorBooleanProperty();
        if (this.color == null) {
            this.color = DyeColor.byId(backwards ? 15 : 0);
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(property, true), Block.UPDATE_CLIENTS);
        }
        else if (this.color.getId() < 15 && !backwards) this.color = DyeColor.byId(this.color.getId() + 1);
        else if (this.color.getId() > 0 && backwards) this.color = DyeColor.byId(this.color.getId() - 1);
        else {
            this.color = null;
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(property, false), Block.UPDATE_CLIENTS);
        }
        this.setChanged();
    }

    public void setColor(DyeColor color) {
        this.color = color;
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(this.colorBooleanProperty(), this.color != null), Block.UPDATE_CLIENTS);
        this.setChanged();
    }

    public DyeColor getColor() {
        return color;
    }

    public void tick() {
        boolean reTick = this.maxUpdateReached();
        Arrays.fill(this.sameTickUpdates, 0);
        if (reTick) {
            // couldn't finish update last tick due to limit, so we try again
            this.updateInputs();
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
