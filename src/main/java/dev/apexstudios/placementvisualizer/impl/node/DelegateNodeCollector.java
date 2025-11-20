package dev.apexstudios.placementvisualizer.impl.node;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.MovingBlockRenderState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

public class DelegateNodeCollector implements SubmitNodeCollector {
    @Nullable
    private SubmitNodeCollector delegate;

    public final void setDelegate(@Nullable SubmitNodeCollector delegate) {
        this.delegate = delegate;
    }

    @OverridingMethodsMustInvokeSuper
    public void reset() {
        delegate = null;
    }

    @Override
    public OrderedSubmitNodeCollector order(int index) {
        return delegate == null ? this : delegate;
    }

    @Override
    public void submitShadow(PoseStack poseStack, float radius, List<EntityRenderState.ShadowPiece> pieces) {
        if(delegate != null) {
            delegate.submitShadow(poseStack, radius, pieces);
        }
    }

    @Override
    public void submitNameTag(PoseStack poseStack, @Nullable Vec3 pos, int yOffset, Component text, boolean seethrough, int packedLight, double distanceToCameraSq, CameraRenderState cameraRenderState) {
        if(delegate != null) {
            delegate.submitNameTag(poseStack, pos, yOffset, text, seethrough, packedLight, distanceToCameraSq, cameraRenderState);
        }
    }

    @Override
    public void submitText(PoseStack poseStack, float x, float y, FormattedCharSequence string, boolean dropShadow, Font.DisplayMode displayMode, int packedLight, int color, int backgroundColor, int outlineColor) {
        if(delegate != null) {
            delegate.submitText(poseStack, x, y, string, dropShadow, displayMode, packedLight, color, backgroundColor, outlineColor);
        }
    }

    @Override
    public void submitFlame(PoseStack poseStack, EntityRenderState renderState, Quaternionf rotation) {
        if(delegate != null) {
            delegate.submitFlame(poseStack, renderState, rotation);
        }
    }

    @Override
    public void submitLeash(PoseStack poseStack, EntityRenderState.LeashState leashState) {
        if(delegate != null) {
            delegate.submitLeash(poseStack, leashState);
        }
    }

    @Override
    public <S> void submitModel(Model<? super S> model, S renderState, PoseStack poseStack, RenderType renderType, int packedLight, int packedOverlay, int tintColor, @Nullable TextureAtlasSprite sprite, int outlineColor, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        if(delegate != null) {
            delegate.submitModel(model, renderState, poseStack, renderType, packedLight, packedOverlay, tintColor, sprite, outlineColor, crumblingOverlay);
        }
    }

    @Override
    public void submitModelPart(ModelPart modelPart, PoseStack poseStack, RenderType renderType, int packedLight, int packedOverlay, @Nullable TextureAtlasSprite sprite, boolean sheeted, boolean hasFoil, int tintColor, @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay, int outlineColor) {
        if(delegate != null) {
            delegate.submitModelPart(modelPart, poseStack, renderType, packedLight, packedOverlay, sprite);
        }
    }

    @Override
    public void submitBlock(PoseStack poseStack, BlockState blockState, int packedLight, int packedOverlay, int outlineColor) {
        if(delegate != null) {
            delegate.submitBlock(poseStack, blockState, packedLight, packedOverlay, outlineColor);
        }
    }

    @Override
    public void submitMovingBlock(PoseStack poseStack, MovingBlockRenderState renderState) {
        if(delegate != null) {
            delegate.submitMovingBlock(poseStack, renderState);
        }
    }

    @Override
    public void submitBlockModel(PoseStack poseStack, RenderType renderType, BlockStateModel model, float r, float g, float b, int packedLight, int packedOverlay, int outlineColor) {
        if(delegate != null) {
            delegate.submitBlockModel(poseStack, renderType, model, r, g, b, packedLight, packedOverlay, outlineColor);
        }
    }

    @Override
    public void submitItem(PoseStack poseStack, ItemDisplayContext displayContext, int packedLight, int packedOverlay, int outlineColor, int[] tintLayers, List<BakedQuad> quads, RenderType renderType, ItemStackRenderState.FoilType foilType) {
        if(delegate != null) {
            delegate.submitItem(poseStack, displayContext, packedLight, packedOverlay, outlineColor, tintLayers, quads, renderType, foilType);
        }
    }

    @Override
    public void submitCustomGeometry(PoseStack poseStack, RenderType renderType, SubmitNodeCollector.CustomGeometryRenderer renderer) {
        if(delegate != null) {
            delegate.submitCustomGeometry(poseStack, renderType, renderer);
        }
    }

    @Override
    public void submitParticleGroup(SubmitNodeCollector.ParticleGroupRenderer renderer) {
        if(delegate != null) {
            delegate.submitParticleGroup(renderer);
        }
    }
}
