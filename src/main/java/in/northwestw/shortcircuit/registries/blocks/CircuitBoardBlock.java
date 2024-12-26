package in.northwestw.shortcircuit.registries.blocks;

import com.google.common.collect.Lists;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBoardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class CircuitBoardBlock extends Block {
    public static final EnumProperty<RelativeDirection> DIRECTION = EnumProperty.create("rel_dir", RelativeDirection.class);
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DIRECTION, ANNOTATED, MODE, POWER);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return state.getValue(MODE) == Mode.INPUT;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(MODE) == Mode.INPUT ? state.getValue(POWER) : 0;
    }

    @Override
    protected int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return super.getSignal(pState, pLevel, pPos, pDirection);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (state.getValue(MODE) != Mode.OUTPUT) return;
        if (neighborBlock != Blocks.CIRCUIT_BOARD.get()) {
            RelativeDirection direction = state.getValue(DIRECTION);
            IntegerProperty property = switch (direction) {
                case UP -> CircuitBlock.UP_POWER;
                case DOWN -> CircuitBlock.DOWN_POWER;
                case LEFT -> CircuitBlock.LEFT_POWER;
                case RIGHT -> CircuitBlock.RIGHT_POWER;
                case FRONT -> CircuitBlock.FRONT_POWER;
                case BACK -> CircuitBlock.BACK_POWER;
            };
            CircuitBoardBlockEntity blockEntity = (CircuitBoardBlockEntity) level.getBlockEntity(pos);
            blockEntity.updateCircuitBlock(level.getSignal(neighborPos, this.getDirectionFromPosToPos(neighborPos, pos)), property);
        }
    }

    private Direction getDirectionFromPosToPos(BlockPos a, BlockPos b) {
        if (a.getX() != b.getX()) return a.getX() - b.getX() == 1 ? Direction.WEST : Direction.EAST;
        if (a.getY() != b.getY()) return a.getY() - b.getY() == 1 ? Direction.DOWN : Direction.UP;
        return a.getZ() - b.getZ() == 1 ? Direction.NORTH : Direction.SOUTH;
    }

    public enum RelativeDirection implements StringRepresentable {
        UP("up"),
        DOWN("down"),
        LEFT("left"),
        RIGHT("right"),
        FRONT("front"),
        BACK("back");

        final String name;

        RelativeDirection(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
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
