package in.northwestw.shortcircuit.registries.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import in.northwestw.shortcircuit.registries.blockentityrenderers.renderstates.CircuitBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class CircuitBlockEntityRenderer implements BlockEntityRenderer<CircuitBlockEntity, CircuitBlockEntityRenderState> {
    private static final float HIDDEN_SCALE = 0.875f; // 14/16
    private static final float HIDDEN_TRANSLATE = 0.0625f; // 1/16
    private static final float HIDDEN_SCALE_CARPET = 0.9375f; // 30/32
    private static final float HIDDEN_TRANSLATE_CARPET = 0.03125f; // 1/16

    public CircuitBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public CircuitBlockEntityRenderState createRenderState() {
        return new CircuitBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(CircuitBlockEntity blockEntity, CircuitBlockEntityRenderState renderState, float partialTick, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderState.extractBase(blockEntity, renderState, breakProgress);
        renderState.hidden = blockEntity.isHidden();
        renderState.blockSize = blockEntity.getBlockSize();
        renderState.blocks = blockEntity.blocks.entrySet();
    }

    @Override
    public void submit(CircuitBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        poseStack.pushPose();
        // direction handling. move to the center and rotate, then move back
        poseStack.translate(0.5, 0.5, 0.5);
        switch (state.blockState.getValue(HorizontalDirectionalBlock.FACING)) {
            case SOUTH -> poseStack.mulPose(new Quaternionf(0, 0.7071068, 0, 0.7071068));
            case EAST -> poseStack.mulPose(new Quaternionf(0, 1, 0, 0));
            case NORTH -> poseStack.mulPose(new Quaternionf(0, 0.7071068, 0, -0.7071068));
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(0, 0.0625, 0);

        if (state.hidden) {
            //poseStack.scale(HIDDEN_SCALE, HIDDEN_SCALE, HIDDEN_SCALE);
            //poseStack.translate(HIDDEN_TRANSLATE, HIDDEN_TRANSLATE, HIDDEN_TRANSLATE);
            submitNodeCollector.submitBlock(
                    poseStack,
                    Blocks.COMPARATOR.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST).setValue(BlockStateProperties.POWERED, state.blockState.getValue(BlockStateProperties.POWERED)),
                    state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        } else {
            // intentional repeated scaling so blocks don't clip out
            poseStack.scale(HIDDEN_SCALE, HIDDEN_SCALE, HIDDEN_SCALE);
            poseStack.translate(HIDDEN_TRANSLATE, 0, HIDDEN_TRANSLATE);
            float scale = 1f / (state.blockSize - 2);
            poseStack.scale(scale, scale, scale);
            for (Map.Entry<BlockPos, BlockState> entry : state.blocks) {
                BlockPos vec = entry.getKey();
                poseStack.translate(vec.getX(), vec.getY(), vec.getZ());
                submitNodeCollector.submitBlock(poseStack, entry.getValue(), state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.translate(-vec.getX(), -vec.getY(), -vec.getZ());
            }
        }
        poseStack.popPose();
    }
}
