package dev.apexstudios.placementvisualizer;

import dev.apexstudios.placementvisualizer.api.BlockItemPlacementEvent;
import dev.apexstudios.placementvisualizer.impl.PlacementVisualizer;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.metadata.PackMetadataGenerator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@Mod(value = PlacementVisualizer.ID, dist = Dist.CLIENT)
public final class PlacementVisualizerDataEntryPoint {
    public PlacementVisualizerDataEntryPoint(IEventBus modBus) {
        modBus.addListener(GatherDataEvent.Client.class, event -> {
            event.createProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.literal("PackVisualizer")));

            // 'vanilla vanilla' data pack enables the visualizer for all vanilla blocks
            var subPack = event.getGenerator().getPackGenerator(true, "visual_vanilla", "visual_vanilla");
            subPack.addProvider(output -> PackMetadataGenerator.forFeaturePack(output, Component.literal("Visual Vanilla")));

            subPack.addProvider(output -> new BlockTagsProvider(output, event.getLookupProvider(), PlacementVisualizer.ID) {
                @Override
                protected void addTags(HolderLookup.Provider provider) {
                    provider.lookupOrThrow(Registries.BLOCK)
                            .listElements()
                            .map(Holder::value)
                            .filter(block -> block.asItem() instanceof BlockItem)
                            .forEach(block -> tag(BlockItemPlacementEvent.RENDERABLES).add(block));
                }
            });
        });
    }
}
