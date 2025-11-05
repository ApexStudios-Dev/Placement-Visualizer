package dev.apexstudios.placementvisualizer.impl;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.apexstudios.placementvisualizer.api.BlockItemPlacementEvent;
import dev.apexstudios.placementvisualizer.api.GhostRenderUtils;
import dev.apexstudios.placementvisualizer.api.PlacementPreviewHandler;
import dev.apexstudios.placementvisualizer.api.PlacementRenderTypes;
import dev.apexstudios.placementvisualizer.impl.node.GhostNodeStorage;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.List;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.Nullable;

final class BlockItemPreviewHandler implements PlacementPreviewHandler<BlockItemPreviewHandler.State> {
    @Override
    @Nullable
    public State extract(LevelRenderState levelState, Level level, BlockHitResult hitResult, Player player, InteractionHand hand) {
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
        var placementPos = placeContext.getClickedPos();

        // placement failed if invalid context or event was cancelled
        // similar result to returning null in 'BlockItem.updatePlacementContext'
        if(canPlace && (!placeContext.canPlace() || eventUPC.isCanceled())) {
            canPlace = false;
        }

        // mark as invalid placement if out of bounds
        // 'level.isInWorldBounds' only checks the max/min world bounds
        // but we are after the world border bounds
        if(canPlace && !level.getWorldBorder().isWithinBounds(placementPos)) {
            canPlace = false;
        }

        // make as invalid placement if gamemode or data components dictate so
        // mainly for adventure mode + 'can_place_on' data component
        if(canPlace && !player.mayUseItemAt(placementPos, hitResult.getDirection(), stack)) {
            canPlace = false;
        }

        // 3) determine default block state
        var defaultBlockState = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.GetDefaultBlockState(placeContext, block)).blockState();

        // 4) load block state data from data components
        // copied from 'BlockItem#updateBlockStateFromTag'
        // since vanilla tries to 'setBlock' if state changed
        var blockStateProperties = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);

        // load block properties onto default block state
        if(!blockStateProperties.isEmpty()) {
            defaultBlockState = blockStateProperties.apply(defaultBlockState);
        }

        // 5) determine placement block state
        var eventGPBS = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.GetPlacementBlockState(placeContext, defaultBlockState));
        var placementBlockState = eventGPBS.blockState();

        // load block properties onto placement block state
        if(!blockStateProperties.isEmpty()) {
            placementBlockState = blockStateProperties.apply(placementBlockState);
        }

        // canceling this event means block placement failed
        // similar result to returning null in 'Block.getStateForPlacement'
        if(canPlace && eventGPBS.isCanceled()) {
            canPlace = false;
        }

        // 6) strip invalid block states
        // by default this strips out waterlogged
        placementBlockState = NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.StripInvalidProperties(defaultBlockState, placementBlockState)).placementBlockState();

        // 7) collect additional block states
        var blockStates = new Long2ObjectOpenHashMap<BlockState>();
        NeoForge.EVENT_BUS.post(new BlockItemPlacementEvent.CollectAdditionalBlockStates(placeContext, placementBlockState, blockStates));
        blockStates.put(placementPos.asLong(), placementBlockState); // ensure origin point can not be overwritten

        // 8) Extract block entity render states
        var blockEntityRenderStates = BlockEntityPreviewHandler.extractAll(levelState, level, 0F, stack, blockStates);

        // 9) return finalized render state
        return new State(
                level,
                canPlace,
                Long2ObjectMaps.unmodifiable(blockStates),
                blockEntityRenderStates
        );
    }

    @Override
    public void submit(RenderLevelStageEvent event, GhostNodeStorage collector, State state) {
        collector.validPlacement(state.canPlace);

        var levelState = event.getLevelRenderState();

        var pose = event.getPoseStack();
        pose.pushPose();
        pose.translate(levelState.cameraRenderState.pos.scale(-1D));

        if(event instanceof RenderLevelStageEvent.AfterTranslucentBlocks) {
            GhostRenderUtils.submitGhost(collector, pose, PlacementRenderTypes.translucentNoDepth(), state::render);
        } else if(event instanceof RenderLevelStageEvent.AfterEntities) {
            BlockEntityPreviewHandler.submitAll(pose, collector, levelState, state.blockEntityRenderStates);
        }

        pose.popPose();
    }

    record State(LevelAccessor level, boolean canPlace, Long2ObjectMap<BlockState> blockStates, List<BlockEntityRenderState> blockEntityRenderStates) {
        public void render(PoseStack pose, VertexConsumer consumer) {
            for(var entry : blockStates.long2ObjectEntrySet()) {
                var pos = BlockPos.of(entry.getLongKey());
                var blockState = entry.getValue();

                GhostRenderUtils.renderBlockState(pose, consumer, level, pos, blockState, canPlace);
            }
        }
    }
}
