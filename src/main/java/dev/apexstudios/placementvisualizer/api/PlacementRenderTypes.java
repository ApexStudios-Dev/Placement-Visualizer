package dev.apexstudios.placementvisualizer.api;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.apexstudios.placementvisualizer.impl.PlacementVisualizer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

public interface PlacementRenderTypes {
    // Render type for block ghosting effect
    RenderType TRANSLUCENT_NO_DEPTH = RenderType.create(
            PlacementVisualizer.id("translucent_no_depth"),
            RenderType.SMALL_BUFFER_SIZE, false, true,
            Pipelines.TRANSLUCENT_NO_DEPTH,
            RenderType.CompositeState
                    .builder()
                    .setTextureState(RenderType.BLOCK_SHEET_MIPPED)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY) // needed for overlay texture to render
                    .setOutputState(RenderStateShard.OUTLINE_TARGET) // needed to not render behind translucent objects
                    .createCompositeState(true)
    );

    interface Pipelines {
        RenderPipeline TRANSLUCENT_NO_DEPTH = RenderPipelines.TRANSLUCENT
                .toBuilder()
                .withLocation(PlacementVisualizer.identifier("pipeline/translucent_no_depth"))
                .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                // needed in order for overlay texture to render
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withSampler("Sampler1")
                .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
                .build();
    }
}
