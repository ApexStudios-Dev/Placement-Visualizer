package dev.apexstudios.placementvisualizer.api;

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
    BiFunction<Identifier, Boolean, RenderType> TRANSLUCENT_NO_DEPTH = Util.memoize((texture, outline) -> RenderType.create(PlacementVisualizer.id("translucent_no_depth"), RenderSetup
            .builder(RenderPipelines.ENTITY_TRANSLUCENT_EMISSIVE)
            .withTexture("Sampler0", texture)
            .useLightmap()
            .useOverlay() // needed for overlay texture to render
            .affectsCrumbling()
            .sortOnUpload()
            .setOutline(outline ? RenderSetup.OutlineProperty.AFFECTS_OUTLINE : RenderSetup.OutlineProperty.NONE)
            .setOutputTarget(OutputTarget.OUTLINE_TARGET) // needed to not render behind translucent objects
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
}
