package dev.apexstudios.placementvisualizer.api;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import dev.apexstudios.placementvisualizer.impl.PlacementVisualizer;
import java.util.function.BiFunction;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

public interface PlacementRenderTypes {
    // Render type for block ghosting effect
    BiFunction<Identifier, Boolean, RenderType> TRANSLUCENT_NO_DEPTH = Util.memoize((texture, outline) -> RenderType.create("translucent_no_depth", RenderSetup
            .builder(Pipelines.TRANSLUCENT_NO_DEPTH)
            .withTexture("Sampler0", texture)
            .useLightmap()
            .useOverlay()
            .setOutputTarget(OutputTarget.OUTLINE_TARGET)
            .createRenderSetup()
    ));

    static RenderType translucentNoDepth(Identifier texture, boolean outline) {
        return TRANSLUCENT_NO_DEPTH.apply(texture, outline);
    }

    static RenderType translucentNoDepth(Identifier texture) {
        return translucentNoDepth(texture, true);
    }

    static RenderType translucentNoDepth(boolean outline) {
        return translucentNoDepth(TextureAtlas.LOCATION_BLOCKS, outline);
    }

    static RenderType translucentNoDepth() {
        return translucentNoDepth(true);
    }

    interface Pipelines {
        RenderPipeline TRANSLUCENT_NO_DEPTH = RenderPipelines.TRANSLUCENT_MOVING_BLOCK
                .toBuilder()
                .withLocation(PlacementVisualizer.identifier("pipeline/translucent_no_depth"))
                .withDepthTestFunction(DepthTestFunction.LEQUAL_DEPTH_TEST)
                // needed in order for overlay texture to render
                .withVertexShader("core/entity")
                .withFragmentShader("core/entity")
                .withSampler("Sampler1")
                .withVertexFormat(DefaultVertexFormat.NEW_ENTITY, VertexFormat.Mode.QUADS)
                .withShaderDefine("EMISSIVE")
                .build();
    }
}
