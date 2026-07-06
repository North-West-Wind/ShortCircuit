package in.northwestw.shortcircuit.registries.items;

import in.northwestw.shortcircuit.Constants;
import in.northwestw.shortcircuit.config.Config;
import in.northwestw.shortcircuit.data.CircuitSavedData;
import in.northwestw.shortcircuit.data.Octolet;
import in.northwestw.shortcircuit.properties.RelativeDirection;
import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import in.northwestw.shortcircuit.registries.datacomponents.LastPosDataComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

//? if >=1.21.11 {
import net.minecraft.world.level.storage.LevelData;
//? } elif <=1.21.1 {
/*import net.minecraft.world.InteractionResultHolder;
*///? }

//? if >=1.21.1 {
import net.minecraft.world.level.portal.TeleportTransition;
//? } else {
/*import com.google.common.collect.Sets;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
*///? }

//? if >=1.20.1 {
import net.minecraft.core.registries.Registries;
//? }

import java.util.Collection;
import java.util.UUID;

public class PokingStickItem extends Item {
    public PokingStickItem(Properties properties) {
        super(properties);
        //? if >=1.21.1 {
        properties.component(DataComponents.SHORT.get(), (short) 4);
        //? }
    }

    @Override
    //~ if <=1.21.1 'InteractionResult' -> 'InteractionResultHolder<ItemStack>'
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
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

