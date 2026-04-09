package de.luckymcdev.foundryengine.common.builder.block;

import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.BuilderState;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilderImpl;
import de.luckymcdev.foundryengine.common.world.block.EngineBlock;
import de.luckymcdev.foundryengine.common.world.item.EngineItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Block Builder using composition instead of inheritance.
 * Much cleaner and allows for better code organization.
 */
public class BlockBuilderImpl implements BlockBuilder {
    private final BuilderState<Block> state;
    private final BiFunction<Block, Item.Properties, Item> itemFactory;
    private final Map<EngineBlock.CallbackType, Object> blockCallbacks = new EnumMap<>(EngineBlock.CallbackType.class);
    private final Map<EngineItem.CallbackType, Object> itemCallbacks = new EnumMap<>(EngineItem.CallbackType.class);
    private BlockBehaviour.Properties properties;
    private Function<BlockBehaviour.Properties, Block> blockFactory;
    private boolean hasItem = true;
    private UnaryOperator<Item.Properties> itemPropertyModifier = p -> p;

    public BlockBuilderImpl(Identifier id) {
        this.state = new BuilderState<>(id);
        this.state.registryKey = Registries.BLOCK;
        this.properties = BlockBehaviour.Properties.of();
        this.blockFactory = EngineBlock::new;
        this.itemFactory = BlockItem::new;
    }

    @Override
    public BlockBuilder factory(Function<BlockBehaviour.Properties, Block> factory) {
        this.blockFactory = factory;
        return this;
    }

    @Override
    public BlockBuilder properties(UnaryOperator<BlockBehaviour.Properties> action) {
        this.properties = action.apply(this.properties);
        return this;
    }

    @Override
    public BlockBuilder noItem() {
        this.hasItem = false;
        return this;
    }

    @Override
    public boolean hasItem() {
        return this.hasItem;
    }

    @Override
    public BlockBuilder itemProperties(UnaryOperator<Item.Properties> action) {
        this.itemPropertyModifier = action;
        return this;
    }

