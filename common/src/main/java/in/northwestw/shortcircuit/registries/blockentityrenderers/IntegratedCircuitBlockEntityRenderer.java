package in.northwestw.shortcircuit.registries.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import in.northwestw.shortcircuit.properties.ColorHelper;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.DyeColor;

public class IntegratedCircuitBlockEntityRenderer implements BlockEntityRenderer<IntegratedCircuitBlockEntity> {
    private final BlockRenderDispatcher blockRenderDispatcher;

    public IntegratedCircuitBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(IntegratedCircuitBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        DyeColor color = blockEntity.getColor();
        if (color != null) {
            this.blockRenderDispatcher.renderSingleBlock(ColorHelper.colorToStainedGlass(color).defaultBlockState(), poseStack, bufferSource, packedLight, packedOverlay);
        }
    }
}
