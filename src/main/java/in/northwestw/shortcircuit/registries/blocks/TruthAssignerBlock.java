package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class TruthAssignerBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<TruthAssignerBlock> CODEC = simpleCodec(TruthAssignerBlock::new);
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    public TruthAssignerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH)
                .setValue(LIT, false)
        );
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }
}
