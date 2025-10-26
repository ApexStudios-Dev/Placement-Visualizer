package dev.apexstudios.placementvisualizer.api;

import com.google.errorprone.annotations.OverridingMethodsMustInvokeSuper;
import dev.apexstudios.placementvisualizer.impl.PlacementVisualizer;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class BlockItemPlacementEvent {
    public static final TagKey<Block> RENDERABLES = BlockTags.create(PlacementVisualizer.identifier("renderable"));

    public static final class UpdatePlacementContext extends Event implements ICancellableEvent {
        private BlockPlaceContext placeContext;

        @ApiStatus.Internal
        public UpdatePlacementContext(Level level, Player player, InteractionHand hand, ItemStack stack, BlockHitResult hitResult) {
            placeContext = new BlockPlaceContext(level, player, hand, stack, hitResult);
        }

        public BlockPlaceContext placeContext() {
            return placeContext;
        }

        public void setPlaceContext(@Nullable BlockPlaceContext placeContext) {
            if(placeContext == null)
                setCanceled(true);
            else
                this.placeContext = placeContext;
        }
    }

    public static final class GetBlock extends Event {
        private final ItemResource item;
        private Block block;

        @ApiStatus.Internal
        public GetBlock(ItemResource item) {
            this.item = item;

            block = Block.byItem(item.value());
        }

        public ItemResource item() {
            return item;
        }

        public Block block() {
            return block;
        }

        public void setBlock(Block block) {
            this.block = block;
        }
    }

    public static sealed class GetBlockState extends Event {
        private final BlockPlaceContext placeContext;
        private BlockState blockState;

        private GetBlockState(BlockPlaceContext placeContext, BlockState blockState) {
            this.placeContext = placeContext;
            this.blockState = blockState;
        }

        public final BlockPlaceContext placeContext() {
            return placeContext;
        }

        public final BlockState blockState() {
            return blockState;
        }

        @OverridingMethodsMustInvokeSuper
        public void setBlockState(BlockState blockState) {
            this.blockState = blockState;
        }
    }

    public static final class GetDefaultBlockState extends GetBlockState {
        @ApiStatus.Internal
        public GetDefaultBlockState(BlockPlaceContext placeContext, Block block) {
            super(placeContext, block.defaultBlockState());
        }
    }

    public static final class GetPlacementBlockState extends GetBlockState implements ICancellableEvent {
        @ApiStatus.Internal
        public GetPlacementBlockState(BlockPlaceContext placeContext, BlockState defaultBlockState) {
            super(placeContext, getInitialBlockState(placeContext, defaultBlockState));
        }

        @Override
        public void setBlockState(@Nullable BlockState blockState) {
            if(blockState == null)
                setCanceled(true);
            else
                super.setBlockState(blockState);
        }

        private static BlockState getInitialBlockState(BlockPlaceContext placeContext, BlockState defaultBlockState) {
            var placementBlockState = defaultBlockState.getBlock().getStateForPlacement(placeContext);
            return placementBlockState == null ? defaultBlockState : placementBlockState;
        }
    }

    public static final class CollectAdditionalBlockStates extends Event {
        private final BlockPlaceContext placeContext;
        private final BlockState blockState;
        private final Long2ObjectMap<BlockState> additionalBlockStates;

        @ApiStatus.Internal
        public CollectAdditionalBlockStates(BlockPlaceContext placeContext, BlockState blockState, Long2ObjectMap<BlockState> additionalBlockStates) {
            this.placeContext = placeContext;
            this.blockState = blockState;
            this.additionalBlockStates = additionalBlockStates;
        }

        public BlockPlaceContext placeContext() {
            return placeContext;
        }

        public BlockState blockState() {
            return blockState;
        }

        public void with(BlockPos pos, BlockState blockState) {
            additionalBlockStates.put(pos.asLong(), blockState);
        }
    }

    public static final class StripInvalidProperties extends Event {
        private final BlockState defaultBlockState;
        private BlockState placementBlockState;

        @ApiStatus.Internal
        public StripInvalidProperties(BlockState defaultBlockState, BlockState placementBlockState) {
            this.defaultBlockState = defaultBlockState;
            this.placementBlockState = placementBlockState;
        }

        public BlockState defaultBlockState() {
            return defaultBlockState;
        }

        public BlockState placementBlockState() {
            return placementBlockState;
        }

        public <T extends Comparable<T>> void strip(Property<T> property) {
            if(defaultBlockState.hasProperty(property))
                placementBlockState = copy(defaultBlockState, placementBlockState, property);
            else
                placementBlockState = copy(placementBlockState.getBlock().defaultBlockState(), placementBlockState, property);
        }

        public void strip(Property<?> property, Property<?>... properties) {
            strip(property);

            for(var prop : properties) {
                strip(prop);
            }
        }

        private static <T extends Comparable<T>> BlockState copy(BlockState from, BlockState into, Property<T> property) {
            if(!from.hasProperty(property))
                return into;

            return into.trySetValue(property, from.getValue(property));
        }
    }
}
