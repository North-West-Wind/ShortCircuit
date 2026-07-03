package in.northwestw.shortcircuit.registries.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import in.northwestw.shortcircuit.ShortCircuitCommon;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Quaternionf;

//? if >=1.21.11 {
import in.northwestw.shortcircuit.registries.blockentityrenderers.renderstates.CircuitBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
//? } else {
/*import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
*///? }

import java.util.Map;
import java.util.Set;

//? if >=1.21.11 {
public class CircuitBlockEntityRenderer implements BlockEntityRenderer<CircuitBlockEntity, CircuitBlockEntityRenderState> {
//? } else
//public class CircuitBlockEntityRenderer implements BlockEntityRenderer<CircuitBlockEntity> {
    private static final float HIDDEN_SCALE = 0.875f; // 14/16
    private static final float HIDDEN_TRANSLATE = 0.0625f; // 1/16

    //? if >=1.21.11 {
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
    //? } else {
    /*private final BlockRenderDispatcher blockRenderDispatcher;

    public CircuitBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }
    *///? }

    @Override
    //? if >=1.21.11 {
    public void submit(CircuitBlockEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState cameraRenderState) {
        BlockState blockState = state.blockState;
        boolean hidden = state.hidden;
        short blockSize = state.blockSize;
        Set<Map.Entry<BlockPos, BlockState>> blocks = state.blocks;
    //? } else {
    /*public void render(CircuitBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState blockState = blockEntity.getBlockState();
        boolean hidden = blockEntity.isHidden();
        short blockSize = blockEntity.getBlockSize();
        Set<Map.Entry<BlockPos, BlockState>> blocks = blockEntity.blocks.entrySet();
    *///? }
        poseStack.pushPose();
        // direction handling. move to the center and rotate, then move back
        poseStack.translate(0.5, 0.5, 0.5);
        switch (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
            case SOUTH -> poseStack.mulPose(new Quaternionf(0, 0.7071068, 0, 0.7071068));
            case EAST -> poseStack.mulPose(new Quaternionf(0, 1, 0, 0));
            case NORTH -> poseStack.mulPose(new Quaternionf(0, 0.7071068, 0, -0.7071068));
        }
        poseStack.translate(-0.5, -0.5, -0.5);
        poseStack.translate(0, 0.0625, 0);

        if (hidden) {
            BlockState renderState = Blocks.COMPARATOR.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST).setValue(BlockStateProperties.POWERED, blockState.getValue(BlockStateProperties.POWERED));
            //? if >=1.21.11 {
            submitNodeCollector.submitBlock(
                    poseStack, renderState,
                    state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            //? } else {
            /*this.blockRenderDispatcher.renderSingleBlock(
                    renderState, poseStack,
                    bufferSource, packedLight, packedOverlay
            );
            *///? }
        } else {
            // intentional repeated scaling so blocks don't clip out
            poseStack.scale(HIDDEN_SCALE, HIDDEN_SCALE, HIDDEN_SCALE);
            poseStack.translate(HIDDEN_TRANSLATE, 0, HIDDEN_TRANSLATE);
            float scale = 1f / (blockSize - 2);
            poseStack.scale(scale, scale, scale);
            for (Map.Entry<BlockPos, BlockState> entry : blocks) {
                BlockPos vec = entry.getKey();
                poseStack.translate(vec.getX(), vec.getY(), vec.getZ());
                //? if >=1.21.11 {
                submitNodeCollector.submitBlock(poseStack, entry.getValue(), state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                //? } else
                //this.blockRenderDispatcher.renderSingleBlock(entry.getValue(), poseStack, bufferSource, packedLight, packedOverlay);
                poseStack.translate(-vec.getX(), -vec.getY(), -vec.getZ());
            }
        }
        poseStack.popPose();
    }
}
