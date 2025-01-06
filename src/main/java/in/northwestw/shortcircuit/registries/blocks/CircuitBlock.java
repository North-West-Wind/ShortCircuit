package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.Items;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<CircuitBlock> CODEC = simpleCodec(CircuitBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public CircuitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    protected @NotNull MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        //builder.add(FACING, UP_POWER, DOWN_POWER, LEFT_POWER, RIGHT_POWER, FRONT_POWER, BACK_POWER);
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof CircuitBlockEntity circuitBlockEntity) {
            ItemStack itemstack = new ItemStack(Blocks.CIRCUIT);
            if (!player.isCreative() && circuitBlockEntity.isValid()) {
                itemstack.applyComponents(blockentity.collectComponents());
                ItemEntity itementity = new ItemEntity(
                        level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, itemstack
                );
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext ctx, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, components, flag);
        if (stack.has(DataComponents.UUID)) {
            components.add(Component.translatable("tooltip.short_circuit.circuit", stack.get(DataComponents.UUID).uuid().toString()));
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntities.CIRCUIT.get() ? CircuitBlockEntity::tick : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        player.displayClientMessage(Component.translatable("action.circuit.reload"), true);
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) blockEntity.reloadRuntime();
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (stack.is(Items.POKING_STICK)) return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION; // handled by poking stick
        return super.useItemOn(stack, state, level, pos, player, hand, result);
    }

    @Override
    protected boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity)) return 0;
        return blockEntity.getPower(direction);
    }

    @Override
    protected int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getSignal(pBlockAccess, pPos, pSide);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        Direction direction = DirectionHelper.getDirectionFromPosToPos(pos, neighborPos);
        RelativeDirection relDir = DirectionHelper.directionToRelativeDirection(state.getValue(FACING), direction);
        int signal = level.getSignal(neighborPos, direction);
        ((CircuitBlockEntity) level.getBlockEntity(pos)).updateRuntimeBlock(signal, relDir);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity)
            blockEntity.removeRuntime();
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity)
            blockEntity.reloadRuntime();
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}
