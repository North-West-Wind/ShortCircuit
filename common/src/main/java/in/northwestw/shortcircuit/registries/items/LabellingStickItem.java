package in.northwestw.shortcircuit.registries.items;

import in.northwestw.shortcircuit.registries.Blocks;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class LabellingStickItem extends Item {
    public LabellingStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.CIRCUIT.get()) || state.is(Blocks.INTEGRATED_CIRCUIT.get())) return this.useOnAnyCircuitBlock(context);
        return super.useOn(context);
    }

    public InteractionResult useOnAnyCircuitBlock(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        if (level.getBlockEntity(pos) instanceof CircuitBlockEntity blockEntity) blockEntity.cycleColor(player != null && player.isCrouching());
        else if (level.getBlockEntity(pos) instanceof IntegratedCircuitBlockEntity blockEntity) blockEntity.cycleColor(player != null && player.isCrouching());
        return InteractionResult.SUCCESS;
    }
}
