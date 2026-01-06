package in.northwestw.shortcircuit.registries.menus;

import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.Menus;
import in.northwestw.shortcircuit.registries.blockentities.TruthAssignerBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class TruthAssignerMenu extends AbstractContainerMenu {
    private final ContainerLevelAccess access;
    private final Container container;
    private final ContainerData containerData;

    // Client constructor
    public TruthAssignerMenu(int containerId, Inventory inventory) {
        this(containerId, inventory, ContainerLevelAccess.NULL, null, new SimpleContainerData(7));
    }

    // Server constructor
    public TruthAssignerMenu(int containerId, Inventory inventory, ContainerLevelAccess access, @Nullable Container container, ContainerData containerData) {
        super(Menus.TRUTH_ASSIGNER.get(), containerId);
        this.access = access;
        if (container == null) this.container = this.createContainer(2);
        else this.container = container;
        this.containerData = containerData;
        checkContainerSize(this.container, 2);
        checkContainerDataCount(this.containerData, 6);

        // input
        this.addSlot(new Slot(this.container, 0, 14, 34));
        // output
        this.addSlot(new Slot(this.container, 1, 72, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // hotbar goes from 2 to 10
        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

        // inventory goes from 11 to 37
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        this.addDataSlots(this.containerData);

        if (this.container instanceof TruthAssignerBlockEntity blockEntity)
            this.addSlotListener(blockEntity);
    }

    // inventory has size 2
    // 0 = out, 1 = in, 2-28 = player inv, 29-37 = player hotbar
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack stack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack rawStack = slot.getItem();
            stack = rawStack.copy();

            // data output slot
            if (index == 1) {
                // move to inventory
                if (!this.moveItemStackTo(rawStack, 2, 38, true))
                    return ItemStack.EMPTY;
                slot.onQuickCraft(rawStack, stack);
            }
            // inv or bar
            else if (index >= 2 && index < 38) {
                // move from inv/bar to in
                if (!this.moveItemStackTo(rawStack, 0, 1, false)) {
                    // move from inv to bar
                    if (index >= 11) {
                        if (!this.moveItemStackTo(rawStack, 2, 11, false))
                            return ItemStack.EMPTY;
                    }
                    // move from bar to inv
                    else if (!this.moveItemStackTo(rawStack, 11, 38, false))
                        return ItemStack.EMPTY;
                }
            }
            // from in to inv/bar
            else if (!this.moveItemStackTo(rawStack, 2, 38, false))
                return ItemStack.EMPTY;

            if (rawStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();

            if (rawStack.getCount() == stack.getCount())
                return ItemStack.EMPTY;
        }
        return stack;
    }

    @Override
    public boolean stillValid(Player player) {
        return AbstractContainerMenu.stillValid(this.access, player, Blocks.TRUTH_ASSIGNER.get());
    }

    public boolean isEmpty() {
        return this.container.isEmpty();
    }

    public boolean isWorking() {
        return this.containerData.get(0) == 1;
    }

    public boolean shouldWait() {
        return this.containerData.get(1) == 1;
    }

    public int getMaxDelay() {
        return this.containerData.get(2);
    }

    public int getExtraDelay() {
        return this.containerData.get(6);
    }

    public int getError() {
        return this.containerData.get(3);
    }

    public int getCurrentInput() {
        return this.containerData.get(4);
    }

    public int getBits() {
        return this.containerData.get(5);
    }

    public void setWait(boolean val) {
        this.setData(1, val ? 1 : 0);
    }

    public boolean setMaxDelay(int maxDelay) {
        if (maxDelay != this.getMaxDelay()) {
            this.setData(2, maxDelay);
            return true;
        }
        return false;
    }

    public boolean setExtraDelay(int extraDelay) {
        if (extraDelay != this.getExtraDelay()) {
            this.setData(6, extraDelay);
            return true;
        }
        return false;
    }

    public void setNextBits() {
        int newBits = this.getBits() * 2;
        if (newBits > 4) newBits = 1;
        this.setData(5, newBits);
    }

    public void start() {
        if (this.isWorking()) return;
        this.setData(0, 1);
    }

    // copied from ItemCombinerMenu
    private SimpleContainer createContainer(int size) {
        return new SimpleContainer(size) {
            @Override
            public void setChanged() {
                super.setChanged();
                TruthAssignerMenu.this.slotsChanged(this);
            }
        };
    }

    @Override
    public void setData(int id, int data) {
        super.setData(id, data);
        this.broadcastChanges();
    }

    // These are server-side
    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == -1) {
            this.setWait(!this.shouldWait());
            return true;
        } else if (id == -2) {
            this.start();
            return true;
        } else if (id == -3) {
            this.setNextBits();
            return true;
        } else {
            // cheese-iest strat ever to transfer different int without implementing my own packet
            int type = id & 0x1;
            id >>= 1;
            if (type == 0) this.setMaxDelay(id);
            else this.setExtraDelay(id);
        }
        return super.clickMenuButton(player, id);
    }
}
