package in.northwestw.shortcircuit.registries.items;

import com.google.common.collect.Sets;
import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.data.CircuitSavedData;
import in.northwestw.shortcircuit.data.Octolet;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public class PokingStickItem extends Item {
    public PokingStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitresult.getType() == HitResult.Type.MISS) return this.cycleBlockSize(player.getItemInHand(hand), player);
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (state.is(Blocks.CIRCUIT_BOARD.get())) return this.useOnCircuitBoardBlock(context);
        if (state.is(Blocks.CIRCUIT.get())) return this.useOnCircuitBlock(context);
        if (state.is(Blocks.INTEGRATED_CIRCUIT.get())) return this.useOnIntegratedCircuitBlock(context);

        return this.cycleBlockSize(stack, player).getResult();
    }

    private InteractionResult useOnCircuitBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof CircuitBlockEntity blockEntity)) return InteractionResult.FAIL;
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player.isCrouching()) {
            blockEntity.setHidden(!blockEntity.isHidden());
        } else {
            DimensionTransition transition;
            UUID uuid = blockEntity.getUuid();
            CompoundTag tag = stack.getOrCreateTag();
            tag.putString("lastPosDim", level.dimension().location().toString());
            tag.put("lastPos", NbtUtils.writeBlockPos(player.blockPosition()));
            stack.setTag(tag);
            if (uuid == null) {
                blockEntity.setBlockSize(this.getBlockSize(stack));
                transition = this.getNewDimensionTransition(this.getBlockSize(stack), level, blockEntity);
            } else {
                transition = this.getDimensionTransition(uuid, level);
            }

            if (transition != null) transition.teleportToDimension(player);
        }

        return InteractionResult.SUCCESS;
    }

    private InteractionResult useOnCircuitBoardBlock(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player.isCrouching()) {
            BlockState state = level.getBlockState(context.getClickedPos());
            level.setBlockAndUpdate(context.getClickedPos(), state.setValue(CircuitBoardBlock.MODE, state.getValue(CircuitBoardBlock.MODE).nextMode()));
        } else {
            ItemStack stack = context.getItemInHand();
            CompoundTag tag = stack.getOrCreateTag();
            MinecraftServer server = level.getServer();
            if (server == null) return InteractionResult.CONSUME;
            if (tag.contains("lastPosDim", CompoundTag.TAG_STRING) && tag.contains("lastPos")) {
                ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, new ResourceLocation(tag.getString("lastPosDim"))));
                new DimensionTransition(serverLevel, NbtUtils.readBlockPos(tag.getCompound("lastPos")).getCenter()).teleportToDimension(player);
                tag.remove("lastPosDim");
                tag.remove("lastPos");
                stack.setTag(tag);
            } else {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                ServerLevel serverLevel = server.getLevel(serverPlayer.getRespawnDimension());
                BlockPos respawn = serverPlayer.getRespawnPosition();
                if (respawn == null) respawn = serverLevel.getSharedSpawnPos();
                new DimensionTransition(serverLevel, respawn.getCenter()).teleportToDimension(player);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult useOnIntegratedCircuitBlock(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof IntegratedCircuitBlockEntity blockEntity)) return InteractionResult.FAIL;
        if (player.isCrouching()) {
            blockEntity.setHidden(!blockEntity.isHidden());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private short getBlockSize(ItemStack stack) {
        short size = stack.getOrCreateTag().getShort("size");
        return size < 4 ? 4 : size;
    }

    private InteractionResultHolder<ItemStack> cycleBlockSize(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        short old = tag.contains("size", CompoundTag.TAG_SHORT) ? tag.getShort("size") : 4;
        short newVal = old == 256 ? 4 : (short) (old * 2);
        tag.putShort("size", newVal);
        stack.setTag(tag);
        player.displayClientMessage(Component.translatable("action.poking_stick.change", newVal), true);
        player.playSound(SoundEvents.CHICKEN_EGG);
        return InteractionResultHolder.success(stack);
    }

    private DimensionTransition getDimensionTransition(UUID uuid, Level level) {
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        ServerLevel circuitBoardLevel = server.getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        if (circuitBoardLevel == null) return null;
        CircuitSavedData data = CircuitSavedData.getCircuitBoardData(circuitBoardLevel);
        return new DimensionTransition(circuitBoardLevel, data.getCircuitStartingPos(uuid).offset(1, 1, 1).getCenter());
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
        return new DimensionTransition(circuitBoardLevel, startingPos.offset(1, 1, 1).getCenter());
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

    private record DimensionTransition(ServerLevel level, Vec3 pos) {
        private void teleportToDimension(Player player) {
            player.teleportTo(this.level, this.pos.x, this.pos.y, this.pos.z, Sets.newHashSet(), 0, 0);
        }
    }
}