        //? if >=1.21.4 {
        return this.cycleBlockSize(stack, player);
        //? } else
        //return this.cycleBlockSize(stack, player).getResult();
    }

    private InteractionResult useOnCircuitBlock(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof CircuitBlockEntity blockEntity)) return InteractionResult.FAIL;
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player.isCrouching() || player.isShiftKeyDown()) {
            blockEntity.setHidden(!blockEntity.isHidden());
        } else {
            TeleportTransition transition;
            UUID uuid = blockEntity.getUuid();
            //? if >=1.21.11 {
            stack.set(DataComponents.LAST_POS.get(), new LastPosDataComponent(level.dimension().identifier(), player.position()));
            //? } elif >=1.21.1 {
            /*stack.set(DataComponents.LAST_POS.get(), new LastPosDataComponent(level.dimension().location(), player.position()));
            *///? } else {
            /*CompoundTag tag = stack.getOrCreateTag();
            tag.putString("lastPosDim", level.dimension().location().toString());
            tag.put("lastPos", NbtUtils.writeBlockPos(player.blockPosition()));
            stack.setTag(tag);
            *///? }
            if (uuid == null) {
                blockEntity.setBlockSize(this.getBlockSize(stack));
                transition = this.getNewTeleportTransition(this.getBlockSize(stack), level, blockEntity);
            } else {
                transition = this.getTeleportTransition(uuid, level);
            }
            if (transition != null)
                //? if >=1.21.4 {
                player.teleport(transition);
                //? } elif >=1.21.1 {
                /*player.changeDimension(transition);
                *///? } else
                //transition.teleportToDimension(player);
        }

        return InteractionResult.SUCCESS;
    }

    private InteractionResult useOnCircuitBoardBlock(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player.isCrouching() || player.isShiftKeyDown()) {
            BlockState state = level.getBlockState(context.getClickedPos());
            level.setBlockAndUpdate(context.getClickedPos(), state.setValue(CircuitBoardBlock.MODE, state.getValue(CircuitBoardBlock.MODE).nextMode()));
        } else {
            ItemStack stack = context.getItemInHand();
            MinecraftServer server = level.getServer();
            if (server == null) return InteractionResult.CONSUME;
            //? if >=1.21.1 {
            if (stack.has(DataComponents.LAST_POS.get())) {
                LastPosDataComponent component = stack.get(DataComponents.LAST_POS.get());
                ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, component.rl()));
                TeleportTransition transition = new TeleportTransition(serverLevel, component.pos(), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING);
            //? } else {
            /*CompoundTag tag = stack.getOrCreateTag();
            if (tag.contains("lastPosDim", CompoundTag.TAG_STRING) && tag.contains("lastPos")) {
                ServerLevel serverLevel = server.getLevel(ResourceKey.create(Registries.DIMENSION, new Identifier(tag.getString("lastPosDim"))));
                new TeleportTransition(serverLevel, NbtUtils.readBlockPos(tag.getCompound("lastPos"))).teleportToDimension(player);
                tag.remove("lastPosDim");
                tag.remove("lastPos");
                stack.setTag(tag);
            *///? }
                //? if >=1.21.4 {
                player.teleport(transition);
                //? } elif >=1.21.1 {
                /*player.changeDimension(transition);
                stack.remove(DataComponents.LAST_POS.get());
                *///? }
            } else {
                ServerPlayer serverPlayer = (ServerPlayer) player;
                //? if >=1.21.11 {
                ServerPlayer.RespawnConfig config = serverPlayer.getRespawnConfig();
                ServerLevel serverLevel = server.getLevel(serverPlayer.getRespawnConfig().respawnData().dimension());
                LevelData.RespawnData data = config == null ? serverLevel.getRespawnData() : config.respawnData();
                player.teleport(new TeleportTransition(serverLevel, data.pos().getCenter(), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
                //? } elif >=1.21.4 {
                /*ServerLevel serverLevel = server.getLevel(serverPlayer.getRespawnDimension());
                BlockPos respawn = serverPlayer.getRespawnPosition();
                if (respawn == null) respawn = serverLevel.getSharedSpawnPos();
                player.teleport(new TeleportTransition(serverLevel, respawn.getCenter(), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
                *///? } elif >=1.21.1 {
                /*ServerLevel serverLevel = server.getLevel(serverPlayer.getRespawnDimension());
                BlockPos respawn = serverPlayer.getRespawnPosition();
                if (respawn == null) respawn = serverLevel.getSharedSpawnPos();
                player.changeDimension(new TeleportTransition(serverLevel, respawn.getCenter(), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING));
                *///? } else {
                /*ServerLevel serverLevel = server.getLevel(serverPlayer.getRespawnDimension());
                BlockPos respawn = serverPlayer.getRespawnPosition();
                if (respawn == null) respawn = serverLevel.getSharedSpawnPos();
                new TeleportTransition(serverLevel, respawn).teleportToDimension(player);
                *///? }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult useOnIntegratedCircuitBlock(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof IntegratedCircuitBlockEntity blockEntity)) return InteractionResult.FAIL;
        if (player.isCrouching() || player.isShiftKeyDown()) {
            blockEntity.setHidden(!blockEntity.isHidden());
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.FAIL;
    }

    private short getBlockSize(ItemStack stack) {
        //? if >=1.21.1 {
        return stack.getOrDefault(DataComponents.SHORT.get(), (short) 4);
        //? } else {
        /*short size = stack.getOrCreateTag().getShort("size");
        return size < 4 ? 4 : size;
        *///? }
    }

    //~ if <=1.21.1 'InteractionResult' -> 'InteractionResultHolder<ItemStack>'
    private InteractionResult cycleBlockSize(ItemStack stack, Player player) {
        //? if >=1.21.1 {
        short old = stack.getOrDefault(DataComponents.SHORT.get(), (short) 4);
        short newVal = old >= Config.MAX_CIRCUIT_SIZE ? 4 : (short) (old * 2);
        stack.set(DataComponents.SHORT.get(), newVal);
        //? } else {
        /*CompoundTag tag = stack.getOrCreateTag();
        short old = tag.contains("size", CompoundTag.TAG_SHORT) ? tag.getShort("size") : 4;
        short newVal = old == 256 ? 4 : (short) (old * 2);
        tag.putShort("size", newVal);
        stack.setTag(tag);
        *///? }
        player.displayClientMessage(Component.translatable("action.poking_stick.change", newVal), true);
        player.playSound(SoundEvents.CHICKEN_EGG);
        //? if >=1.21.4 {
        return InteractionResult.SUCCESS;
        //? } else
        //return InteractionResultHolder.success(stack);
    }

    //? if >=1.21.4 {
    private TeleportTransition getTeleportTransition(UUID uuid, Level level) {
    //? } else
    //private TeleportTransition getTeleportTransition(UUID uuid, Level level) {
        MinecraftServer server = level.getServer();
        if (server == null) return null;
        ServerLevel circuitBoardLevel = server.getLevel(Constants.CIRCUIT_BOARD_DIMENSION);
        if (circuitBoardLevel == null) return null;
        CircuitSavedData data = CircuitSavedData.getCircuitBoardData(circuitBoardLevel);
        //? if >=1.21.1 {
        return new TeleportTransition(circuitBoardLevel, data.getCircuitStartingPos(uuid).offset(1, 1, 1).getCenter(), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING);
        //? } else
        //return new TeleportTransition(circuitBoardLevel, data.getCircuitStartingPos(uuid).offset(1, 1, 1));
    }

    private TeleportTransition getNewTeleportTransition(short blockSize, Level level, CircuitBlockEntity blockEntity) {
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
        //? if >=1.21.1 {
        return new TeleportTransition(circuitBoardLevel, startingPos.offset(1, 1, 1).getCenter(), Vec3.ZERO, 0, 0, TeleportTransition.DO_NOTHING);
        //? } else
        //return new TeleportTransition(circuitBoardLevel, startingPos.offset(1, 1, 1));
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

    //? if <=1.20.1 {
    /*private record TeleportTransition(ServerLevel level, BlockPos pos) {
        private void teleportToDimension(Player player) {
            // minecraft itself has broken effects on changing dimension. this is a workaround
            Collection<MobEffectInstance> effects = player.getActiveEffects();
            Vec3 center = new Vec3(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5);
            //? if >=1.20.1 {
            player.teleportTo(this.level, center.x, center.y, center.z, Sets.newHashSet(), 0, 0);
            //? } else {
            /^player.changeDimension(this.level);
            player.teleportTo(center.x, center.y, center.z);
            ^///? }
            Entity entity = this.level.getEntity(player.getUUID());
            if (entity instanceof Player newPlayer)
                effects.forEach(effect -> newPlayer.forceAddEffect(effect, null));
        }
    }
    *///? }
}
