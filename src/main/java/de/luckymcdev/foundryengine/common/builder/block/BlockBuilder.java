package de.luckymcdev.foundryengine.common.builder.block;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
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

public class BlockBuilder extends AbstractBuilder<Block> {
	private final BiFunction<Block, Item.Properties, Item> itemFactory;
	private final Map<EngineBlock.CallbackType, Object> blockCallbacks = new EnumMap<>(EngineBlock.CallbackType.class);
	private final Map<EngineItem.CallbackType, Object> itemCallbacks = new EnumMap<>(EngineItem.CallbackType.class);
	private BlockBehaviour.Properties properties;
	private Function<BlockBehaviour.Properties, Block> blockFactory;
	private boolean hasItem = true;
	private UnaryOperator<Item.Properties> itemPropertyModifier = p -> p;

	public BlockBuilder(Identifier id) {
		super(id);
		this.properties = BlockBehaviour.Properties.of();
		this.blockFactory = EngineBlock::new;
		this.itemFactory = BlockItem::new;
	}

	public static BlockBuilder create(Identifier id) {
		return new BlockBuilder(id);
	}

	public BlockBuilder factory(Function<BlockBehaviour.Properties, Block> factory) {
		this.blockFactory = factory;
		return this;
	}

	public BlockBuilder properties(UnaryOperator<BlockBehaviour.Properties> action) {
		this.properties = action.apply(this.properties);
		return this;
	}

	public BlockBuilder noItem() {
		this.hasItem = false;
		return this;
	}

	public boolean hasItem() {
		return this.hasItem;
	}

	public BlockBuilder itemProperties(UnaryOperator<Item.Properties> action) {
		this.itemPropertyModifier = action;
		return this;
	}

	public BlockBuilder animateTick(EngineBlock.AnimateTickCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.ANIMATE_TICK, cb);
		return this;
	}

	public BlockBuilder destroy(EngineBlock.DestroyCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.DESTROY, cb);
		return this;
	}

	public BlockBuilder wasExploded(EngineBlock.WasExplodedCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.WAS_EXPLODED, cb);
		return this;
	}

	public BlockBuilder stepOn(EngineBlock.StepOnCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.STEP_ON, cb);
		return this;
	}

	public BlockBuilder setPlacedBy(EngineBlock.SetPlacedByCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.SET_PLACED_BY, cb);
		return this;
	}

	public BlockBuilder fallOn(EngineBlock.FallOnCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.FALL_ON, cb);
		return this;
	}

	public BlockBuilder playerWillDestroy(EngineBlock.PlayerWillDestroyCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.PLAYER_WILL_DESTROY, cb);
		return this;
	}

	public BlockBuilder playerDestroy(EngineBlock.PlayerDestroyCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.PLAYER_DESTROY, cb);
		return this;
	}

	public BlockBuilder handlePrecipitation(EngineBlock.HandlePrecipitationCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.HANDLE_PRECIPITATION, cb);
		return this;
	}

	public BlockBuilder itemInventoryTick(EngineItem.InventoryTickCallback cb) {
		itemCallbacks.put(EngineItem.CallbackType.INVENTORY_TICK, cb);
		return this;
	}

	public BlockBuilder itemUse(EngineItem.UseCallback cb) {
		itemCallbacks.put(EngineItem.CallbackType.USE, cb);
		return this;
	}

	public BlockBuilder itemUseOn(EngineItem.UseOnCallback cb) {
		itemCallbacks.put(EngineItem.CallbackType.USE_ON, cb);
		return this;
	}

	public BlockBuilder itemFinishUsing(EngineItem.FinishUsingItemCallback cb) {
		itemCallbacks.put(EngineItem.CallbackType.FINISH_USING_ITEM, cb);
		return this;
	}

	public BlockBuilder itemHurtEnemy(EngineItem.HurtEnemyCallback cb) {
		itemCallbacks.put(EngineItem.CallbackType.HURT_ENEMY, cb);
		return this;
	}

	public BlockBuilder itemPostHurtEnemy(EngineItem.PostHurtEnemyCallback cb) {
		itemCallbacks.put(EngineItem.CallbackType.POST_HURT_ENEMY, cb);
		return this;
	}

	public BlockBuilder itemReleaseUsing(EngineItem.ReleaseUsingCallback cb) {
		itemCallbacks.put(EngineItem.CallbackType.RELEASE_USING, cb);
		return this;
	}

	public BlockBuilder itemOnCraftedPostProcess(EngineItem.OnCraftedPostProcessCallback cb) {
		itemCallbacks.put(EngineItem.CallbackType.ON_CRAFTED_POST_PROCESS, cb);
		return this;
	}

	public Block registerBlock(RegisterEvent.RegisterHelper<Block> helper) {
		Block block = build();
		helper.register(id, block);
		setObject(block);
		return block;
	}

	public Item registerItem(RegisterEvent.RegisterHelper<Item> helper) {
		if (!hasItem) {
			throw new IllegalStateException("Cannot register item for block " + id + " because noItem() was called.");
		}

		ItemBuilder itemBuilder = new ItemBuilder(id)
			.factory(props -> itemFactory.apply(object, props))
			.properties(itemPropertyModifier)
			.generateData(generateData);

		itemCallbacks.forEach(itemBuilder::callback);
		return itemBuilder.register(helper);
	}

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
					case HANDLE_PRECIPITATION -> block.handlePrecipitation((EngineBlock.HandlePrecipitationCallback) cb);
				}
			});
			return block;
		}

		return blockFactory.apply(this.properties);
	}

	<C> BlockBuilder callback(EngineItem.CallbackType type, C cb) {
		itemCallbacks.put(type, cb);
		return this;
	}

	public BlockBuilder generateData(boolean generate) {
		this.generateData = generate;
		return this;
	}
}
