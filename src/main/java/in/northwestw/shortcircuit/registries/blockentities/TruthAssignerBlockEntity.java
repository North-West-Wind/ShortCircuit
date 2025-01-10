package in.northwestw.shortcircuit.registries.blockentities;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.data.TruthTableSavedData;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.Items;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import in.northwestw.shortcircuit.registries.blocks.TruthAssignerBlock;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import in.northwestw.shortcircuit.registries.menus.TruthAssignerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class TruthAssignerBlockEntity extends BaseContainerBlockEntity implements ContainerListener {
    public static final int SIZE = 2;
    private NonNullList<ItemStack> items = NonNullList.withSize(SIZE, ItemStack.EMPTY);
    private boolean working, wait;
    private int maxDelay, ticks, errorCode;
    private final ContainerData containerData;
    // For assigning
    private final List<RelativeDirection> inputOrder, outputOrder;
    private int currentInput, lastOutput;
    private final Map<Integer, Integer> outputMap;
    private UUID workingUuid;

    public TruthAssignerBlockEntity(BlockPos pos, BlockState state) {
        super(BlockEntities.TRUTH_ASSIGNER.get(), pos, state);
        this.wait = true;
        this.maxDelay = 20;
        this.inputOrder = Lists.newArrayList();
        this.outputOrder = Lists.newArrayList();
        this.outputMap = Maps.newHashMap();
        this.containerData = new ContainerData() {
            @Override
            public int get(int index) {
                return switch (index) {
                    case 0 -> TruthAssignerBlockEntity.this.working ? 1 : 0;
                    case 1 -> TruthAssignerBlockEntity.this.wait ? 1 : 0;
                    case 2 -> TruthAssignerBlockEntity.this.maxDelay;
                    case 3 -> TruthAssignerBlockEntity.this.errorCode;
                    default -> 0;
                };
            }

            @Override
            public void set(int index, int value) {
                switch (index) {
                    case 0:
                        boolean oldWorking = TruthAssignerBlockEntity.this.working;
                        TruthAssignerBlockEntity.this.working = value != 0;
                        if (TruthAssignerBlockEntity.this.working && !oldWorking)
                            start();
                        break;
                    case 1:
                        TruthAssignerBlockEntity.this.wait = value != 0;
                        break;
                    case 2:
                        TruthAssignerBlockEntity.this.maxDelay = value;
                        break;
                    case 3:
                        TruthAssignerBlockEntity.this.errorCode = value;
                        break;
                }
                TruthAssignerBlockEntity.this.setChanged();
            }

            @Override
            public int getCount() {
                return 4;
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

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new TruthAssignerMenu(containerId, inventory, this.level == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(this.level, this.getBlockPos()), this, this.containerData);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, this.items, registries);
        this.working = tag.getBoolean("working");
        this.wait = tag.getBoolean("wait");
        this.maxDelay = tag.getInt("maxDelay");
        this.ticks = tag.getInt("ticks");
        this.errorCode = tag.getInt("errorCode");
        if (tag.hasUUID("workingUuid")) this.workingUuid = tag.getUUID("workingUuid");
        this.lastOutput = tag.getInt("lastOutput");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, this.items, registries);
        tag.putBoolean("working", this.working);
        tag.putBoolean("wait", this.wait);
        tag.putInt("maxDelay", this.maxDelay);
        tag.putInt("ticks", this.ticks);
        tag.putInt("errorCode", this.errorCode);
        if (this.workingUuid != null) tag.putUUID("workingUuid", this.workingUuid);
        tag.putInt("lastOutput", lastOutput);
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

    public boolean isWorking() {
        return working;
    }

    private void start() {
        this.level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(TruthAssignerBlock.LIT, this.working));
        this.level.setBlockAndUpdate(this.getBlockPos().above(), Blocks.CIRCUIT.get().defaultBlockState());
        CircuitBlockEntity blockEntity = (CircuitBlockEntity) this.level.getBlockEntity(this.getBlockPos().above());
        blockEntity.setFake(true);
        ItemStack input = this.getItem(0);
        if (input.has(DataComponents.UUID))
            blockEntity.setUuid(input.get(DataComponents.UUID).uuid());
        Pair<CircuitBlockEntity.RuntimeReloadResult, Map<RelativeDirection, CircuitBoardBlock.Mode>> pair = blockEntity.reloadRuntimeAndModeMap(Sets.newHashSet());
        CircuitBlockEntity.RuntimeReloadResult result = pair.getLeft();
        if (!result.isGood()) {
            this.setErrorCode(2, false);
            this.stop(false);
        }
        Map<RelativeDirection, CircuitBoardBlock.Mode> modeMap = pair.getRight();
        for (Map.Entry<RelativeDirection, CircuitBoardBlock.Mode> entry : modeMap.entrySet()) {
            CircuitBoardBlock.Mode mode = entry.getValue();
            if (mode == CircuitBoardBlock.Mode.INPUT) this.inputOrder.add(entry.getKey());
            else if (mode == CircuitBoardBlock.Mode.OUTPUT) this.outputOrder.add(entry.getKey());
        }
        this.workingUuid = blockEntity.getRuntimeUuid();
        this.setChanged();
    }

    private void stop(boolean success) {
        if (success && this.level instanceof ServerLevel level) {
            // copy input output mapping to data
            TruthTableSavedData data = TruthTableSavedData.getTruthTableData(level);
            UUID uuid = UUID.randomUUID();
            uuid = data.insertTruthTable(uuid, this.inputOrder, this.outputOrder, this.outputMap.entrySet().stream().filter(entry -> entry.getValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

            // create integrated circuits by amount of circuits
            ItemStack input = this.getItem(0);
            ItemStack outputs = new ItemStack(Items.INTEGRATED_CIRCUIT.get(), input.getCount());
            outputs.set(DataComponents.UUID, new UUIDDataComponent(uuid));
            this.setItem(0, ItemStack.EMPTY);
            this.setItem(1, outputs);
        }

        // clear working memory
        this.currentInput = 0;
        this.inputOrder.clear();
        this.outputOrder.clear();
        this.outputMap.clear();

        // set working to false
        this.containerData.set(0, 0);
        this.level.setBlockAndUpdate(getBlockPos(), getBlockState().setValue(TruthAssignerBlock.LIT, this.working));

        // remove the circuit
        if (this.level.getBlockEntity(this.getBlockPos().above()) instanceof CircuitBlockEntity blockEntity) blockEntity.removeRuntime();
        this.level.setBlockAndUpdate(this.getBlockPos().above(), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());

        // ding!
        this.level.playLocalSound(this.getBlockPos(), in.northwestw.shortcircuit.registries.SoundEvents.TRUTH_ASSIGNED.get(), SoundSource.BLOCKS, 1, this.level.random.nextFloat() * 0.2f + 0.95f, false);
    }

    public void tick() {
        if (!this.working) return;
        if (this.level != null && this.level.random.nextDouble() < 0.1)
            this.level.playLocalSound(this.getBlockPos(), SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.2f, this.level.random.nextFloat() * 0.4f + 0.8f, false);
        if (!(this.level.getBlockEntity(this.getBlockPos().above()) instanceof CircuitBlockEntity blockEntity)) {
            this.stop(false);
            return;
        }
        // on tick 0, setup input signals
        if (this.ticks == 0) {
            // input encoding is larger than all possible situation
            if (this.currentInput >= Math.pow(2, this.inputOrder.size() * 4)) {
                this.stop(true);
                return;
            }
            // update runtime block signals according to encoding
            for (int ii = 0; ii < this.inputOrder.size(); ii++) {
                int power = (this.currentInput >> (ii * 4)) & 0xF;
                blockEntity.updateRuntimeBlock(power, this.inputOrder.get(ii));
            }
        }
        this.ticks++;
        if (this.ticks >= this.maxDelay) this.recordOutput(true);
    }

    private void recordOutput(boolean forced) {
        if (!this.working || (!forced && this.wait) || !(this.level.getBlockEntity(this.getBlockPos().above()) instanceof CircuitBlockEntity blockEntity)) return;
        int signals = 0;
        for (RelativeDirection dir : this.outputOrder) {
            signals <<= 4;
            signals |= blockEntity.getRelativePower(dir);
        }
        if (signals != this.lastOutput || forced) {
            this.lastOutput = signals;
            this.ticks = 0;
            this.outputMap.put(this.currentInput, signals);
            this.currentInput++;
        }
    }

    public void checkAndRecord() {
        if (!(this.level.getBlockEntity(this.getBlockPos().above()) instanceof CircuitBlockEntity)) {
            this.stop(false);
            this.setErrorCode(3, false);
            return;
        }
        this.recordOutput(false);
    }

    public void setErrorCode(int errorCode, boolean unset) {
        if (!unset) this.containerData.set(3, errorCode);
        else if (this.errorCode == errorCode) this.containerData.set(3, 0);
        this.setChanged();
    }

    @Override
    public void slotChanged(AbstractContainerMenu menu, int index, ItemStack stack) {
        if ((this.errorCode == 2 || this.errorCode == 3) && index == 0 && stack.isEmpty())
            this.setErrorCode(this.errorCode, true);
    }

    @Override
    public void dataChanged(AbstractContainerMenu pContainerMenu, int pDataSlotIndex, int pValue) {}
}
