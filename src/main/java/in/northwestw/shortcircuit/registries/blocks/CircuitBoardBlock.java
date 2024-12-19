package in.northwestw.shortcircuit.registries.blocks;

import com.google.common.collect.Lists;
import net.minecraft.util.StringRepresentable;
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
