package io.github.luckymcdev.foundryengine.common.registry.builder;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * An Item Builder, which allows for Item registering in a Builder format.
 * Inspired by KubeJs
 */
public class ItemBuilder extends BuilderBase<Item> {
    private Item.Properties properties;
    private Function<Item.Properties, Item> factory;

    public ItemBuilder(Identifier id) {
        super(id);
        this.registryKey = Registries.ITEM;
        this.properties = new Item.Properties();
        this.factory = Item::new;
    }

    /**
     * Set a custom factory if you want to use a specific Item subclass
     * (e.g. a custom class).
     */
    public ItemBuilder factory(Function<Item.Properties, Item> factory) {
        this.factory = factory;
        return this;
    }

    /**
     * Directly modify the Minecraft Item.Properties.
     */
    public ItemBuilder properties(UnaryOperator<Item.Properties> propertiesAction) {
        this.properties = propertiesAction.apply(this.properties);
        return this;
    }

    /**
     * Helper to set the maximum stack size.
     */
    public ItemBuilder stacksTo(int count) {
        this.properties = this.properties.stacksTo(count);
        return this;
    }

    /**
     * Helper to make the item fire-resistant.
     */
    public ItemBuilder fireResistant() {
        this.properties = this.properties.fireResistant();
        return this;
    }

    /**
     * Add a component to the item.
     */
    public <T> ItemBuilder component(DataComponentType<T> type, T value) {
        this.properties = this.properties.component(type, value);
        return this;
    }

    public Item register(RegisterEvent.RegisterHelper<Item> helper) {
        Item item = build();
        helper.register(this.id, item);
        this.object = item;
        return item;
    }

    @Override
    public Item build() {
        this.properties.setId(ResourceKey.create(Registries.ITEM, id));
        return factory.apply(this.properties);
    }
}