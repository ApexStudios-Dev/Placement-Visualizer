package dev.apexstudios.placementvisualizer.api;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.placementvisualizer.impl.PlacementPreviewRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;

public interface PlacementPreviewHandler<TState> {
    // Treat 'level' as immutable, DO NO mutate states, only lookups
    @Nullable
    TState extract(Level level, BlockHitResult hitResult, Player player, InteractionHand hand);

    void submit(PoseStack pose, SubmitNodeCollector collector, LevelRenderState levelState, TState state);

    default Class<? extends RenderLevelStageEvent> stage() {
        return RenderLevelStageEvent.AfterTranslucentBlocks.class;
    }

    static void register(ResourceLocation registryName, PlacementPreviewHandler<?> handler) {
        PlacementPreviewRegistry.register(registryName, handler);
    }

    static void renderBlockState(PoseStack.Pose pose, VertexConsumer consumer, BlockAndTintGetter level, BlockPos pos, BlockState blockState, int overlay) {
        var poseStack = new PoseStack();
        poseStack.mulPose(pose.pose());
        renderBlockState(poseStack, consumer, level, pos, blockState, overlay);
    }

    static void renderBlockState(PoseStack pose, VertexConsumer consumer, BlockAndTintGetter level, BlockPos pos, BlockState blockState, int overlay) {
        var blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();

        pose.pushPose();
        pose.translate(pos.getX(), pos.getY(), pos.getZ());

        // Copy of MovingBlockRenderState render logic in BlockFeatureRenderer
        var modelParts = blockRenderDispatcher.getBlockModel(blockState).collectParts(
                level,
                pos,
                blockState,
                RandomSource.create(blockState.getSeed(pos))
        );

        blockRenderDispatcher.getModelRenderer().tesselateBlock(
                level,
                modelParts,
                blockState,
                pos,
                pose,
                consumer,
                false,
                overlay
        );

        pose.popPose();
    }
}
