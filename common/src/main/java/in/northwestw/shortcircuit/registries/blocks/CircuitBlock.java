package in.northwestw.shortcircuit.registries.blocks;

import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.data.CircuitLimitSavedData;
import in.northwestw.shortcircuit.registries.*;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty COLORED = BooleanProperty.create("colored");

    public CircuitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(COLORED, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        //builder.add(FACING, UP_POWER, DOWN_POWER, LEFT_POWER, RIGHT_POWER, FRONT_POWER, BACK_POWER);
        builder.add(FACING, POWERED, COLORED);
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof CircuitBlockEntity circuitBlockEntity) {
            if (!player.isCreative() && circuitBlockEntity.isValid()) {
                ItemStack stack = new ItemStack(Blocks.CIRCUIT.get());
                circuitBlockEntity.saveToItem(stack);
                ItemEntity itementity = new ItemEntity(
                        level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, stack
                );
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
            circuitBlockEntity.removeRuntime();

            UUID owner = circuitBlockEntity.getOwnerUuid();
            MinecraftServer server = player.getServer();
            if (owner != null && server != null)
                CircuitLimitSavedData.getRuntimeData(server).remove(owner);
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            blockEntity.removeRuntime();

            UUID owner = blockEntity.getOwnerUuid();
            MinecraftServer server = level.getServer();
            if (owner != null && server != null)
                CircuitLimitSavedData.getRuntimeData(server).remove(owner);
        }
        super.wasExploded(level, pos, explosion);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, level, components, flag);
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID("uuid")) {
            components.add(Component.translatable("tooltip.short_circuit.circuit", tag.getUUID("uuid").toString()).withStyle(Style.EMPTY.withColor(0x7f7f7f)));
        }
        if (tag.contains("color", CompoundTag.TAG_SHORT)) {
            DyeColor color = DyeColor.byId(tag.getShort("color"));
            components.add(Component.translatable("tooltip.short_circuit.circuit.color", Component.translatable("color.minecraft." + color.getName())).withStyle(Style.EMPTY.withColor(color.getTextColor())));
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntities.CIRCUIT.get() ? (pLevel, pPos, pState, blockEntity) -> ((CircuitBlockEntity) blockEntity).tick() : null;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return this.useWithoutItem(level, pos, player);
        else return this.useItemOn(stack, level, pos, player, hand, hitResult);
    }

    protected InteractionResult useWithoutItem(Level level, BlockPos pos, Player player) {
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            if (blockEntity.isFake()) return InteractionResult.PASS;
            else {
                player.displayClientMessage(Component.translatable("action.circuit.reload"), true);
                CircuitBlockEntity.RuntimeReloadResult result = blockEntity.reloadRuntime();
                if (result != CircuitBlockEntity.RuntimeReloadResult.FAIL_NO_SERVER)
                    player.displayClientMessage(Component.translatable(result.getTranslationKey()).withStyle(Style.EMPTY.withColor(result.isGood() ? 0x00ff00 : 0xff0000)), true);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    protected InteractionResult useItemOn(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.POKING_STICK.get()) || stack.is(Items.LABELLING_STICK.get())) return stack.useOn(new UseOnContext(player, hand, hitResult)); // handled by item
        else if ((stack.is(Items.CIRCUIT.get()) || stack.is(Items.INTEGRATED_CIRCUIT.get())) && !player.isCrouching() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity && blockEntity.isValid()) {
            ItemStack newStack = new ItemStack(Items.CIRCUIT.get(), stack.getCount());
            if (stack.hasTag()) newStack.setTag(stack.getTag());
            CompoundTag tag = newStack.getOrCreateTag();
            tag.putUUID("uuid", blockEntity.getUuid());
            if (blockEntity.getColor() != null)
                tag.putShort("color", (short) blockEntity.getColor().getId());
            player.setItemInHand(hand, newStack);
            player.playSound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1);
            return InteractionResult.SUCCESS;
        } else return this.useWithoutItem(level, pos, player);
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity)) return 0;
        return blockEntity.getPower(direction);
    }

    @Override
    public int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getSignal(pBlockAccess, pPos, pSide);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
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

            CompoundTag tag = stack.getOrCreateTag();
            if (!disallowed && tag.hasUUID("uuid")) {
                blockEntity.setUuid(tag.getUUID("uuid"));
                if (stack.hasCustomHoverName())
                    blockEntity.setName(stack.getHoverName());
                if (tag.contains("color", Tag.TAG_SHORT))
                    blockEntity.setColor(DyeColor.byId(tag.getShort("color")));
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
