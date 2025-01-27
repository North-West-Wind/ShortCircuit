package in.northwestw.shortcircuit.registries.blocks;

import in.northwestw.shortcircuit.registries.*;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IntegratedCircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty COLORED = BooleanProperty.create("colored");
    public static final DustParticleOptions PARTICLE = new DustParticleOptions(Vec3.fromRGB24(0xFFDD00).toVector3f(), 1.0F);

    public IntegratedCircuitBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(COLORED, false));
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
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) {
            if (!player.isCreative() && blockEntity.isValid()) {
                ItemStack stack = new ItemStack(Blocks.INTEGRATED_CIRCUIT.get());
                blockEntity.saveToItem(stack);
                ItemEntity itementity = new ItemEntity(
                        level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, stack
                );
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        }

        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void appendHoverText(ItemStack stack, BlockGetter level, List<Component> components, TooltipFlag flag) {
        super.appendHoverText(stack, level, components, flag);
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.hasUUID("uuid")) {
            components.add(Component.translatable("tooltip.short_circuit.circuit", tag.getUUID("uuid").toString()).withStyle(Style.EMPTY.withColor(0x7f7f7f)));
        }
        if (tag.contains("color", CompoundTag.TAG_SHORT)) {
            DyeColor color = DyeColor.byId(tag.getShort("color"));
            components.add(Component.translatable("tooltip.short_circuit.circuit.color", Component.translatable("color.minecraft." + color.getName())).withStyle(Style.EMPTY.withColor(color.getTextColor())));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        ItemStack stack = player.getItemInHand(hand);
        if (!stack.isEmpty()) {
            InteractionResult result = this.useItemOn(player.getItemInHand(hand), level, pos, player, hand);
            if (result != null) return result;
        }
        return super.use(state, level, pos, player, hand, hitResult);
    }

    protected InteractionResult useItemOn(ItemStack stack, Level level, BlockPos pos, Player player, InteractionHand hand) {
        if ((stack.is(Items.CIRCUIT.get()) || stack.is(Items.INTEGRATED_CIRCUIT.get())) && !player.isCrouching() && level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity && blockEntity.isValid()) {
            ItemStack newStack = new ItemStack(Items.INTEGRATED_CIRCUIT.get(), stack.getCount());
            if (stack.hasTag()) newStack.setTag(stack.getTag());
            CompoundTag tag = newStack.getOrCreateTag();
            tag.putUUID("uuid", blockEntity.getUuid());
            if (blockEntity.getColor() != null)
                tag.putShort("color", (short) blockEntity.getColor().getId());
            player.setItemInHand(hand, newStack);
            player.playSound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1);
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity)
            blockEntity.updateInputs();
    }

    @Override
    public boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return ((IntegratedCircuitBlockEntity) level.getBlockEntity(pos)).getPower(direction);
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
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
        if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.hasUUID("uuid")) {
                blockEntity.setUuid(tag.getUUID("uuid"));
                if (stack.hasCustomHoverName())
                    blockEntity.setName(stack.getHoverName());
                if (tag.contains("color", Tag.TAG_SHORT))
                    blockEntity.setColor(DyeColor.byId(tag.getShort("color")));
                blockEntity.updateInputs();
            }
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
            if (!level.getBlockState(blockpos).isSolidRender(level, pos)) {
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
