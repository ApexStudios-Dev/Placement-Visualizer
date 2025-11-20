package dev.apexstudios.placementvisualizer.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.placementvisualizer.impl.GhostVertexConsumer;
import dev.apexstudios.placementvisualizer.mixin.RenderSetupAccessor;
import dev.apexstudios.placementvisualizer.mixin.RenderTypeAccessor;
import java.util.function.BiConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface GhostRenderUtils {
    static PoseStack toStack(PoseStack.Pose pose) {
        var stack = new PoseStack();
        stack.last().set(pose);
        return stack;
    }

    static void submitGhost(OrderedSubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, BiConsumer<PoseStack, VertexConsumer> action) {
        collector.submitCustomGeometry(poseStack, renderType, (pose, consumer) -> {
            var stack = toStack(pose);
            GhostVertexConsumer.INSTANCE.setDelegate(consumer);
            action.accept(stack, GhostVertexConsumer.INSTANCE);
            GhostVertexConsumer.INSTANCE.reset();
        });
    }

    static <T> void submitModel(OrderedSubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, Model<T> model, T modelState, @Nullable TextureAtlasSprite sprite, int light, int overlay, int tintColor, boolean validPlacement) {
        submitGhost(collector, poseStack, PlacementRenderTypes.translucentNoDepth(extractTexture(sprite, renderType)), (stack, consumer) -> {
            model.setupAnim(modelState);
            model.renderToBuffer(stack, wrap(sprite, consumer), light, overlay, validPlacement ?
                                                                                tintColor :
                                                                                CommonColors.SOFT_RED);
        });
    }

    static void submitModelPart(OrderedSubmitNodeCollector collector, PoseStack poseStack, RenderType renderType, ModelPart modelPart, @Nullable TextureAtlasSprite sprite, int light, int overlay, int tintColor, boolean validPlacement) {
        submitGhost(collector, poseStack, PlacementRenderTypes.translucentNoDepth(extractTexture(sprite, renderType)), (stack, consumer) -> modelPart.render(stack, wrap(sprite, consumer), light, overlay,
                validPlacement ?
                tintColor :
                CommonColors.SOFT_RED));
    }

    static void renderBlockState(PoseStack poseStack, VertexConsumer consumer, BlockAndTintGetter level, BlockPos pos, BlockState blockState, boolean validPlacement) {
        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

        poseStack.pushPose();
        poseStack.translate(pos.getX(), pos.getY(), pos.getZ());

        // Copy of MovingBlockRenderState render logic in BlockFeatureRenderer
        var modelParts = blockRenderDispatcher.getBlockModel(blockState)
                                              .collectParts(level, pos, blockState, RandomSource.create(blockState.getSeed(pos)));

        blockRenderDispatcher.getModelRenderer()
                             .tesselateBlock(level, modelParts, blockState, pos, poseStack, consumer, false,
                                     validPlacement ?
                                     OverlayTexture.NO_OVERLAY :
                                     OverlayTexture.pack(OverlayTexture.RED_OVERLAY_V, OverlayTexture.NO_WHITE_U));

        poseStack.popPose();
    }

    static Identifier extractTexture(@Nullable TextureAtlasSprite sprite, RenderType renderType) {
        if(sprite != null) {
            return sprite.atlasLocation();
        }

        var state = ((RenderTypeAccessor) renderType).PlacementVisualizer$getState();
        var textures = ((RenderSetupAccessor) state).PlacementVisualizer$getTextures();
        var texture = textures.get("Sampler0");

        if(texture != null) {
            return texture.location();
        }

        return TextureAtlas.LOCATION_BLOCKS;
    }

    static VertexConsumer wrap(@Nullable TextureAtlasSprite sprite, VertexConsumer consumer) {
        return sprite == null ? consumer : sprite.wrap(consumer);
    }
}
