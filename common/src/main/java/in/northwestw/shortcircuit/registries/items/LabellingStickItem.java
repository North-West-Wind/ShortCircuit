package in.northwestw.shortcircuit.registries.items;

import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.DataComponents;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blocks.CircuitBoardBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class LabellingStickItem extends Item {
    public LabellingStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        HitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hitresult.getType() == HitResult.Type.MISS) return this.changeMode(player.getItemInHand(hand), player);
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        ItemStack stack = context.getItemInHand();
        boolean copyPasteMode = stack.getOrDefault(DataComponents.BIT.get(), false);
        if (state.is(Blocks.CIRCUIT.get()) || state.is(Blocks.INTEGRATED_CIRCUIT.get()))
            return copyPasteMode ? this.copyOrPasteCircuitColor(context) : this.cycleCircuitColor(context);
        if (state.is(Blocks.CIRCUIT_BOARD.get()))
            return this.toggleAnnotation(context);
        return super.useOn(context);
    }

    private InteractionResult cycleCircuitColor(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) blockEntity.cycleColor(player != null && player.isCrouching());
        else if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) blockEntity.cycleColor(player != null && player.isCrouching());
        return InteractionResult.SUCCESS;
    }

    private InteractionResult copyOrPasteCircuitColor(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player.isCrouching()) {
            // copy color
            DyeColor color = null;
            if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) color = blockEntity.getColor();
            else if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) color = blockEntity.getColor();
            if (color == null) stack.remove(DataComponents.SHORT.get());
            else stack.set(DataComponents.SHORT.get(), (short) color.getId());
            player.displayClientMessage(Component.translatable("action.labelling_stick.copy").withColor(color == null ? 0xFFFFFF : color.getTextColor()), true);
        } else {
            short id = stack.getOrDefault(DataComponents.SHORT.get(), (short) -1);
            DyeColor color = id < 0 ? null : DyeColor.byId(id);
            if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) blockEntity.setColor(color);
            else if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) blockEntity.setColor(color);
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult changeMode(ItemStack stack, Player player) {
        boolean copyPasteMode = stack.getOrDefault(DataComponents.BIT.get(), false);
        stack.set(DataComponents.BIT.get(), !copyPasteMode);
        player.displayClientMessage(Component.translatable("action.labelling_stick.change." + (!copyPasteMode ? "copy" : "cycle")), true);
        player.playSound(SoundEvents.CHICKEN_EGG);
        return InteractionResult.SUCCESS;
    }

    private InteractionResult toggleAnnotation(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        level.setBlockAndUpdate(pos, level.getBlockState(pos).setValue(CircuitBoardBlock.ANNOTATED, !level.getBlockState(pos).getValue(CircuitBoardBlock.ANNOTATED)));
        return InteractionResult.SUCCESS;
    }
}
