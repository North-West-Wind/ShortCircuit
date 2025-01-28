package in.northwestw.shortcircuit.registries.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import in.northwestw.shortcircuit.properties.ColorHelper;
import in.northwestw.shortcircuit.registries.blockentities.IntegratedCircuitBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class IntegratedCircuitBlockEntityRenderer implements BlockEntityRenderer<IntegratedCircuitBlockEntity> {
    private static final float HIDDEN_SCALE = 0.875f; // 14/16
    private static final float HIDDEN_TRANSLATE = 0.0625f; // 1/16
    private static final float HIDDEN_SCALE_CARPET = 0.9375f; // 30/32
    private static final float HIDDEN_TRANSLATE_CARPET = 0.03125f; // 1/16
    private final BlockRenderDispatcher blockRenderDispatcher;

    public IntegratedCircuitBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.blockRenderDispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(IntegratedCircuitBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        DyeColor color = blockEntity.getColor();
        if (color != null) {
            this.blockRenderDispatcher.renderSingleBlock(ColorHelper.colorToStainedGlass(color).defaultBlockState(), poseStack, bufferSource, packedLight, packedOverlay);
        }
        // direction handling. move to the center and rotate, then move back
        poseStack.translate(0.5, 0.5, 0.5);
        switch (blockEntity.getBlockState().getValue(HorizontalDirectionalBlock.FACING)) {
            case SOUTH -> poseStack.mulPose(new Quaternion(0, 0.7071068f, 0, 0.7071068f));
            case EAST -> poseStack.mulPose(new Quaternion(0, 1, 0, 0));
            case NORTH -> poseStack.mulPose(new Quaternion(0, 0.7071068f, 0, -0.7071068f));
        }
        poseStack.translate(-0.5, -0.5, -0.5);

        poseStack.scale(HIDDEN_SCALE_CARPET, HIDDEN_SCALE_CARPET, HIDDEN_SCALE_CARPET);
        poseStack.translate(HIDDEN_TRANSLATE_CARPET, HIDDEN_TRANSLATE_CARPET, HIDDEN_TRANSLATE_CARPET);
        this.blockRenderDispatcher.renderSingleBlock(Blocks.YELLOW_CARPET.defaultBlockState(), poseStack, bufferSource, packedLight, packedOverlay);
        poseStack.translate(0, 0.0625, 0);

        if (blockEntity.isHidden()) {
            this.blockRenderDispatcher.renderSingleBlock(
                    in.northwestw.shortcircuit.registries.Blocks.INNER_IC.get().defaultBlockState().setValue(BlockStateProperties.POWERED, blockEntity.getBlockState().getValue(BlockStateProperties.POWERED)),
                    poseStack, bufferSource, packedLight, packedOverlay
            );
        } else {
            // intentional repeated scaling so blocks don't clip out
            poseStack.scale(HIDDEN_SCALE, HIDDEN_SCALE, HIDDEN_SCALE);
            poseStack.translate(HIDDEN_TRANSLATE, 0, HIDDEN_TRANSLATE);
            float scale = 1f / 2;
            poseStack.scale(scale, scale, scale);
            int count = 0;
            for (BlockState state : blockEntity.blocks) {
                BlockPos vec = new BlockPos(count % 2, (count / 2) % 2, (count / 4) % 2);
                poseStack.translate(vec.getX(), vec.getY(), vec.getZ());
                this.blockRenderDispatcher.renderSingleBlock(state, poseStack, bufferSource, packedLight, packedOverlay);
                poseStack.translate(-vec.getX(), -vec.getY(), -vec.getZ());
                count++;
            }
        }

        poseStack.popPose();
    }
}