    @Override
    public BlockBuilder animateTick(EngineBlock.AnimateTickCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.ANIMATE_TICK, cb);
        return this;
    }

    @Override
    public BlockBuilder destroy(EngineBlock.DestroyCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.DESTROY, cb);
        return this;
    }

    @Override
    public BlockBuilder wasExploded(EngineBlock.WasExplodedCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.WAS_EXPLODED, cb);
        return this;
    }

    @Override
    public BlockBuilder stepOn(EngineBlock.StepOnCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.STEP_ON, cb);
        return this;
    }

    @Override
    public BlockBuilder setPlacedBy(EngineBlock.SetPlacedByCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.SET_PLACED_BY, cb);
        return this;
    }

    @Override
    public BlockBuilder fallOn(EngineBlock.FallOnCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.FALL_ON, cb);
        return this;
    }

    @Override
    public BlockBuilder playerWillDestroy(EngineBlock.PlayerWillDestroyCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.PLAYER_WILL_DESTROY, cb);
        return this;
    }

    @Override
    public BlockBuilder playerDestroy(EngineBlock.PlayerDestroyCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.PLAYER_DESTROY, cb);
        return this;
    }

    @Override
    public BlockBuilder handlePrecipitation(EngineBlock.HandlePrecipitationCallback cb) {
        blockCallbacks.put(EngineBlock.CallbackType.HANDLE_PRECIPITATION, cb);
        return this;
    }

    @Override
    public BlockBuilder itemInventoryTick(EngineItem.InventoryTickCallback cb) {
        itemCallbacks.put(EngineItem.CallbackType.INVENTORY_TICK, cb);
        return this;
    }

    @Override
    public BlockBuilder itemUse(EngineItem.UseCallback cb) {
        itemCallbacks.put(EngineItem.CallbackType.USE, cb);
        return this;
    }

    @Override
    public BlockBuilder itemUseOn(EngineItem.UseOnCallback cb) {
        itemCallbacks.put(EngineItem.CallbackType.USE_ON, cb);
        return this;
    }

    @Override
    public BlockBuilder itemFinishUsing(EngineItem.FinishUsingItemCallback cb) {
        itemCallbacks.put(EngineItem.CallbackType.FINISH_USING_ITEM, cb);
        return this;
    }

    @Override
    public BlockBuilder itemHurtEnemy(EngineItem.HurtEnemyCallback cb) {
        itemCallbacks.put(EngineItem.CallbackType.HURT_ENEMY, cb);
        return this;
    }

    @Override
    public BlockBuilder itemPostHurtEnemy(EngineItem.PostHurtEnemyCallback cb) {
        itemCallbacks.put(EngineItem.CallbackType.POST_HURT_ENEMY, cb);
        return this;
    }

    @Override
    public BlockBuilder itemReleaseUsing(EngineItem.ReleaseUsingCallback cb) {
        itemCallbacks.put(EngineItem.CallbackType.RELEASE_USING, cb);
        return this;
    }

    @Override
    public BlockBuilder itemOnCraftedPostProcess(EngineItem.OnCraftedPostProcessCallback cb) {
        itemCallbacks.put(EngineItem.CallbackType.ON_CRAFTED_POST_PROCESS, cb);
        return this;
    }

    @Override
    public Block registerBlock(RegisterEvent.RegisterHelper<Block> helper) {
        Block block = build();
        helper.register(state.id, block);
        state.setObject(block);
        return block;
    }

    @Override
    public Item registerItem(RegisterEvent.RegisterHelper<Item> helper) {
        if (!hasItem) {
            throw new IllegalStateException("Cannot register item for block " + state.id + " because noItem() was called.");
        }

        ItemBuilderImpl itemBuilder = (ItemBuilderImpl) new ItemBuilderImpl(state.id)
                .factory(props -> itemFactory.apply(state.object, props))
                .properties(itemPropertyModifier);

        itemCallbacks.forEach(itemBuilder::callback);

        return itemBuilder.register(helper);
    }

    @Override
    public Block build() {
        this.properties.setId(ResourceKey.create(Registries.BLOCK, state.id));

        if (!blockCallbacks.isEmpty()) {
            EngineBlock block = new EngineBlock(this.properties);
            blockCallbacks.forEach((type, cb) -> {
                switch (type) {
                    case ANIMATE_TICK -> block.animateTick((EngineBlock.AnimateTickCallback) cb);
                    case DESTROY -> block.destroy((EngineBlock.DestroyCallback) cb);
                    case WAS_EXPLODED -> block.wasExploded((EngineBlock.WasExplodedCallback) cb);
                    case STEP_ON -> block.stepOn((EngineBlock.StepOnCallback) cb);
                    case SET_PLACED_BY -> block.setPlacedBy((EngineBlock.SetPlacedByCallback) cb);
                    case FALL_ON -> block.fallOn((EngineBlock.FallOnCallback) cb);
                    case PLAYER_WILL_DESTROY -> block.playerWillDestroy((EngineBlock.PlayerWillDestroyCallback) cb);
                    case PLAYER_DESTROY -> block.playerDestroy((EngineBlock.PlayerDestroyCallback) cb);
                    case HANDLE_PRECIPITATION ->
                            block.handlePrecipitation((EngineBlock.HandlePrecipitationCallback) cb);
                }
            });
            return block;
        }

        return blockFactory.apply(this.properties);
    }

    @Override
    public Block get() {
        return state.get();
    }

    @Override
    public Block getOrCreate() {
        return state.getOrCreate();
    }

    @Override
    public Identifier newID(String pre, String post) {
        return state.newID(pre, post);
    }

    /**
     * Internal method for ItemBuilderImpl to register callbacks
     */
    <C> BlockBuilder callback(EngineItem.CallbackType type, C cb) {
        itemCallbacks.put(type, cb);
        return this;
    }
}