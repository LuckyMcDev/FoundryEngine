package de.luckymcdev.foundryengine.common.builder.block;

import de.luckymcdev.foundryengine.api.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.BuilderBaseImpl;
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

public class BlockBuilderImpl extends BuilderBaseImpl<Block> implements BlockBuilder {
    private final BiFunction<Block, Item.Properties, Item> itemFactory;
    private final Map<EngineBlock.CallbackType, Object> blockCallbacks = new EnumMap<>(EngineBlock.CallbackType.class);
    private final Map<EngineItem.CallbackType, Object> itemCallbacks = new EnumMap<>(EngineItem.CallbackType.class);
    private BlockBehaviour.Properties properties;
    private Function<BlockBehaviour.Properties, Block> blockFactory;
    private boolean hasItem = true;
    private UnaryOperator<Item.Properties> itemPropertyModifier = p -> p;

    public BlockBuilderImpl(Identifier id) {
        super(id);
        this.registryKey = Registries.BLOCK;
        this.properties = BlockBehaviour.Properties.of();
        this.blockFactory = EngineBlock::new;
        this.itemFactory = BlockItem::new;
    }

    private <C> BlockBuilder blockCallback(EngineBlock.CallbackType type, C cb) {
        blockCallbacks.put(type, cb);
        return this;
    }

    private <C> BlockBuilder itemCallback(EngineItem.CallbackType type, C cb) {
        itemCallbacks.put(type, cb);
        return this;
    }

    @Override
    public BlockBuilder animateTick(EngineBlock.AnimateTickCallback cb) {
        return blockCallback(EngineBlock.CallbackType.ANIMATE_TICK, cb);
    }

    @Override
    public BlockBuilder destroy(EngineBlock.DestroyCallback cb) {
        return blockCallback(EngineBlock.CallbackType.DESTROY, cb);
    }

    @Override
    public BlockBuilder wasExploded(EngineBlock.WasExplodedCallback cb) {
        return blockCallback(EngineBlock.CallbackType.WAS_EXPLODED, cb);
    }

    @Override
    public BlockBuilder stepOn(EngineBlock.StepOnCallback cb) {
        return blockCallback(EngineBlock.CallbackType.STEP_ON, cb);
    }

    @Override
    public BlockBuilder setPlacedBy(EngineBlock.SetPlacedByCallback cb) {
        return blockCallback(EngineBlock.CallbackType.SET_PLACED_BY, cb);
    }

    @Override
    public BlockBuilder fallOn(EngineBlock.FallOnCallback cb) {
        return blockCallback(EngineBlock.CallbackType.FALL_ON, cb);
    }

    @Override
    public BlockBuilder playerWillDestroy(EngineBlock.PlayerWillDestroyCallback cb) {
        return blockCallback(EngineBlock.CallbackType.PLAYER_WILL_DESTROY, cb);
    }

    @Override
    public BlockBuilder playerDestroy(EngineBlock.PlayerDestroyCallback cb) {
        return blockCallback(EngineBlock.CallbackType.PLAYER_DESTROY, cb);
    }

    @Override
    public BlockBuilder handlePrecipitation(EngineBlock.HandlePrecipitationCallback cb) {
        return blockCallback(EngineBlock.CallbackType.HANDLE_PRECIPITATION, cb);
    }

    @Override
    public BlockBuilder itemInventoryTick(EngineItem.InventoryTickCallback cb) {
        return itemCallback(EngineItem.CallbackType.INVENTORY_TICK, cb);
    }

    @Override
    public BlockBuilder itemUse(EngineItem.UseCallback cb) {
        return itemCallback(EngineItem.CallbackType.USE, cb);
    }

    @Override
    public BlockBuilder itemUseOn(EngineItem.UseOnCallback cb) {
        return itemCallback(EngineItem.CallbackType.USE_ON, cb);
    }

    @Override
    public BlockBuilder itemFinishUsing(EngineItem.FinishUsingItemCallback cb) {
        return itemCallback(EngineItem.CallbackType.FINISH_USING_ITEM, cb);
    }

    @Override
    public BlockBuilder itemHurtEnemy(EngineItem.HurtEnemyCallback cb) {
        return itemCallback(EngineItem.CallbackType.HURT_ENEMY, cb);
    }

    @Override
    public BlockBuilder itemPostHurtEnemy(EngineItem.PostHurtEnemyCallback cb) {
        return itemCallback(EngineItem.CallbackType.POST_HURT_ENEMY, cb);
    }

    @Override
    public BlockBuilder itemReleaseUsing(EngineItem.ReleaseUsingCallback cb) {
        return itemCallback(EngineItem.CallbackType.RELEASE_USING, cb);
    }

    @Override
    public BlockBuilder itemOnCraftedPostProcess(EngineItem.OnCraftedPostProcessCallback cb) {
        return itemCallback(EngineItem.CallbackType.ON_CRAFTED_POST_PROCESS, cb);
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
    public Block registerBlock(RegisterEvent.RegisterHelper<Block> helper) {
        Block block = build();
        helper.register(this.id, block);
        this.object = block;
        return block;
    }

    @Override
    public Item registerItem(RegisterEvent.RegisterHelper<Item> helper) {
        if (!hasItem) {
            throw new IllegalStateException("Cannot register item for block " + id + " because noItem() was called.");
        }

        ItemBuilderImpl itemBuilder = (ItemBuilderImpl) new ItemBuilderImpl(id)
                .factory(props -> itemFactory.apply(object, props))
                .properties(itemPropertyModifier);

        itemCallbacks.forEach(itemBuilder::callback);

        return itemBuilder.register(helper);
    }

    @Override
    public Block build() {
        this.properties.setId(ResourceKey.create(Registries.BLOCK, id));

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
}