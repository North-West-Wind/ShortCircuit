package in.northwestw.shortcircuit.registries.blockentities.common;

import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class CircuitProperties {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);
}
