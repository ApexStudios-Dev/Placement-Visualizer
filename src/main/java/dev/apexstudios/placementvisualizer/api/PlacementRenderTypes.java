package dev.apexstudios.placementvisualizer.api;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.apexstudios.placementvisualizer.impl.PlacementVisualizer;
import java.util.function.BiFunction;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;

public interface PlacementRenderTypes {
    // Render type for block ghosting effect
    BiFunction<ResourceLocation, Boolean, RenderType> TRANSLUCENT_NO_DEPTH = Util.memoize((texture, mipmap) -> RenderType.create(
            PlacementVisualizer.id("translucent_no_depth"),
            RenderType.SMALL_BUFFER_SIZE, false, true,
            Pipelines.TRANSLUCENT_NO_DEPTH,
            RenderType.CompositeState
                    .builder()
                    .setTextureState(new RenderStateShard.TextureStateShard(texture, mipmap))
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY) // needed for overlay texture to render
                    .setOutputState(RenderStateShard.OUTLINE_TARGET) // needed to not render behind translucent objects
                    .createCompositeState(true)
    ));

    static RenderType translucentNoDepth(ResourceLocation texture, boolean mipmap) {
        return TRANSLUCENT_NO_DEPTH.apply(texture, mipmap);
    }

    static RenderType translucentNoDepth(ResourceLocation texture) {
        return translucentNoDepth(texture, true);
    }

    static RenderType translucentNoDepth(boolean mipmap) {
        return translucentNoDepth(TextureAtlas.LOCATION_BLOCKS, mipmap);
    }

    static RenderType translucentNoDepth() {
        return translucentNoDepth(true);
    }

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
