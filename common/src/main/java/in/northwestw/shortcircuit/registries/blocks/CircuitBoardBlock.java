package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.Codecs;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBoardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import org.jetbrains.annotations.Nullable;

public class CircuitBoardBlock extends Block implements EntityBlock {
    public static final EnumProperty<RelativeDirection> DIRECTION = RelativeDirection.REL_DIRECTION;
    public static final BooleanProperty ANNOTATED = BooleanProperty.create("annotated");
    public static final EnumProperty<Mode> MODE = EnumProperty.create("mode", Mode.class);
    public static final IntegerProperty POWER = BlockStateProperties.POWER;

    public CircuitBoardBlock(BlockBehaviour.Properties properties) {
        this(properties, RelativeDirection.FRONT, false);
    }

    public CircuitBoardBlock(BlockBehaviour.Properties properties, RelativeDirection direction, boolean annotated) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition
                        .any()
                        .setValue(DIRECTION, direction)
                        .setValue(ANNOTATED, annotated)
                        .setValue(MODE, Mode.NONE)
                        .setValue(POWER, 0)
        );
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return Codecs.CIRCUIT_BOARD.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION, ANNOTATED, MODE, POWER);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return state.getValue(MODE) != Mode.NONE;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(MODE) == Mode.INPUT && direction == DirectionHelper.circuitBoardFixedDirection(state.getValue(DIRECTION)) ? state.getValue(POWER) : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return this.getSignal(state, level, pos, direction);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (state.getValue(MODE) != Mode.OUTPUT || neighborBlock == Blocks.CIRCUIT_BOARD.get()) return;
        if (level.getBlockEntity(pos) instanceof CircuitBoardBlockEntity blockEntity) {
            Direction direction = DirectionHelper.circuitBoardFixedDirection(state.getValue(DIRECTION)).getOpposite();
            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);
            if (!neighborState.is(Blocks.CIRCUIT_BOARD.get()) && !neighborState.isAir()) {
                blockEntity.updateCircuitBlock(level.getSignal(neighborPos, direction), state.getValue(DIRECTION));
            }
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitBoardBlockEntity(pos, state);
    }

    public enum Mode implements StringRepresentable {
        NONE("none"),
        INPUT("input"),
        OUTPUT("output");

        final String name;
        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Mode nextMode() {
            switch (this) {
                case NONE -> {
                    return INPUT;
                }
                case INPUT -> {
                    return OUTPUT;
                }
                default -> {
                    return NONE;
                }
            }
        }
    }
}
