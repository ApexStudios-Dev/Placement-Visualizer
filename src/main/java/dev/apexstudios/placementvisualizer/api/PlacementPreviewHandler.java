package dev.apexstudios.placementvisualizer.api;

import dev.apexstudios.placementvisualizer.impl.PlacementPreviewRegistry;
import dev.apexstudios.placementvisualizer.impl.node.GhostNodeStorage;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

public interface PlacementPreviewHandler<TState> {
    // Treat 'level' as immutable, DO NO mutate states, only lookups
    @Nullable TState extract(LevelRenderState levelState, Level level, BlockHitResult hitResult, Player player, InteractionHand hand);

    void submit(RenderLevelStageEvent event, GhostNodeStorage collector, TState state);

    static void register(Identifier registryName, PlacementPreviewHandler<?> handler) {
        PlacementPreviewRegistry.register(registryName, handler);
    }
}
