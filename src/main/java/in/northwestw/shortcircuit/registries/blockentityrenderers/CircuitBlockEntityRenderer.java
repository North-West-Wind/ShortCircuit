package in.northwestw.shortcircuit.registries.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import in.northwestw.shortcircuit.registries.blockentities.CircuitBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import java.util.Map;

public class CircuitBlockEntityRenderer implements BlockEntityRenderer<CircuitBlockEntity> {
    private static final float HIDDEN_SCALE = 0.875f; // 14/16
    private static final float HIDDEN_TRANSLATE = 0.0625f; // 1/16
    private static final float HIDDEN_SCALE_CARPET = 0.9375f; // 30/32
    private static final float HIDDEN_TRANSLATE_CARPET = 0.03125f; // 1/16
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
            case SOUTH -> poseStack.mulPose(new Quaternionf(0, 0.7071068, 0, 0.7071068));
            case EAST -> poseStack.mulPose(new Quaternionf(0, 1, 0, 0));
            case NORTH -> poseStack.mulPose(new Quaternionf(0, 0.7071068, 0, -0.7071068));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.scale(HIDDEN_SCALE_CARPET, HIDDEN_SCALE_CARPET, HIDDEN_SCALE_CARPET);
        poseStack.translate(HIDDEN_TRANSLATE_CARPET, HIDDEN_TRANSLATE_CARPET, HIDDEN_TRANSLATE_CARPET);
        this.blockRenderDispatcher.renderSingleBlock(Blocks.WHITE_CARPET.defaultBlockState(), poseStack, bufferSource, packedLight, packedOverlay, ModelData.EMPTY, null);
        poseStack.translate(0, 0.0625, 0);

        if (blockEntity.isHidden()) {
            //poseStack.scale(HIDDEN_SCALE, HIDDEN_SCALE, HIDDEN_SCALE);
            //poseStack.translate(HIDDEN_TRANSLATE, HIDDEN_TRANSLATE, HIDDEN_TRANSLATE);
            this.blockRenderDispatcher.renderSingleBlock(
                    Blocks.COMPARATOR.defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.EAST).setValue(BlockStateProperties.POWERED, blockEntity.getBlockState().getValue(BlockStateProperties.POWERED)),
                    poseStack, bufferSource, packedLight, packedOverlay, ModelData.EMPTY, null
            );
        } else {
            // intentional repeated scaling so blocks don't clip out
            poseStack.scale(HIDDEN_SCALE, HIDDEN_SCALE, HIDDEN_SCALE);
            poseStack.translate(HIDDEN_TRANSLATE, 0, HIDDEN_TRANSLATE);
            float scale = 1f / (blockEntity.getBlockSize() - 2);
            poseStack.scale(scale, scale, scale);
            for (Map.Entry<BlockPos, BlockState> entry : blockEntity.blocks.entrySet()) {
                BlockPos vec = entry.getKey();
                poseStack.translate(vec.getX(), vec.getY(), vec.getZ());
                this.blockRenderDispatcher.renderSingleBlock(entry.getValue(), poseStack, bufferSource, packedLight, packedOverlay, ModelData.EMPTY, null);
                poseStack.translate(-vec.getX(), -vec.getY(), -vec.getZ());
            }
        }
        poseStack.popPose();
    }
}
