package in.northwestw.shortcircuit.registries.blocks.common;

import in.northwestw.shortcircuit.registries.DataComponents;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.apache.commons.compress.utils.Lists;

import java.util.List;

public abstract class CommonCircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final IntegerProperty COLOR = IntegerProperty.create("color", 0, 16);

    public CommonCircuitBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(COLOR, 16));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        //builder.add(FACING, UP_POWER, DOWN_POWER, LEFT_POWER, RIGHT_POWER, FRONT_POWER, BACK_POWER);
        builder.add(FACING, POWERED, COLOR);
    }

    public List<Component> extraTooltip(ItemStack stack) {
        List<Component> list = Lists.newArrayList();
        if (stack.has(DataComponents.UUID.get())) {
            list.add(Component.translatable("tooltip.short_circuit.circuit", stack.get(DataComponents.UUID.get()).uuid().toString()).withColor(0x7f7f7f));
        }
        if (stack.has(DataComponents.SHORT.get())) {
            DyeColor color = DyeColor.byId(stack.get(DataComponents.SHORT.get()));
            list.add(Component.translatable("tooltip.short_circuit.circuit.color", Component.translatable("color.minecraft." + color.getName())).withColor(color.getTextColor()));
        }
        return list;
    }
}
