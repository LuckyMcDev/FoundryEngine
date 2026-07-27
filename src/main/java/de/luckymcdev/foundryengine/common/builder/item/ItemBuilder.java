package de.luckymcdev.foundryengine.common.builder.item;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import de.luckymcdev.foundryengine.common.builder.tag.ItemTagBuilder;
import de.luckymcdev.foundryengine.common.world.item.EngineItem;
import de.luckymcdev.foundryengine.common.wrapper.DataComponentWrapper;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class ItemBuilder extends AbstractBuilder<Item> {
	private final Map<EngineItem.CallbackType, Object> callbacks = new EnumMap<>(EngineItem.CallbackType.class);
	private final List<ItemTagBuilder> tags = new ArrayList<>();
	private Item.Properties properties;
	private Function<Item.Properties, Item> factory;

	public ItemBuilder(Identifier id) {
		super(id);
		this.properties = new Item.Properties();
		this.factory = EngineItem::new;
	}

	public static ItemBuilder create(Identifier id) {
		return new ItemBuilder(id);
	}

	public ItemBuilder factory(Function<Item.Properties, Item> factory) {
		this.factory = factory;
		return this;
	}

	public ItemBuilder properties(UnaryOperator<Item.Properties> propertiesAction) {
		this.properties = propertiesAction.apply(this.properties);
		return this;
	}

	public ItemBuilder stacksTo(int count) {
		this.properties = this.properties.stacksTo(count);
		return this;
	}

	public ItemBuilder fireResistant() {
		this.properties = this.properties.fireResistant();
		return this;
	}

	@ApiStatus.Internal
	public <C> void callback(EngineItem.CallbackType type, C cb) {
		callbacks.put(type, cb);
	}

	public ItemBuilder onUseTick(EngineItem.OnUseTickCallback cb) {
		callbacks.put(EngineItem.CallbackType.ON_USE_TICK, cb);
		return this;
	}

	public ItemBuilder useOn(EngineItem.UseOnCallback cb) {
		callbacks.put(EngineItem.CallbackType.USE_ON, cb);
		return this;
	}

	public ItemBuilder use(EngineItem.UseCallback cb) {
		callbacks.put(EngineItem.CallbackType.USE, cb);
		return this;
	}

	public ItemBuilder finishUsingItem(EngineItem.FinishUsingItemCallback cb) {
		callbacks.put(EngineItem.CallbackType.FINISH_USING_ITEM, cb);
		return this;
	}

	public ItemBuilder hurtEnemy(EngineItem.HurtEnemyCallback cb) {
		callbacks.put(EngineItem.CallbackType.HURT_ENEMY, cb);
		return this;
	}

	public ItemBuilder postHurtEnemy(EngineItem.PostHurtEnemyCallback cb) {
		callbacks.put(EngineItem.CallbackType.POST_HURT_ENEMY, cb);
		return this;
	}

	public ItemBuilder inventoryTick(EngineItem.InventoryTickCallback cb) {
		callbacks.put(EngineItem.CallbackType.INVENTORY_TICK, cb);
		return this;
	}

	public ItemBuilder onCraftedPostProcess(EngineItem.OnCraftedPostProcessCallback cb) {
		callbacks.put(EngineItem.CallbackType.ON_CRAFTED_POST_PROCESS, cb);
		return this;
	}

	public ItemBuilder releaseUsing(EngineItem.ReleaseUsingCallback cb) {
		callbacks.put(EngineItem.CallbackType.RELEASE_USING, cb);
		return this;
	}

	public <T> ItemBuilder component(DataComponentType<T> type, T value) {
		this.properties = this.properties.component(type, value);
		return this;
	}

	@SuppressWarnings("unchecked")
	public <T> ItemBuilder component(String type, T value) {
		DataComponentType<T> componentType = (DataComponentType<T>) DataComponentWrapper.resolve(type);
		this.properties = this.properties.component(componentType, value);
		return this;
	}

	public Item register(RegisterEvent.RegisterHelper<Item> helper) {
		Item item = build();
		helper.register(id, item);
		setObject(item);
		return item;
	}

	public Item build() {
		this.properties.setId(ResourceKey.create(Registries.ITEM, id));

		if (!callbacks.isEmpty()) {
			EngineItem engineItem = new EngineItem(this.properties);
			callbacks.forEach((type, cb) -> {
				switch (type) {
					case ON_USE_TICK -> engineItem.onUseTick((EngineItem.OnUseTickCallback) cb);
					case USE_ON -> engineItem.useOn((EngineItem.UseOnCallback) cb);
					case USE -> engineItem.use((EngineItem.UseCallback) cb);
					case FINISH_USING_ITEM -> engineItem.finishUsingItem((EngineItem.FinishUsingItemCallback) cb);
					case HURT_ENEMY -> engineItem.hurtEnemy((EngineItem.HurtEnemyCallback) cb);
					case POST_HURT_ENEMY -> engineItem.postHurtEnemy((EngineItem.PostHurtEnemyCallback) cb);
					case INVENTORY_TICK -> engineItem.inventoryTick((EngineItem.InventoryTickCallback) cb);
					case ON_CRAFTED_POST_PROCESS -> engineItem.onCraftedPostProcess((EngineItem.OnCraftedPostProcessCallback) cb);
					case RELEASE_USING -> engineItem.releaseUsing((EngineItem.ReleaseUsingCallback) cb);
				}
			});
			return engineItem;
		}

		return factory.apply(this.properties);
	}

	public ItemBuilder generateData(boolean generate) {
		this.generateData = generate;
		return this;
	}

	public ItemBuilder tag(ItemTagBuilder tagBuilder) {
		tagBuilder.add(ResourceKey.create(Registries.ITEM, id));
		tags.add(tagBuilder);
		return this;
	}

	public ItemBuilder tag(Identifier tagId) {
		return tag(ItemTagBuilder.create(tagId));
	}

	public List<ItemTagBuilder> getTags() {
		return tags;
	}
}
