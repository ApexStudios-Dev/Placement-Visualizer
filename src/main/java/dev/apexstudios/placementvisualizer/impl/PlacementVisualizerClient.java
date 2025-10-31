package dev.apexstudios.placementvisualizer.impl;

import dev.apexstudios.placementvisualizer.api.BlockItemPlacementEvent;
import dev.apexstudios.placementvisualizer.api.PlacementPreviewHandler;
import dev.apexstudios.placementvisualizer.api.PlacementRenderTypes;
import dev.apexstudios.placementvisualizer.mixin.BlockItemAccessor;
import net.minecraft.client.gui.components.debug.DebugEntryNoop;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.RegisterDebugEntriesEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = PlacementVisualizer.ID, dist = Dist.CLIENT)
public final class PlacementVisualizerClient {
    public PlacementVisualizerClient(IEventBus modBus) {
        addRequiredListeners(modBus);
        addBlockItemListeners();
    }

    private void addRequiredListeners(IEventBus modBus) {
        modBus.addListener(FMLClientSetupEvent.class, event -> event.enqueueWork(() -> PlacementPreviewHandler.register(PlacementVisualizer.identifier("block_item"), new BlockItemPreviewHandler())));
        modBus.addListener(RegisterRenderPipelinesEvent.class, event -> event.registerPipeline(PlacementRenderTypes.Pipelines.TRANSLUCENT_NO_DEPTH));
        modBus.addListener(RegisterDebugEntriesEvent.class, event -> event.register(PlacementPreviewRegistry.DEBUG_KEY, new DebugEntryNoop()));

        NeoForge.EVENT_BUS.addListener(ExtractLevelRenderStateEvent.class, PlacementPreviewRegistry::extract);

        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterSky.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterOpaqueBlocks.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterEntities.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterTranslucentBlocks.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterTripwireBlocks.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterParticles.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterWeather.class, PlacementPreviewRegistry::submit);
        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.AfterLevel.class, PlacementPreviewRegistry::submit);
    }

    private void addBlockItemListeners() {
        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.UpdatePlacementContext.class, event -> {
            var placeContext = event.placeContext();

            if(placeContext.getItemInHand().getItem() instanceof BlockItem blockItem) {
                event.setPlaceContext(blockItem.updatePlacementContext(placeContext));
            }
        });

        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.GetDefaultBlockState.class, this::getBlockState);
        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.GetPlacementBlockState.class, this::getBlockState);

        NeoForge.EVENT_BUS.addListener(EventPriority.LOW, true, BlockItemPlacementEvent.GetPlacementBlockState.class, event -> {
            if(!event.isCanceled())
                return;

            // some blocks lose facing data when placement fails
            // this ensures these blocks get the corrected facing properties

            if(event.blockState().getBlock() instanceof BedBlock && event.blockState().hasProperty(BedBlock.FACING))
                event.setBlockState(event.blockState().setValue(BedBlock.FACING, event.placeContext().getHorizontalDirection()));
        });

        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.StripInvalidProperties.class, event -> event.strip(BlockStateProperties.WATERLOGGED));

        NeoForge.EVENT_BUS.addListener(BlockItemPlacementEvent.CollectAdditionalBlockStates.class, event -> {
            var blockState = event.blockState();

            if(blockState.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                var half = blockState.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF);
                var otherHalf = half.getOtherHalf();
                var otherOffset = half.getDirectionToOther();
                var otherPos = event.placeContext().getClickedPos().relative(otherOffset);
                event.with(otherPos, blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, otherHalf));
            }

            if(blockState.hasProperty(BlockStateProperties.BED_PART)) {
                var part = blockState.getValue(BlockStateProperties.BED_PART);

                var otherPart = switch(part) {
                    case FOOT -> BedPart.HEAD;
                    case HEAD -> BedPart.FOOT;
                };

                var otherOffset = BedBlock.getConnectedDirection(blockState);
                var otherPos = event.placeContext().getClickedPos().relative(otherOffset);
                event.with(otherPos, blockState.setValue(BlockStateProperties.BED_PART, otherPart));
            }
        });
    }

    private void getBlockState(BlockItemPlacementEvent.GetBlockState event) {
        var placeContext = event.placeContext();

        if(placeContext.getItemInHand().getItem() instanceof BlockItem blockItem) {
            var blockState = ((BlockItemAccessor) blockItem).PlacementVisualizer$getPlacementState(placeContext);
            var forPlacement = event instanceof BlockItemPlacementEvent.GetPlacementBlockState;

            // null here means 'BlockItem.canPlace' returned false
            // 'GetDefaultBlockState' requires non-null
            // 'GetPlacementBlockState' cancels event on null block states (cancelled means placement failed)
            if(blockState != null || forPlacement)
                event.setBlockState(blockState);
        }
    }
}
