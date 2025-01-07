package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.registries.BlockEntities;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.TruthAssignerBlockEntity;
import in.northwestw.shortcircuit.registries.menus.TruthAssignerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class TruthAssignerBlock extends HorizontalDirectionalBlock implements EntityBlock {
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

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TruthAssignerBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntities.TRUTH_ASSIGNER.get() ? (pLevel, pPos, pState, blockEntity) -> ((TruthAssignerBlockEntity) blockEntity).tick() : null;
    }

    @Override
    protected @Nullable MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof TruthAssignerBlockEntity blockEntity) {
            return new SimpleMenuProvider(
                    (containerId, inventory, player) -> new TruthAssignerMenu(containerId, inventory, ContainerLevelAccess.create(level, pos), blockEntity, blockEntity.getContainerData()),
                    Component.translatable("menu.title.short_circuit.truth_assigner"));
        } else return null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult result) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(state.getMenuProvider(level, pos));
        }
        return super.useWithoutItem(state, level, pos, player, result);
    }
}
