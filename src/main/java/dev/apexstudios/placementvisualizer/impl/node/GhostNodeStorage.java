package dev.apexstudios.placementvisualizer.impl.node;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.apexstudios.placementvisualizer.api.GhostRenderUtils;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.jspecify.annotations.Nullable;

public final class GhostNodeStorage extends DelegateNodeCollector {
    public static final GhostNodeStorage INSTANCE = new GhostNodeStorage();

    private boolean validPlacement = true;

    private GhostNodeStorage() {
    }

    public void validPlacement(boolean validPlacement) {
        this.validPlacement = validPlacement;
    }

    @Override
    public void reset() {
        super.reset();
        validPlacement(true);
    }

    @Override
    public <S> void submitModel(Model<? super S> model, S renderState, PoseStack poseStack, RenderType renderType, int packedLight, int packedOverlay, int tintColor, @Nullable TextureAtlasSprite sprite, int outlineColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay) {
        GhostRenderUtils.submitModel(this, poseStack, renderType, model, renderState, sprite, packedLight, packedOverlay, tintColor, validPlacement);
    }

    @Override
    public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int packedLight, int packedOverlay, @Nullable TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil, int tintColor, ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay, int outlineColor) {
        GhostRenderUtils.submitModelPart(this, poseStack, renderType, modelPart, sprite, packedLight, packedOverlay, tintColor, validPlacement);
    }
}
