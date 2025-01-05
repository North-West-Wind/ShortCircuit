package in.northwestw.shortcircuit.registries.items;

import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.ShortCircuit;
import in.northwestw.shortcircuit.data.CircuitSavedData;
import in.northwestw.shortcircuit.data.Octolet;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import in.northwestw.shortcircuit.registries.datacomponents.LastPosDataComponent;
import in.northwestw.shortcircuit.registries.datacomponents.ShortDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PokingStickItem extends Item {
    public PokingStickItem(Properties properties) {
        super(properties);
        properties.component(DataComponents.SHORT, new ShortDataComponent((short) 4));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        this.cycleBlockSize(stack, player);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (state.is(Blocks.CIRCUIT_BOARD)) return this.useOnCircuitBoardBlock(context);
        if (!state.is(Blocks.CIRCUIT)) return this.cycleBlockSize(stack, player);
        BlockEntity be = level.getBlockEntity(pos);
        if (player == null) return InteractionResult.FAIL;
        if (be instanceof CircuitBlockEntity) return this.useOnCircuitBlock(context);

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private InteractionResult useOnCircuitBlock(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        CircuitBlockEntity blockEntity = (CircuitBlockEntity) level.getBlockEntity(context.getClickedPos());
        ItemStack stack = context.getItemInHand();
        if (player.isCrouching()) {
            blockEntity.setHidden(!blockEntity.isHidden());
        } else {
            DimensionTransition transition;
            UUID uuid = blockEntity.getUuid();
            stack.set(DataComponents.LAST_POS, new LastPosDataComponent(level.dimension().location(), player.position()));
            if (uuid == null) {
                blockEntity.setBlockSize(this.getBlockSize(stack));
                transition = this.getNewDimensionTransition(this.getBlockSize(stack), level, blockEntity);
            } else {
                transition = this.getDimensionTransition(uuid, level);
            }

            if (transition != null) player.changeDimension(transition);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private InteractionResult useOnCircuitBoardBlock(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player.isCrouching()) {
            BlockState state = level.getBlockState(context.getClickedPos());
            level.setBlockAndUpdate(context.getClickedPos(), state.setValue(CircuitBoardBlock.MODE, state.getValue(CircuitBoardBlock.MODE).nextMode()));
        } else {
            ItemStack stack = context.getItemInHand();
            if (stack.has(DataComponents.LAST_POS)) {
                MinecraftServer server = level.getServer();
                if (server == null) return InteractionResult.CONSUME;
                LastPosDataComponent component = stack.get(DataComponents.LAST_POS);
                ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, component.rl()));
                player.changeDimension(new DimensionTransition(serverLevel, component.pos(), Vec3.ZERO, 0, 0, DimensionTransition.DO_NOTHING));
                stack.remove(DataComponents.LAST_POS);
            } else {
                MinecraftServer server = level.getServer();
                if (server == null) return InteractionResult.CONSUME;
                ServerPlayer serverPlayer = (ServerPlayer) player;
                ServerLevel serverLevel = server.getLevel(serverPlayer.getRespawnDimension());
                BlockPos respawn = serverPlayer.getRespawnPosition();
                if (respawn == null) respawn = serverLevel.getSharedSpawnPos();
                player.changeDimension(new DimensionTransition(serverLevel, respawn.getCenter(), Vec3.ZERO, 0, 0, DimensionTransition.DO_NOTHING));
            }
        }
        return InteractionResult.SUCCESS;
    }

    private short getBlockSize(ItemStack stack) {
        ShortDataComponent component = stack.get(DataComponents.SHORT);
        return component == null ? 4 : component.value();
    }

    private InteractionResult cycleBlockSize(ItemStack stack, Player player) {
        short old = stack.getOrDefault(DataComponents.SHORT, new ShortDataComponent((short) 4)).value();
        short newVal = old == 256 ? 4 : (short) (old * 2);
        if (old == 256) stack.set(DataComponents.SHORT, new ShortDataComponent(newVal));
        else stack.set(DataComponents.SHORT, new ShortDataComponent(newVal));
        player.displayClientMessage(Component.translatable("action.poking_stick.change", newVal), true);
        player.playSound(SoundEvents.CHICKEN_EGG);
        return InteractionResult.SUCCESS;
    }

    private DimensionTransition getDimensionTransition(UUID uuid, Level level) {
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        ServerLevel circuitBoardLevel = server.getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        if (circuitBoardLevel == null) return null;
        CircuitSavedData data = CircuitSavedData.getCircuitBoardData(circuitBoardLevel);
        return new DimensionTransition(circuitBoardLevel, data.getCircuitStartingPos(uuid).offset(1, 1, 1).getCenter(), Vec3.ZERO, 0, 0, DimensionTransition.DO_NOTHING);
    }

    private DimensionTransition getNewDimensionTransition(short blockSize, Level level, CircuitBlockEntity blockEntity) {
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        ServerLevel circuitBoardLevel = server.getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        if (circuitBoardLevel == null) return null;
        // add the circuit to the world
        CircuitSavedData data = CircuitSavedData.getCircuitBoardData(circuitBoardLevel);
        int octoletIndex = data.octoletIndexForSize(blockSize);
        if (!data.octolets.containsKey(octoletIndex)) data.addOctolet(octoletIndex, new Octolet(blockSize));
        UUID uuid = UUID.randomUUID();
        data.addCircuit(uuid, octoletIndex);
        blockEntity.setUuid(uuid);
        // create the space in circuit board
        BlockPos startingPos = data.getCircuitStartingPos(uuid);
        for (int ii = 0; ii < blockSize; ii++) {
            for (int jj = 0; jj < blockSize; jj++) {
                for (int kk = 0; kk < blockSize; kk++) {
                    if ((ii != 0 && ii != blockSize - 1) && (jj != 0 && jj != blockSize - 1) && (kk != 0 && kk != blockSize - 1)) continue;
                    BlockState state = Blocks.CIRCUIT_BOARD.get().defaultBlockState();
                    if (jj == 0) state = state.setValue(CircuitBoardBlock.DIRECTION, RelativeDirection.DOWN);
                    else if (jj == blockSize - 1) state = state.setValue(CircuitBoardBlock.DIRECTION, RelativeDirection.UP);
                    else if (ii == 0) state = state.setValue(CircuitBoardBlock.DIRECTION, RelativeDirection.FRONT);
                    else if (ii == blockSize - 1) state = state.setValue(CircuitBoardBlock.DIRECTION, RelativeDirection.BACK);
                    else if (kk == 0) state = state.setValue(CircuitBoardBlock.DIRECTION, RelativeDirection.RIGHT);
                    else if (kk == blockSize - 1) state = state.setValue(CircuitBoardBlock.DIRECTION, RelativeDirection.LEFT);
                    // annotate middle 4
                    if (this.isMiddleFour(ii, jj, kk, blockSize)) state = state.setValue(CircuitBoardBlock.ANNOTATED, true);
                    circuitBoardLevel.setBlock(startingPos.offset(ii, jj, kk), state, 3);
                }
            }
        }
        return new DimensionTransition(circuitBoardLevel, startingPos.offset(1, 1, 1).getCenter(), Vec3.ZERO, 0, 0, DimensionTransition.DO_NOTHING);
    }

    private boolean isMiddleFour(int ii, int jj, int kk, short blockSize) {
        int halfBlockSize = blockSize / 2;
        boolean iiHalf = Math.abs((ii + 0.5) - halfBlockSize) == 0.5;
        boolean jjHalf = Math.abs((jj + 0.5) - halfBlockSize) == 0.5;
        boolean kkHalf = Math.abs((kk + 0.5) - halfBlockSize) == 0.5;
        return
                (iiHalf && jjHalf) && (kk == 0 || kk == blockSize - 1) ||
                (jjHalf && kkHalf) && (ii == 0 || ii == blockSize - 1) ||
                (kkHalf && iiHalf) && (jj == 0 || jj == blockSize - 1);
    }
}
