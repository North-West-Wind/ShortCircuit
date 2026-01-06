package in.northwestw.shortcircuit.registries.blockentityrenderers.renderstates;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Set;

public class CircuitBlockEntityRenderState extends BlockEntityRenderState {
    public DyeColor color = null;
    public boolean hidden = false;
    public short blockSize = 4;
    public Set<Map.Entry<BlockPos, BlockState>> blocks;
}
