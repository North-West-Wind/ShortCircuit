package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.data.CircuitLimitSavedData;
import in.northwestw.shortcircuit.registries.*;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.common.CommonCircuitBlock;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//? if >=1.21.4 {
import in.northwestw.shortcircuit.ShortCircuitCommon;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.redstone.Orientation;
//? } elif >=1.21.1 {
/*import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ItemInteractionResult;
*///? } else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
*///? }

import java.util.UUID;

public class CircuitBlock extends CommonCircuitBlock {
    public CircuitBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    //? if >=1.21.1 {
    @Override
    protected @NotNull MapCodec<CircuitBlock> codec() {
        return Codecs.CIRCUIT.get();
    }
    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    //? } else {
    /*@Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    *///? }
        BlockEntity blockentity = level.getBlockEntity(pos);
        ItemStack stack = new ItemStack(Blocks.CIRCUIT.get());
        if (blockentity instanceof CircuitBlockEntity circuitBlockEntity) {
            if (!player.isCreative() && circuitBlockEntity.isValid()) {
                //? if >=1.21.1 {
                stack.applyComponents(blockentity.collectComponents());
                //? } else
                //circuitBlockEntity.saveToItem(stack);
            }
            circuitBlockEntity.removeRuntime();

            UUID owner = circuitBlockEntity.getOwnerUuid();
            MinecraftServer server = circuitBlockEntity.getLevel().getServer();
            if (owner != null && server != null)
                CircuitLimitSavedData.getRuntimeData(server).remove(owner);
        }
        if (!player.isCreative()) {
            ItemEntity itementity = new ItemEntity(level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, stack);
            itementity.setDefaultPickUpDelay();
            level.addFreshEntity(itementity);
        }

        //? if >=1.21.1 {
        return super.playerWillDestroy(level, pos, state, player);
        //? } else
        //super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    //~ if <=1.21.1 'ServerLevel' -> 'Level'
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            blockEntity.removeRuntime();

