package in.northwestw.shortcircuit.registries.blockentities.common;

import in.northwestw.shortcircuit.config.Config;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.UUID;

public class CommonCircuitBlockEntity extends BlockEntity {
    protected UUID uuid;
    protected boolean hidden;
    protected Component name;
    private byte color;
    private boolean savedColor;
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
        if (tag.contains("color", Tag.TAG_BYTE)) this.color = tag.getByte("color");
        else this.savedColor = true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.uuid != null) tag.putUUID("uuid", this.uuid);
        tag.putBoolean("hidden", this.hidden);
        if (this.name != null) tag.putString("customName", Component.Serializer.toJson(this.name));
        if (!this.savedColor) tag.putByte("color", this.color);
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
        tag.putShort("color", this.getBlockState().getValue(CircuitProperties.COLOR).shortValue());
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
