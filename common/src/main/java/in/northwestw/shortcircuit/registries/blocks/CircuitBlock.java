package in.northwestw.shortcircuit.registries.blocks;

import com.mojang.serialization.MapCodec;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.data.CircuitLimitSavedData;
import in.northwestw.shortcircuit.registries.*;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.datacomponents.UUIDDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CircuitBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty COLORED = BooleanProperty.create("colored");

    public CircuitBlock(BlockBehaviour.Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(POWERED, false)
                .setValue(COLORED, false));
    }

    @Override
    protected @NotNull MapCodec<CircuitBlock> codec() {
        return Codecs.CIRCUIT.get();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        //builder.add(FACING, UP_POWER, DOWN_POWER, LEFT_POWER, RIGHT_POWER, FRONT_POWER, BACK_POWER);
        builder.add(FACING, POWERED, COLORED);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity instanceof CircuitBlockEntity circuitBlockEntity) {
            if (!player.isCreative() && circuitBlockEntity.isValid()) {
                ItemStack stack = new ItemStack(Blocks.CIRCUIT.get());
                stack.applyComponents(blockentity.collectComponents());
                ItemEntity itementity = new ItemEntity(
                        level, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5, stack
                );
                itementity.setDefaultPickUpDelay();
                level.addFreshEntity(itementity);
            }
            circuitBlockEntity.removeRuntime();

            UUID owner = circuitBlockEntity.getOwnerUuid();
            MinecraftServer server = player.getServer();
            if (owner != null && server != null)
                CircuitLimitSavedData.getRuntimeData(server).remove(owner);
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            blockEntity.removeRuntime();

            UUID owner = blockEntity.getOwnerUuid();
            if (owner != null)
                CircuitLimitSavedData.getRuntimeData(level).remove(owner);
        }
        super.wasExploded(level, pos, explosion);
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
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CircuitBlockEntity(pos, state);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return type == BlockEntities.CIRCUIT.get() ? (pLevel, pPos, pState, blockEntity) -> ((CircuitBlockEntity) blockEntity).tick() : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            if (blockEntity.isFake()) return super.useWithoutItem(state, level, pos, player, hitResult);
            else {
                player.displayClientMessage(Component.translatable("action.circuit.reload"), true);
                CircuitBlockEntity.RuntimeReloadResult result = blockEntity.reloadRuntime();
                if (result != CircuitBlockEntity.RuntimeReloadResult.FAIL_NO_SERVER)
                    player.displayClientMessage(Component.translatable(result.getTranslationKey()).withStyle(Style.EMPTY.withColor(result.isGood() ? 0x00ff00 : 0xff0000)), true);
            }
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (stack.is(Items.POKING_STICK.get()) || stack.is(Items.LABELLING_STICK.get())) return InteractionResult.PASS; // handled by item
        else if ((stack.is(Items.CIRCUIT.get()) || stack.is(Items.INTEGRATED_CIRCUIT.get())) && !player.isCrouching() && !player.isShiftKeyDown() && level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity && blockEntity.isValid()) {
            ItemStack newStack = new ItemStack(Items.CIRCUIT.get(), stack.getCount());
            newStack.applyComponents(stack.getComponents());
            newStack.set(DataComponents.UUID.get(), new UUIDDataComponent(blockEntity.getUuid()));
            if (blockEntity.getColor() != null)
                newStack.set(DataComponents.SHORT.get(), (short) blockEntity.getColor().getId());
            newStack.set(net.minecraft.core.component.DataComponents.ITEM_MODEL, ShortCircuitCommon.rl("circuit"));
            player.setItemInHand(hand, newStack);
            player.playSound(SoundEvents.BEACON_ACTIVATE, 0.5f, 1);
            return InteractionResult.SUCCESS.heldItemTransformedTo(newStack);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, result);
    }

    @Override
    protected boolean isSignalSource(BlockState pState) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        if (!(level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity)) return 0;
        return blockEntity.getPower(direction);
    }

    @Override
    protected int getDirectSignal(BlockState pBlockState, BlockGetter pBlockAccess, BlockPos pPos, Direction pSide) {
        return pBlockState.getSignal(pBlockAccess, pPos, pSide);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, orientation, movedByPiston);
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            //ShortCircuitCommon.LOGGER.info("neighbor changed for circuit at {}", pos);
            blockEntity.updateInputs();
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (state.hasBlockEntity() && level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) {
            boolean disallowed = false;
            MinecraftServer server = level.getServer();
            if (server != null && placer instanceof Player player) {
                blockEntity.setOwnerUuid(placer.getUUID());
                CircuitLimitSavedData data = CircuitLimitSavedData.getRuntimeData(server);
                if (!data.canAdd(placer.getUUID())) {
                    player.displayClientMessage(Component.translatable("warning.circuit.place.circuit_board.limit").withStyle(Style.EMPTY.withColor(0xffff00)), true);
                    disallowed = true;
                } else
                    data.add(placer.getUUID());
            }

            if (!disallowed && stack.has(DataComponents.UUID.get())) {
                blockEntity.setUuid(stack.get(DataComponents.UUID.get()).uuid());
                if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME))
                    blockEntity.setName(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME));
                if (stack.has(DataComponents.SHORT.get()))
                    blockEntity.setColor(DyeColor.byId(stack.get(DataComponents.SHORT.get())));
                if (!level.dimension().equals(Constants.CIRCUIT_BOARD_DIMENSION)) {
                    blockEntity.reloadRuntime();
                    blockEntity.updateInputs();
                } else if (placer instanceof Player player)
                    player.displayClientMessage(Component.translatable("warning.circuit.place.circuit_board").withStyle(Style.EMPTY.withColor(0xffff00)), true);
            }
        }
        super.setPlacedBy(level, pos, state, placer, stack);
    }
}
