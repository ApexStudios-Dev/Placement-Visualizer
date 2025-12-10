package dev.apexstudios.placementvisualizer.impl;

import com.google.common.collect.Maps;
import dev.apexstudios.placementvisualizer.api.PlacementPreviewHandler;
import dev.apexstudios.placementvisualizer.impl.node.GhostNodeStorage;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public final class PlacementPreviewRegistry {
    private static final Map<Identifier, PlacementPreviewHandler<?>> REGISTRY = Maps.newConcurrentMap();
    private static final ContextKey<State<?>> KEY = new ContextKey<>(PlacementVisualizer.identifier("render_state"));
    public static final Identifier DEBUG_KEY = PlacementVisualizer.identifier("force_render");

    public static void register(Identifier registryName, PlacementPreviewHandler<?> handler) {
        if(REGISTRY.putIfAbsent(registryName, handler) != null) {
            throw new IllegalStateException("Duplicate PlacementPreviewHandler registration: " + registryName);
        }
    }

    static void extract(ExtractLevelRenderStateEvent event) {
        var client = Minecraft.getInstance();

        if(client.player == null || !(client.hitResult instanceof BlockHitResult hitResult)) {
            return;
        }

        var forceRender = Minecraft.getInstance().debugEntries.isCurrentlyEnabled(DEBUG_KEY);

        if(hitResult.getType() == HitResult.Type.MISS && !forceRender) {
            return;
        }

        var level = event.getLevel();
        var levelState = event.getRenderState();

        if(!extract(level, levelState, hitResult, client.player, InteractionHand.MAIN_HAND)) {
            extract(level, levelState, hitResult, client.player, InteractionHand.OFF_HAND);
        }
    }

    static void submit(RenderLevelStageEvent event) {
        var state = event.getLevelRenderState().getRenderData(KEY);

        if(state != null) {
            // TODO: Neo should maybe pass this via event
            GhostNodeStorage.INSTANCE.setDelegate(Minecraft.getInstance().gameRenderer.getSubmitNodeStorage());
            state.submit(event);
            GhostNodeStorage.INSTANCE.reset();
        }
    }

    private static boolean extract(ClientLevel level, LevelRenderState levelState, BlockHitResult hitResult, LocalPlayer player, InteractionHand hand) {
        for(var handler : REGISTRY.values()) {
            if(extract(handler, level, levelState, hitResult, player, hand)) {
                return true;
            }
        }

        return false;
    }

    private static <TState> boolean extract(PlacementPreviewHandler<TState> handler, ClientLevel level, LevelRenderState levelState, BlockHitResult hitResult, LocalPlayer player, InteractionHand hand) {
        var state = handler.extract(levelState, level, hitResult, player, hand);

        if(state == null) {
            return false;
        }

        levelState.setRenderData(KEY, new State<>(handler, state));
        return true;
    }

    private record State<TState>(PlacementPreviewHandler<TState> handler, TState state) {
        public void submit(RenderLevelStageEvent event) {
            handler.submit(event, GhostNodeStorage.INSTANCE, state);
        }
    }
}
