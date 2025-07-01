package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.registries.*;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntegratedCircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty COLORED = BooleanProperty.create("colored");
    public static final DustParticleOptions PARTICLE = new DustParticleOptions(0xFFDD00, 1.0F);

    public IntegratedCircuitBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(COLORED, false));
    }

    @Override
    protected MapCodec<IntegratedCircuitBlock> codec() {
        return Codecs.INTEGRATED_CIRCUIT.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED, COLORED);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) {
            if (!player.isCreative() && blockEntity.isValid()) {
                ItemStack stack = new ItemStack(Blocks.INTEGRATED_CIRCUIT.get());
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
        if (stack.has(DataComponents.UUID.get())) {
            components.add(Component.translatable("tooltip.short_circuit.circuit", stack.get(DataComponents.UUID.get()).uuid().toString()).withColor(0x7f7f7f));
        }
        if (stack.has(DataComponents.SHORT.get())) {
            DyeColor color = DyeColor.byId(stack.get(DataComponents.SHORT.get()));
            components.add(Component.translatable("tooltip.short_circuit.circuit.color", Component.translatable("color.minecraft." + color.getName())).withColor(color.getTextColor()));
        }
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if ((stack.is(Items.CIRCUIT.get()) || stack.is(Items.INTEGRATED_CIRCUIT.get())) && !player.isCrouching() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity && blockEntity.isValid()) {
            ItemStack newStack = new ItemStack(Items.INTEGRATED_CIRCUIT.get(), stack.getCount());
            newStack.applyComponents(stack.getComponents());
            newStack.set(DataComponents.UUID.get(), new UUIDDataComponent(blockEntity.getUuid()));
            if (blockEntity.getColor() != null)
                newStack.set(DataComponents.SHORT.get(), (short) blockEntity.getColor().getId());
            newStack.set(net.minecraft.core.component.DataComponents.ITEM_MODEL, ShortCircuitCommon.rl("integrated_circuit"));
            player.setItemInHand(hand, newStack);
            player.playSound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1);
            return InteractionResult.SUCCESS.heldItemTransformedTo(newStack);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, result);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity)
            blockEntity.updateInputs();
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
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getSignal(level, pos, direction);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IntegratedCircuitBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntities.INTEGRATED_CIRCUIT.get() ? (pLevel, pos, pState, blockEntity) -> ((IntegratedCircuitBlockEntity) pLevel.getBlockEntity(pos)).tick() : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity)
            if (stack.has(DataComponents.UUID.get())) {
                blockEntity.setUuid(stack.get(DataComponents.UUID.get()).uuid());
                if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME))
                    blockEntity.setName(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
                if (stack.has(DataComponents.SHORT.get()))
                    blockEntity.setColor(DyeColor.byId(stack.get(DataComponents.SHORT.get())));
                blockEntity.updateInputs();
            }
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(POWERED) && (!(level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) || !blockEntity.isHidden())) {
            spawnParticles(level, pos);
        }
    }

    // copied from RedstoneOreBlock
    private static void spawnParticles(Level level, BlockPos pos) {
        RandomSource randomsource = level.random;
        for (Direction direction : Direction.values()) {
            BlockPos blockpos = pos.relative(direction);
            if (!level.getBlockState(blockpos).isSolidRender()) {
                Direction.Axis direction$axis = direction.getAxis();
                double d1 = direction$axis == Direction.Axis.X ? 0.5 + 0.5625 * (double)direction.getStepX() : (double)randomsource.nextFloat();
                double d2 = direction$axis == Direction.Axis.Y ? 0.5 + 0.5625 * (double)direction.getStepY() : (double)randomsource.nextFloat();
                double d3 = direction$axis == Direction.Axis.Z ? 0.5 + 0.5625 * (double)direction.getStepZ() : (double)randomsource.nextFloat();
                level.addParticle(
                        PARTICLE, (double)pos.getX() + d1, (double)pos.getY() + d2, (double)pos.getZ() + d3, 0.0, 0.0, 0.0
                );
            }
        }
    }
}
