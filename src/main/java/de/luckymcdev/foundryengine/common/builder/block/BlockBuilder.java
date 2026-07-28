package de.luckymcdev.foundryengine.common.builder.block;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import de.luckymcdev.foundryengine.common.builder.blockentity.BlockEntityBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import de.luckymcdev.foundryengine.common.builder.tag.BlockTagBuilder;
import de.luckymcdev.foundryengine.common.world.block.EngineBlock;
import de.luckymcdev.foundryengine.common.world.block.EngineEntityBlock;
import de.luckymcdev.foundryengine.common.world.item.EngineItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.EndRodBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LightningRodBlock;
import net.minecraft.world.level.block.ObserverBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class BlockBuilder extends AbstractBuilder<Block> {
	private final BiFunction<Block, Item.Properties, Item> itemFactory;
	private final Map<EngineBlock.CallbackType, Object> blockCallbacks = new EnumMap<>(EngineBlock.CallbackType.class);
	private final Map<EngineItem.CallbackType, Object> itemCallbacks = new EnumMap<>(EngineItem.CallbackType.class);
	private final List<BlockTagBuilder> tags = new ArrayList<>();
	private BlockBehaviour.Properties properties;
	private Function<BlockBehaviour.Properties, Block> blockFactory;
	private boolean hasItem = true;
	private UnaryOperator<Item.Properties> itemPropertyModifier = p -> p;
	@Nullable
	private BiPredicate<Player, BlockState> visibilityCondition;
	@Nullable
	private BlockEntityBuilder<?> blockEntityBuilder;
	private DropType dropType = DropType.SELF;
	private @Nullable ItemLike dropItem;
	private @Nullable Function<Block, LootTable.Builder> dropCustomizer;

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

	public BlockBuilder use(EngineBlock.UseCallback cb) {
		blockCallbacks.put(EngineBlock.CallbackType.USE, cb);
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

		EngineBlock block;
		if (blockEntityBuilder != null) {
			block = new EngineEntityBlock(this.properties,
				() -> (BlockEntityType<?>) blockEntityBuilder.getOrCreate(),
				blockEntityBuilder.hasTick());
		} else if (!blockCallbacks.isEmpty() || visibilityCondition != null) {
			block = new EngineBlock(this.properties);
		} else {
			Block raw = blockFactory.apply(this.properties);
			if (raw instanceof EngineBlock eb) {
				applyCallbacks(eb);
			}
			return raw;
		}

		block.visibilityCondition(visibilityCondition);
		applyCallbacks(block);
		return block;
	}

	@SuppressWarnings("unchecked")
	private void applyCallbacks(EngineBlock block) {
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
				case USE -> block.use((EngineBlock.UseCallback) cb);
			}
		});
	}

	<C> BlockBuilder callback(EngineItem.CallbackType type, C cb) {
		itemCallbacks.put(type, cb);
		return this;
	}

	public BlockBuilder ghost() {
		this.visibilityCondition = (player, state) -> player.isHolding(state.getBlock().asItem());
		return this;
	}

	public BlockBuilder ghost(BiPredicate<Player, BlockState> visibilityCondition) {
		this.visibilityCondition = visibilityCondition;
		return this;
	}

	public BlockBuilder tag(BlockTagBuilder tagBuilder) {
		tagBuilder.add(ResourceKey.create(Registries.BLOCK, id));
		tags.add(tagBuilder);
		return this;
	}

	public BlockBuilder tag(Identifier tagId) {
		return tag(BlockTagBuilder.create(tagId));
	}

	public List<BlockTagBuilder> getTags() {
		return tags;
	}

	public BlockBuilder stairs(BlockState base) {
		this.blockFactory = props -> new StairBlock(base, props);
		return this;
	}

	public BlockBuilder slab() {
		this.blockFactory = SlabBlock::new;
		return this;
	}

	public BlockBuilder wall() {
		this.blockFactory = WallBlock::new;
		return this;
	}

	public BlockBuilder fence() {
		this.blockFactory = FenceBlock::new;
		return this;
	}

	public BlockBuilder fenceGate(WoodType type) {
		this.blockFactory = props -> new FenceGateBlock(type, props);
		return this;
	}

	public BlockBuilder door(BlockSetType type) {
		this.blockFactory = props -> new DoorBlock(type, props);
		return this;
	}

	public BlockBuilder trapdoor(BlockSetType type) {
		this.blockFactory = props -> new TrapDoorBlock(type, props);
		return this;
	}

	public BlockBuilder pressurePlate(BlockSetType type) {
		this.blockFactory = props -> new PressurePlateBlock(type, props);
		return this;
	}

	public BlockBuilder button(BlockSetType type, int ticksToStayPressed) {
		this.blockFactory = props -> new ButtonBlock(type, ticksToStayPressed, props);
		return this;
	}

	public BlockBuilder pillar() {
		this.blockFactory = RotatedPillarBlock::new;
		return this;
	}

	public BlockBuilder glass() {
		this.blockFactory = TransparentBlock::new;
		return this;
	}

	public BlockBuilder bars() {
		this.blockFactory = IronBarsBlock::new;
		return this;
	}

	public BlockBuilder carpet() {
		this.blockFactory = CarpetBlock::new;
		return this;
	}

	public BlockBuilder chain() {
		this.blockFactory = ChainBlock::new;
		return this;
	}

	public BlockBuilder lantern() {
		this.blockFactory = LanternBlock::new;
		return this;
	}

	public BlockBuilder ladder() {
		this.blockFactory = LadderBlock::new;
		return this;
	}

	public BlockBuilder endRod() {
		this.blockFactory = EndRodBlock::new;
		return this;
	}

	public BlockBuilder lever() {
		this.blockFactory = LeverBlock::new;
		return this;
	}

	public BlockBuilder observer() {
		this.blockFactory = ObserverBlock::new;
		return this;
	}

	public BlockBuilder dispenser() {
		this.blockFactory = DispenserBlock::new;
		return this;
	}

	public BlockBuilder dropper() {
		this.blockFactory = DropperBlock::new;
		return this;
	}

	public BlockBuilder hopper() {
		this.blockFactory = HopperBlock::new;
		return this;
	}

	public BlockBuilder anvil() {
		this.blockFactory = AnvilBlock::new;
		return this;
	}

	public BlockBuilder grindstone() {
		this.blockFactory = GrindstoneBlock::new;
		return this;
	}

	public BlockBuilder composter() {
		this.blockFactory = ComposterBlock::new;
		return this;
	}

	public BlockBuilder redstoneLamp() {
		this.blockFactory = RedstoneLampBlock::new;
		return this;
	}

	public BlockBuilder daylightDetector() {
		this.blockFactory = DaylightDetectorBlock::new;
		return this;
	}

	public BlockBuilder beacon() {
		this.blockFactory = BeaconBlock::new;
		return this;
	}

	public BlockBuilder lightningRod() {
		this.blockFactory = LightningRodBlock::new;
		return this;
	}

	public BlockBuilder generateData(boolean generate) {
		this.generateData = generate;
		return this;
	}

	public BlockBuilder blockEntity(BlockEntityBuilder<?> beBuilder) {
		this.blockEntityBuilder = beBuilder;
		beBuilder.validBlock(this);
		return this;
	}

	public @Nullable BlockEntityBuilder<?> getBlockEntityBuilder() {
		return blockEntityBuilder;
	}

	public BlockBuilder dropsSelf() {
		this.dropType = DropType.SELF;
		return this;
	}

	public BlockBuilder drops(ItemLike drop) {
		this.dropType = DropType.ITEM;
		this.dropItem = drop;
		return this;
	}

	public BlockBuilder dropsNothing() {
		this.dropType = DropType.NOTHING;
		return this;
	}

	public BlockBuilder dropsCustom(Function<Block, LootTable.Builder> customizer) {
		this.dropType = DropType.CUSTOM;
		this.dropCustomizer = customizer;
		return this;
	}

	public DropType getDropType() {
		return dropType;
	}

	public @Nullable ItemLike getDropItem() {
		return dropItem;
	}

	public @Nullable Function<Block, LootTable.Builder> getDropCustomizer() {
		return dropCustomizer;
	}

	public enum DropType {
		SELF,
		ITEM,
		NOTHING,
		CUSTOM
	}
}
