package in.northwestw.shortcircuit.registries.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.Map;

public class CircuitBlockEntityRenderer implements BlockEntityRenderer<CircuitBlockEntity> {
    private static final float HIDDEN_SCALE = 0.875f; // 14/16
    private static final float HIDDEN_TRANSLATE = 0.0625f;
    private final BlockRenderDispatcher blockRenderDispatcher;

    public CircuitBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(CircuitBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        // direction handling. move to the center and rotate, then move back
        poseStack.translate(0.5, 0.5, 0.5);
        switch (blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING)) {
            case EAST -> poseStack.mulPose(new Quaternionf(0, 0.7071068, 0, 0.7071068));
            case SOUTH -> poseStack.mulPose(new Quaternionf(0, 1, 0, 0));
            case WEST -> poseStack.mulPose(new Quaternionf(0, 0.7071068, 0, -0.7071068));
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        if (blockEntity.isHidden()) {
            poseStack.scale(HIDDEN_SCALE, HIDDEN_SCALE, HIDDEN_SCALE);
            poseStack.translate(HIDDEN_TRANSLATE, HIDDEN_TRANSLATE, HIDDEN_TRANSLATE);
            this.blockRenderDispatcher.renderSingleBlock(
                    Blocks.COMPARATOR.defaultBlockState(),
                    poseStack, bufferSource, packedLight, packedOverlay
            );
        } else {
            float scale = 1f / blockEntity.getBlockSize();
            poseStack.scale(scale, scale, scale);
            poseStack.translate(1, 1, 1);
            for (Map.Entry<Vec3, BlockState> entry : blockEntity.blocks.entrySet()) {
                Vec3 vec = entry.getKey();
                poseStack.translate(vec.x, vec.y, vec.z);
                this.blockRenderDispatcher.renderSingleBlock(entry.getValue(), poseStack, bufferSource, packedLight, packedOverlay);
                poseStack.translate(-vec.x, -vec.y, -vec.z);
            }
        }
        poseStack.popPose();
    }
}
