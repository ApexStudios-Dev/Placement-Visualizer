package dev.apexstudios.placementvisualizer.impl;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

public final class GhostVertexConsumer implements VertexConsumer {
    public static final GhostVertexConsumer INSTANCE = new GhostVertexConsumer();
    public static final int GHOST_ALPHA = 190;

    @Nullable
    private VertexConsumer delegate;

    private GhostVertexConsumer() {
    }

    public void setDelegate(@Nullable VertexConsumer delegate) {
        this.delegate = delegate;
    }

    public void reset() {
        delegate = null;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        if(delegate != null) {
            delegate.addVertex(x, y, z);
        }

        return this;
    }

    @Override
    public VertexConsumer setColor(int r, int g, int b, int a) {
        if(delegate != null) {
            delegate.setColor(r, g, b, (a * GHOST_ALPHA) / 0xFF);
        }

        return this;
    }

    @Override
    public VertexConsumer setColor(int i) {
        return setColor(ARGB.red(i), ARGB.green(i), ARGB.blue(i), ARGB.alpha(i));
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        if(delegate != null) {
            delegate.setUv(u, v);
        }

        return this;
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        if(delegate != null) {
            delegate.setUv1(u, v);
        }

        return this;
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        if(delegate != null) {
            delegate.setUv2(u, v);
        }

        return this;
    }

    @Override
    public VertexConsumer setNormal(float normalX, float normalY, float normalZ) {
        if(delegate != null) {
            delegate.setNormal(normalX, normalY, normalZ);
        }

        return this;
    }

    @Override
    public VertexConsumer setLineWidth(float v) {
        if(delegate != null) {
            delegate.setLineWidth(v);
        }

        return this;
    }
}
