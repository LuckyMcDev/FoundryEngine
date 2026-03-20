package io.github.luckymcdev.foundryengine.common.registry.builder.item;

import io.github.luckymcdev.foundryengine.api.builder.item.ItemBuilder;
import io.github.luckymcdev.foundryengine.common.registry.builder.BuilderBaseImpl;
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
public class ItemBuilderImpl extends BuilderBaseImpl<Item> implements ItemBuilder {
    private Item.Properties properties;
    private Function<Item.Properties, Item> factory;

    public ItemBuilderImpl(Identifier id) {
        super(id);
        this.registryKey = Registries.ITEM;
        this.properties = new Item.Properties();
        this.factory = Item::new;
    }

    @Override
    public ItemBuilder factory(Function<Item.Properties, Item> factory) {
        this.factory = factory;
        return this;
    }

    @Override
    public ItemBuilder properties(UnaryOperator<Item.Properties> propertiesAction) {
        this.properties = propertiesAction.apply(this.properties);
        return this;
    }

    @Override
    public ItemBuilder stacksTo(int count) {
        this.properties = this.properties.stacksTo(count);
        return this;
    }

    @Override
    public ItemBuilder fireResistant() {
        this.properties = this.properties.fireResistant();
        return this;
    }

    @Override
    public <T> ItemBuilder component(DataComponentType<T> type, T value) {
        this.properties = this.properties.component(type, value);
        return this;
    }

    @Override
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