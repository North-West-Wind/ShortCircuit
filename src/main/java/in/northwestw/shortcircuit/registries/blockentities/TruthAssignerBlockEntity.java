package in.northwestw.shortcircuit.registries.blockentities;

import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.blocks.TruthAssignerBlock;
import in.northwestw.shortcircuit.registries.menus.TruthAssignerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class TruthAssignerBlockEntity extends BaseContainerBlockEntity {
    public static final int SIZE = 2;
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private boolean working, wait;
    private int maxDelay, ticks;
    private final ContainerData containerData;

    public TruthAssignerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.TRUTH_ASSIGNER.get(), pos, state);
        this.maxDelay = 20;
        this.containerData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> working ? 1 : 0;
                    case 1 -> wait ? 1 : 0;
                    case 2 -> maxDelay;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0:
                        boolean oldWorking = working;
                        working = value != 0;
                        if (value != 0 && !oldWorking)
                            start();
                        break;
                    case 1:
                        wait = value != 0;
                        break;
                    case 2:
                        maxDelay = value;
                        break;
                }
                setChanged();
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    @Override
    public int getContainerSize() {
        return SIZE;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.short_circuit.truth_assigner");
    }

    public ContainerData getContainerData() {
        return containerData;
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new TruthAssignerMenu(containerId, inventory, this, this.containerData);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.working = tag.getBoolean("working");
        this.wait = tag.getBoolean("wait");
        this.maxDelay = tag.getInt("maxDelay");
        this.ticks = tag.getInt("ticks");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putBoolean("working", this.working);
        tag.putBoolean("wait", this.wait);
        tag.putInt("maxDelay", this.maxDelay);
        tag.putInt("ticks", this.ticks);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider resgitries) {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag, resgitries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void start() {
        this.level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(TruthAssignerBlock.LIT, this.working));
    }

    private void stop() {
        // testing code: copy input to output
        ItemStack input = this.getItem(1);
        this.setItem(0, input.copy());
        this.setItem(1, ItemStack.EMPTY);
    }

    public void tick() {
        if (!this.working) return;
        // testing code
        if (++this.ticks >= this.maxDelay) {
            this.ticks = 0;
            this.stop();
        }
    }
}
