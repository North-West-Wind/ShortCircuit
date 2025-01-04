package in.northwestw.shortcircuit.registries.blocks;

import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBoardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CircuitBoardBlock extends Block implements EntityBlock {
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
        return state.getValue(MODE).equals(Mode.INPUT);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(MODE).equals(Mode.INPUT) ? state.getValue(POWER) : 0;
    }

    @Override
    protected int getDirectSignal(BlockState pState, BlockGetter pLevel, BlockPos pPos, Direction pDirection) {
        return super.getSignal(pState, pLevel, pPos, pDirection);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!state.getValue(MODE).equals(Mode.OUTPUT)) return;
        if (neighborBlock != Blocks.CIRCUIT_BOARD.get()) {
            CircuitBoardBlockEntity blockEntity = (CircuitBoardBlockEntity) level.getBlockEntity(pos);
            int signal = level.getSignal(neighborPos, this.getDirectionFromPosToPos(pos, neighborPos));
            ShortCircuit.LOGGER.debug("neighbor {}, direction {}, signal {}", neighborPos, this.getDirectionFromPosToPos(neighborPos, pos), signal);
            blockEntity.updateCircuitBlock(signal, state.getValue(DIRECTION));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("tooltip.short_circuit.circuit_board").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(0x7f7f7f)).withItalic(true)));
        super.appendHoverText(stack, context, components, flag);
    }

    private Direction getDirectionFromPosToPos(BlockPos a, BlockPos b) {
        if (a.getX() != b.getX()) return a.getX() - b.getX() > 0 ? Direction.WEST : Direction.EAST;
        if (a.getY() != b.getY()) return a.getY() - b.getY() > 0 ? Direction.DOWN : Direction.UP;
        return a.getZ() - b.getZ() > 0 ? Direction.NORTH : Direction.SOUTH;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitBoardBlockEntity(pos, state);
    }

    public enum RelativeDirection implements StringRepresentable {
        UP("up", 0),
        DOWN("down", 1),
        LEFT("left", 2),
        RIGHT("right", 3),
        FRONT("front", 4),
        BACK("back", 5);

        final String name;
        final byte id;

        RelativeDirection(String name, int id) {
            this.name = name;
            this.id = (byte) id;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public byte getId() {
            return id;
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
