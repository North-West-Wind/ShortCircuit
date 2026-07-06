package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.registries.*;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.common.CommonCircuitBlock;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

//~ if =1.21.1 'InteractionResult' -> 'ItemInteractionResult'
import net.minecraft.world.InteractionResult;

//? if >=1.21.4 {
import in.northwestw.shortcircuit.ShortCircuitCommon;
import net.minecraft.world.level.redstone.Orientation;
//? } else {
/*import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;
*///? }

//? if <=1.19.2 {
/*import com.mojang.math.Vector3f;
*///? }

public class IntegratedCircuitBlock extends CommonCircuitBlock {
    //? if >=1.21.4 {
    public static final DustParticleOptions PARTICLE = new DustParticleOptions(0xFFDD00, 1.0F);
    //? } elif >=1.20.1 {
    /*public static final DustParticleOptions PARTICLE = new DustParticleOptions(Vec3.fromRGB24(0xFFDD00).toVector3f(), 1.0F);
    *///? } else
    //public static final DustParticleOptions PARTICLE = new DustParticleOptions(new Vector3f(Vec3.fromRGB24(0xFFDD00)), 1.0F);

    public IntegratedCircuitBlock(Properties pProperties) {
        super(pProperties);
    }

    //? if >=1.21.1 {
    @Override
    protected MapCodec<IntegratedCircuitBlock> codec() {
        return Codecs.INTEGRATED_CIRCUIT.get();
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    //? } else {
    /*@Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
    *///? }
        if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) {
            if (!player.isCreative() && blockEntity.isValid()) {
                ItemStack stack = new ItemStack(Blocks.INTEGRATED_CIRCUIT.get());
                //? if >=1.21.1 {
                stack.applyComponents(blockEntity.collectComponents());
                //? } else
                //blockEntity.saveToItem(stack);
                ItemEntity itementity = new ItemEntity(
                        level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, stack
                );
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
        }

        //? if >=1.21.1 {
        return super.playerWillDestroy(level, pos, state, player);
        //? } else
        //super.playerWillDestroy(level, pos, state, player);
    }

    //? if <=1.20.1 {
    /*@Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return this.useItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hit);
    }
    *///? }

    // @Override can be omitted so this works across versions
    //~ if =1.21.1 'InteractionResult' -> 'ItemInteractionResult'
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if ((stack.is(Items.CIRCUIT.get()) || stack.is(Items.INTEGRATED_CIRCUIT.get())) && !player.isCrouching() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity && blockEntity.isValid()) {
            ItemStack newStack = new ItemStack(Items.INTEGRATED_CIRCUIT.get(), stack.getCount());
            //? if >=1.21.1 {
            newStack.applyComponents(stack.getComponents());
            newStack.set(DataComponents.UUID.get(), new UUIDDataComponent(blockEntity.getUuid()));
            newStack.set(DataComponents.SHORT.get(), state.getValue(COLOR).shortValue());
            //? } else {
            /*if (stack.hasTag()) newStack.setTag(stack.getTag());
            CompoundTag tag = newStack.getOrCreateTag();
            tag.putUUID("uuid", blockEntity.getUuid());
            tag.putShort("color", level.getBlockState(pos).getValue(COLOR).shortValue());
            *///? }
            //? if >=1.21.4 {
            newStack.set(net.minecraft.core.component.DataComponents.ITEM_MODEL, ShortCircuitCommon.rl("integrated_circuit"));
            //? }
            player.setItemInHand(hand, newStack);
            player.playSound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1);
            //? if >=1.21.4 {
            return InteractionResult.SUCCESS.heldItemTransformedTo(newStack);
            //? } elif >=1.21.1 {
            /*return ItemInteractionResult.SUCCESS;
            *///? } else
            //return InteractionResult.SUCCESS;
        }
        //? if >=1.21.1 {
        return super.useItemOn(stack, state, level, pos, player, hand, result);
        //? } else
        //return null;
    }

    @Override
    //? if >=1.21.4 {
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
    //? } elif >=1.21.1 {
    /*protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    *///? } else {
    /*public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    *///? }
        if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity)
            blockEntity.updateInputs();
    }

    @Override
    //~ if <=1.20.1 'protected' -> 'public'
    protected boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    //~ if <=1.20.1 'protected' -> 'public'
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return ((IntegratedCircuitBlockEntity) level.getBlockEntity(pos)).getPower(direction);
    }

    @Override
    //~ if <=1.20.1 'protected' -> 'public'
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getSignal(level, pos, direction);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IntegratedCircuitBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntityTypes.INTEGRATED_CIRCUIT.get() ? (pLevel, pos, pState, blockEntity) -> ((IntegratedCircuitBlockEntity) pLevel.getBlockEntity(pos)).tick() : null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) {
            //? if >=1.21.1 {
            if (stack.has(DataComponents.UUID.get())) {
                blockEntity.setUuid(stack.get(DataComponents.UUID.get()).uuid());
                if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME))
                    blockEntity.setName(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME).getString());
                if (stack.has(DataComponents.SHORT.get()))
                    level.setBlock(pos, state.setValue(COLOR, stack.get(DataComponents.SHORT.get()).intValue()), Block.UPDATE_CLIENTS);
            //? } else {
            /*CompoundTag tag = stack.getOrCreateTag();
            if (tag.hasUUID("uuid")) {
                blockEntity.setUuid(tag.getUUID("uuid"));
                if (stack.hasCustomHoverName())
                    blockEntity.setName(stack.getHoverName().getString());
                if (tag.contains("color", Tag.TAG_SHORT))
                    level.setBlock(pos, state.setValue(COLOR, (int) tag.getShort("color")), Block.UPDATE_CLIENTS);
            *///? }
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
            //? if >=1.21.4 {
            if (!level.getBlockState(blockpos).isSolidRender()) {
            //? } else
            //if (!level.getBlockState(blockpos).isSolidRender(level, pos)) {
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
