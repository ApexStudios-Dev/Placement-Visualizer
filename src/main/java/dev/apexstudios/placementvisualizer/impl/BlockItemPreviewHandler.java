package dev.apexstudios.placementvisualizer.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.placementvisualizer.api.AlphaVertexConsumer;
import dev.apexstudios.placementvisualizer.api.BlockItemPlacementEvent;
import dev.apexstudios.placementvisualizer.api.PlacementPreviewHandler;
import dev.apexstudios.placementvisualizer.api.PlacementRenderTypes;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

final class BlockItemPreviewHandler implements PlacementPreviewHandler<BlockItemPreviewHandler.State> {
    @Override
    @Nullable
    public State extract(Level level, BlockHitResult hitResult, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if(stack.isEmpty()) {
            return null;
        }

        var item = ItemResource.of(stack);

        var enabledFeatures = level.enabledFeatures();
        // placement should fail if item is disabled
        var canPlace = hitResult.getType() != HitResult.Type.MISS && stack.isItemEnabled(enabledFeatures);
        // 1) determine block for placement
        var block = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.GetBlock(item)).block();

        // got empty block, move onto next arm
        if(block == Blocks.AIR) {
            return null;
        }

        // block does not have required tag, move onto next arm
        if(!block.builtInRegistryHolder().is(BlockItemPlacementEvent.RENDERABLES)) {
            return null;
        }

        // placement should fail if block is disabled
        if(canPlace && !block.isEnabled(enabledFeatures)) {
            canPlace = false;
        }

        // 2) calculate placement context
        var eventUPC = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.UpdatePlacementContext(level, player, hand, stack, hitResult));
        var placeContext = eventUPC.placeContext();

        // placement failed if invalid context or event was cancelled
        // similar result to returning null in 'BlockItem.updatePlacementContext'
        if(canPlace && (!placeContext.canPlace() || eventUPC.isCanceled())) {
            canPlace = false;
        }

        // 3) determine default block state
        var defaultBlockState = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.GetDefaultBlockState(placeContext, block)).blockState();

        // 4) determine placement block state
        var eventGPBS = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.GetPlacementBlockState(placeContext, defaultBlockState));
        var placementBlockState = eventGPBS.blockState();

        // canceling this event means block placement failed
        // similar result to returning null in 'Block.getStateForPlacement'
        if(canPlace && eventGPBS.isCanceled()) {
            canPlace = false;
        }

        // 5) strip invalid block states
        // by default this strips out waterlogged
        placementBlockState = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.StripInvalidProperties(defaultBlockState, placementBlockState)).placementBlockState();

        // 6) collect additional block states
        var blockStates = new Long2ObjectOpenHashMap<BlockState>();
        NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.CollectAdditionalBlockStates(placeContext, placementBlockState, blockStates));
        blockStates.put(placeContext.getClickedPos().asLong(), placementBlockState); // ensure origin point can not be overwritten

        // 7) return finalized render state
        return new State(
                level,
                canPlace,
                Long2ObjectMaps.unmodifiable(blockStates)
        );
    }

    @Override
    public void submit(PoseStack pose, SubmitNodeCollector collector, LevelRenderState levelState, State state) {
        pose.pushPose();
        pose.translate(levelState.cameraRenderState.pos.scale(-1D));

        collector.submitCustomGeometry(pose, PlacementRenderTypes.TRANSLUCENT_NO_DEPTH, state::render);
        pose.popPose();
    }

    record State(LevelAccessor level, boolean canPlace, Long2ObjectMap<BlockState> blockStates) {
        public void render(PoseStack.Pose pose, VertexConsumer consumer) {
            var overlay = canPlace ? OverlayTexture.NO_OVERLAY : OverlayTexture.pack(OverlayTexture.RED_OVERLAY_V, OverlayTexture.NO_WHITE_U);
            var wrapped = new AlphaVertexConsumer(consumer, 190);

            for(var entry : blockStates.long2ObjectEntrySet()) {
                var pos = BlockPos.of(entry.getLongKey());
                var blockState = entry.getValue();

                PlacementPreviewHandler.renderBlockState(pose, wrapped, level, pos, blockState, overlay);
            }
        }
    }
}
