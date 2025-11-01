package dev.apexstudios.placementvisualizer.impl;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@Mod(value = PlacementVisualizer.ID, dist = Dist.CLIENT)
public final class PlacementVisualizer {
    public static final String ID = "placementvisualizer";

    public PlacementVisualizer(IEventBus modBus) {
        modBus.addListener(AddPackFindersEvent.class, event ->  event.addPackFinders(
                identifier("visual_vanilla"),
                PackType.SERVER_DATA,
                Component.literal("Visual Vanilla"),
                PackSource.create(PackSource.BUILT_IN::decorate, false),
                false,
                Pack.Position.TOP
        ));

        // TODO: Remove me
    }

    public static ResourceLocation identifier(String identifier) {
        return ResourceLocation.fromNamespaceAndPath(ID, identifier);
    }

    public static String id(String identifier) {
        return ID + ResourceLocation.NAMESPACE_SEPARATOR + identifier;
    }
}