            UUID owner = blockEntity.getOwnerUuid();
            //? if >=1.21.4 {
            if (owner != null)
                CircuitLimitSavedData.getRuntimeData(level).remove(owner);
            //? } else {
            /*MinecraftServer server = level.getServer();
            if (owner != null && server != null)
                CircuitLimitSavedData.getRuntimeData(server).remove(owner);
            *///? }
        }
        super.wasExploded(level, pos, explosion);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypes.CIRCUIT.get() ? (pLevel, pPos, pState, blockEntity) -> ((CircuitBlockEntity) blockEntity).tick() : null;
    }

    //? <=1.20.1 {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return this.useWithoutItem(state, level, pos, player, hit);
        else return this.useItemOn(stack, state, level, pos, player, hand, hit);
    }
    *///? }

    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            //? if >=1.21.1 {
            if (blockEntity.isFake()) return super.useWithoutItem(state, level, pos, player, hitResult);
            //? } else
            //if (blockEntity.isFake()) return InteractionResult.PASS;
            else {
                player.displayClientMessage(Component.translatable("action.circuit.reload"), true);
                CircuitBlockEntity.RuntimeReloadResult result = blockEntity.reloadRuntime();
                if (result != CircuitBlockEntity.RuntimeReloadResult.FAIL_NO_SERVER)
                    player.displayClientMessage(Component.translatable(result.getTranslationKey()).withStyle(Style.EMPTY.withColor(result.isGood() ? 0x00ff00 : 0xff0000)), true);
            }
        }
        //? if >=1.21.4 {
        return InteractionResult.SUCCESS_SERVER;
        //? } else
        //return InteractionResult.sidedSuccess(level.isClientSide);
    }

    //? if >=1.21.4 {
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (stack.is(Items.POKING_STICK.get()) || stack.is(Items.LABELLING_STICK.get())) return InteractionResult.PASS; // handled by item
    //? } elif >=1.21.1 {
    /*@Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (stack.is(Items.POKING_STICK.get()) || stack.is(Items.LABELLING_STICK.get())) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION; // handled by item
    *///? } else {
    /*protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (stack.is(Items.POKING_STICK.get()) || stack.is(Items.LABELLING_STICK.get())) return InteractionResult.PASS; // handled by item
    *///? }
        else if ((stack.is(Items.CIRCUIT.get()) || stack.is(Items.INTEGRATED_CIRCUIT.get())) && !player.isCrouching() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity && blockEntity.isValid()) {
            ItemStack newStack = new ItemStack(Items.CIRCUIT.get(), stack.getCount());
            //? if >=1.21.1 {
            newStack.applyComponents(stack.getComponents());
            newStack.set(DataComponents.UUID.get(), new UUIDDataComponent(blockEntity.getUuid()));
            newStack.set(DataComponents.SHORT.get(), state.getValue(COLOR).shortValue());
            //? } else {
            /*if (stack.hasTag()) newStack.setTag(stack.getTag());
            CompoundTag tag = newStack.getOrCreateTag();
            tag.putUUID("uuid", blockEntity.getUuid());
            tag.putShort("color", level.getBlockState(pos).getValue(CommonCircuitBlock.COLOR).shortValue());
            *///? }
            //? if >=1.21.4 {
            newStack.set(net.minecraft.core.component.DataComponents.ITEM_MODEL, ShortCircuitCommon.rl("circuit"));
            //? }
            player.setItemInHand(hand, newStack);
            player.playSound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1);
            //? if >=1.21.4 {
            return InteractionResult.SUCCESS.heldItemTransformedTo(newStack);
            //? } elif >=1.21.1 {
            /*return ItemInteractionResult.SUCCESS;
            *///? } else
            //return InteractionResult.SUCCESS;
        }
        //? if >=1.21.1 {
        return super.useItemOn(stack, state, level, pos, player, hand, result);
        //? } else
        //return this.useWithoutItem(state, level, pos, player, result);
    }

    @Override
    //~ if <=1.20.1 'protected' -> 'public'
    protected boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    //~ if <=1.20.1 'protected' -> 'public'
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity)) return 0;
        return blockEntity.getPower(direction);
    }

    @Override
    //~ if <=1.20.1 'protected' -> 'public'
    protected int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getSignal(pBlockAccess, pPos, pSide);
    }

    @Override
    //? if >=1.21.4 {
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
    //? } elif >=1.21.1 {
    /*protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    *///? } else {
    /*public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    *///? }
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            //ShortCircuitCommon.LOGGER.info("neighbor changed for circuit at {}", pos);
            blockEntity.updateInputs();
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            boolean disallowed = false;
            MinecraftServer server = level.getServer();
            if (server == null) return;
            if (placer instanceof Player player) {
                blockEntity.setOwnerUuid(placer.getUUID());
                CircuitLimitSavedData data = CircuitLimitSavedData.getRuntimeData(server);
                if (!data.canAdd(placer.getUUID())) {
                    player.displayClientMessage(Component.translatable("warning.circuit.place.circuit_board.limit").withStyle(Style.EMPTY.withColor(0xffff00)), true);
                    disallowed = true;
                } else
                    data.add(placer.getUUID());
            }

            //? if >=1.21.1 {
            if (!disallowed && stack.has(DataComponents.UUID.get())) {
                blockEntity.setUuid(stack.get(DataComponents.UUID.get()).uuid());
                if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME))
                    blockEntity.setName(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME).getString());
                if (stack.has(DataComponents.SHORT.get()))
                    level.setBlock(pos, state.setValue(COLOR, stack.get(DataComponents.SHORT.get()).intValue()), Block.UPDATE_CLIENTS);
            //? } else {
            /*CompoundTag tag = stack.getOrCreateTag();
            if (!disallowed && tag.hasUUID("uuid")) {
                blockEntity.setUuid(tag.getUUID("uuid"));
                if (stack.hasCustomHoverName())
                    blockEntity.setName(stack.getHoverName().getString());
                if (tag.contains("color", Tag.TAG_SHORT))
                    level.setBlock(pos, state.setValue(COLOR, (int) tag.getShort("color")), Block.UPDATE_CLIENTS);
            *///? }
                if (!level.dimension().equals(Constants.CIRCUIT_BOARD_DIMENSION)) {
                    blockEntity.reloadRuntime();
                    blockEntity.updateInputs();
                } else if (placer instanceof Player player)
                    player.displayClientMessage(Component.translatable("warning.circuit.place.circuit_board").withStyle(Style.EMPTY.withColor(0xffff00)), true);
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}
