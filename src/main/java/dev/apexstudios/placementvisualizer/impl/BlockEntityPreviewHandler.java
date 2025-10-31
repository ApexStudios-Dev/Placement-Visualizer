package dev.apexstudios.placementvisualizer.impl;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import java.util.Collections;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public interface BlockEntityPreviewHandler {
    @Nullable
    static BlockEntityRenderState extract(LevelRenderState levelState, Level level, float partialTick, @Nullable DataComponentGetter components, BlockState blockState, BlockPos pos) {
        var blockEntity = createAndLoadBlockEntity(level, components, blockState, pos);

        if(blockEntity == null) {
            return null;
        }

        return createAndExtractRenderState(levelState, blockEntity, partialTick);
    }

    static List<BlockEntityRenderState> extractAll(LevelRenderState levelState, Level level, float partialTick, @Nullable DataComponentGetter components, Long2ObjectMap<BlockState> blockStates) {
        var renderStates = Lists.<BlockEntityRenderState>newArrayList();

        blockStates.forEach((posId, blockState) -> {
            var pos = BlockPos.of(posId);
            var renderState = extract(levelState, level, partialTick, components, blockState, pos);

            if(renderState != null) {
                renderStates.add(renderState);
            }
        });

        return Collections.unmodifiableList(renderStates);
    }

    static void submit(PoseStack pose, SubmitNodeCollector collector, LevelRenderState levelState, @Nullable BlockEntityRenderState renderState) {
        if(renderState == null) {
            return;
        }

        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(renderState);

        if(renderer != null) {
            pose.pushPose();
            pose.translate(
                    renderState.blockPos.getX(),
                    renderState.blockPos.getY(),
                    renderState.blockPos.getZ()
            );

            renderer.submit(renderState, pose, collector, levelState.cameraRenderState);
            pose.popPose();
        }
    }

    static void submitAll(PoseStack pose, SubmitNodeCollector collector, LevelRenderState levelState, List<BlockEntityRenderState> renderStates) {
        renderStates.forEach(renderState -> submit(pose, collector, levelState, renderState));
    }

    @Nullable
    private static BlockEntity createAndLoadBlockEntity(Level level, @Nullable DataComponentGetter components, BlockState blockState, BlockPos pos) {
        if(!blockState.hasBlockEntity()) {
            return null;
        }

        var blockEntity = ((EntityBlock) blockState.getBlock()).newBlockEntity(pos, blockState);

        if(blockEntity == null) {
            return null;
        }

        blockEntity.setLevel(level);

        if(components != null) {
            var data = components.get(DataComponents.BLOCK_ENTITY_DATA);

            if(data != null && blockEntity.getType() == data.type()) {
                data.loadInto(blockEntity, level.registryAccess());
            }
        }

        return blockEntity;
    }

    @Nullable
    private static BlockEntityRenderState createAndExtractRenderState(LevelRenderState levelState, BlockEntity blockEntity, float partialTick) {
        var renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);

        if(renderer == null) {
            return null;
        }

        var renderState = renderer.createRenderState();
        renderer.extractRenderState(blockEntity, renderState, partialTick, levelState.cameraRenderState.pos, null);
        return renderState;
    }
}
