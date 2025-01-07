package in.northwestw.shortcircuit.registries.menus;

import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.Menus;
import in.northwestw.shortcircuit.registries.blockentities.TruthAssignerBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
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

    public TruthAssignerMenu(int containerId, Inventory inventory, FriendlyByteBuf extraData) {
        this(containerId, inventory, new SimpleContainer(2), new SimpleContainerData(3));
    }

    public TruthAssignerMenu(int containerId, Inventory inventory, Container container, ContainerData containerData) {
        this(containerId, inventory, ContainerLevelAccess.NULL, container, containerData);
    }

    public TruthAssignerMenu(int containerId, Inventory inventory, ContainerLevelAccess access, Container container, ContainerData containerData) {
        super(Menus.TRUTH_ASSIGNER.get(), containerId);
        this.access = access;
        this.container = container;
        this.containerData = containerData;

        this.addSlot(new Slot(this.container, 1, 14, 34));
        this.addSlot(new Slot(this.container, 0, 72, 34) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }

        for (int k = 0; k < 9; k++) {
            this.addSlot(new Slot(inventory, k, 8 + k * 18, 142));
        }

        this.addDataSlots(this.containerData);
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
            if (index == 0) {
                // move to inventory
                if (!this.moveItemStackTo(rawStack, 2, 38, true))
                    return ItemStack.EMPTY;
                slot.onQuickCraft(rawStack, stack);
            }
            // inv or bar
            else if (index >= 2 && index < 37) {
                // move from inv/bar to in
                if (!this.moveItemStackTo(rawStack, 1, 2, false)) {
                    // move from inv to bar
                    if (index < 29) {
                        if (this.moveItemStackTo(rawStack, 29, 38, false))
                            return ItemStack.EMPTY;
                    }
                    // move from bar to inv
                    else if (!this.moveItemStackTo(rawStack, 2, 29, false))
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

    public void setWait(boolean val) {
        this.containerData.set(1, val ? 1 : 0);
    }

    public void setMaxDelay(int maxDelay) {
        this.containerData.set(2, maxDelay);
    }

    public void start() {
        if (this.isWorking()) return;
        this.containerData.set(0, 1);
    }
}
