package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.RepeaterBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<RepeaterBlock> CODEC = simpleCodec(RepeaterBlock::new);

    public static final IntegerProperty UP_POWER = IntegerProperty.create("up_power", 0, 15);
    public static final IntegerProperty DOWN_POWER = IntegerProperty.create("down_power", 0, 15);
    public static final IntegerProperty LEFT_POWER = IntegerProperty.create("left_power", 0, 15);
    public static final IntegerProperty RIGHT_POWER = IntegerProperty.create("right_power", 0, 15);
    public static final IntegerProperty FRONT_POWER = IntegerProperty.create("front_power", 0, 15);
    public static final IntegerProperty BACK_POWER = IntegerProperty.create("back_power", 0, 15);

    public CircuitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(UP_POWER, 0)
                .setValue(DOWN_POWER, 0)
                .setValue(LEFT_POWER, 0)
                .setValue(RIGHT_POWER, 0)
                .setValue(FRONT_POWER, 0)
                .setValue(BACK_POWER, 0));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, UP_POWER, DOWN_POWER, LEFT_POWER, RIGHT_POWER, FRONT_POWER, BACK_POWER);
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
            components.add(Component.translatable("tooltip.short_circuit.circuit", stack.get(DataComponents.UUID).uuid()));
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
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof CircuitBlockEntity) {
            CircuitBlockEntity blockEntity = (CircuitBlockEntity) be;
            blockEntity.reloadRuntime();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        switch (direction) {
            case UP: return state.getValue(UP_POWER);
            case DOWN: return state.getValue(DOWN_POWER);
        }
        int data2d = state.getValue(FACING).get2DDataValue();
        int offset = direction.get2DDataValue() - data2d;
        if (offset < 0) offset += 4;
        return switch (offset) {
            case 0 -> state.getValue(FRONT_POWER);
            case 1 -> state.getValue(RIGHT_POWER);
            case 2 -> state.getValue(BACK_POWER);
            case 3 -> state.getValue(LEFT_POWER);
            default -> 0;
        };
    }

    @Override
    protected int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getSignal(pBlockAccess, pPos, pSide);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        Direction direction = this.getDirectionFromPosToPos(neighborPos, pos);
        int signal = level.getSignal(neighborPos, direction);
        ((CircuitBlockEntity) level.getBlockEntity(pos)).updateRuntimeBlock(signal, this.directionToRelativeDirection(state.getValue(FACING), direction.getOpposite()));
    }

    private Direction getDirectionFromPosToPos(BlockPos a, BlockPos b) {
        if (a.getX() != b.getX()) return a.getX() - b.getX() == 1 ? Direction.WEST : Direction.EAST;
        if (a.getY() != b.getY()) return a.getY() - b.getY() == 1 ? Direction.DOWN : Direction.UP;
        return a.getZ() - b.getZ() == 1 ? Direction.NORTH : Direction.SOUTH;
    }

    private CircuitBoardBlock.RelativeDirection directionToRelativeDirection(Direction facing, Direction direction) {
        if (direction == Direction.UP) return CircuitBoardBlock.RelativeDirection.UP;
        if (direction == Direction.DOWN) return CircuitBoardBlock.RelativeDirection.DOWN;
        int offset = direction.get2DDataValue() - facing.get2DDataValue();
        if (offset < 0) offset += 4;
        return switch (offset) {
            case 0 -> CircuitBoardBlock.RelativeDirection.FRONT;
            case 1 -> CircuitBoardBlock.RelativeDirection.RIGHT;
            case 2 -> CircuitBoardBlock.RelativeDirection.BACK;
            default ->  CircuitBoardBlock.RelativeDirection.LEFT;
        };
    }
}
