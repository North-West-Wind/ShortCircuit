package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.properties.DirectionHelper;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.Items;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntegratedCircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<CircuitBlock> CODEC = simpleCodec(CircuitBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public IntegratedCircuitBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) {
            if (!player.isCreative() && blockEntity.isValid()) {
                ItemStack stack = new ItemStack(Blocks.INTEGRATED_CIRCUIT);
                stack.applyComponents(blockEntity.collectComponents());
                ItemEntity itementity = new ItemEntity(
                        level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, stack
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
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if ((stack.is(Items.CIRCUIT) || stack.is(Items.INTEGRATED_CIRCUIT)) && !player.isCrouching() && level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity && blockEntity.isValid()) {
            ItemStack newStack = new ItemStack(Items.INTEGRATED_CIRCUIT.get(), stack.getCount());
            newStack.applyComponents(stack.getComponents());
            newStack.set(DataComponents.UUID, new UUIDDataComponent(blockEntity.getUuid()));
            player.playSound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1);
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, result);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        Direction direction = DirectionHelper.getDirectionFromPosToPos(pos, neighborPos);
        RelativeDirection relDir = DirectionHelper.directionToRelativeDirection(state.getValue(FACING), direction);
        int signal = level.getSignal(neighborPos, direction);
        ((IntegratedCircuitBlockEntity) level.getBlockEntity(pos)).setInputAndUpdate(relDir, signal);
        level.updateNeighborsAtExceptFromFacing(pos, state.getBlock(), DirectionHelper.getDirectionFromPosToPos(pos, neighborPos));
    }

    @Override
    protected boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return ((IntegratedCircuitBlockEntity) level.getBlockEntity(pos)).getPower(direction);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IntegratedCircuitBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity)
            if (stack.has(DataComponents.UUID)) {
                blockEntity.setUuid(stack.get(DataComponents.UUID).uuid());
                blockEntity.getInputSignals();
            }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}
