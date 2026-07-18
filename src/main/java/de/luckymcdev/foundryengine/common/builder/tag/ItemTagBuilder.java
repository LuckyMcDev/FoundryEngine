package de.luckymcdev.foundryengine.common.builder.tag;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class ItemTagBuilder extends TagBuilder<Item> {
	protected ItemTagBuilder(Identifier id) {
		super(id, Registries.ITEM);
	}

	public static ItemTagBuilder create(Identifier id) {
		return new ItemTagBuilder(id);
	}

	@Override
	public ItemTagBuilder add(ResourceKey<Item> key) {
		return (ItemTagBuilder) super.add(key);
	}

	@Override
	public ItemTagBuilder addOptional(ResourceKey<Item> key) {
		return (ItemTagBuilder) super.addOptional(key);
	}

	@Override
	public ItemTagBuilder addTag(TagKey<Item> tag) {
		return (ItemTagBuilder) super.addTag(tag);
	}

	@Override
	public ItemTagBuilder addOptionalTag(TagKey<Item> tag) {
		return (ItemTagBuilder) super.addOptionalTag(tag);
	}

	@Override
	public ItemTagBuilder remove(ResourceKey<Item> key) {
		return (ItemTagBuilder) super.remove(key);
	}

	@Override
	public ItemTagBuilder removeTag(TagKey<Item> tag) {
		return (ItemTagBuilder) super.removeTag(tag);
	}

	@Override
	public ItemTagBuilder replace() {
		return (ItemTagBuilder) super.replace();
	}

	@Override
	public ItemTagBuilder replace(boolean replace) {
		return (ItemTagBuilder) super.replace(replace);
	}

	public ItemTagBuilder add(Item item) {
		return add(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow());
	}

	public ItemTagBuilder addOptional(Item item) {
		return addOptional(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow());
	}

	public ItemTagBuilder remove(Item item) {
		return remove(BuiltInRegistries.ITEM.getResourceKey(item).orElseThrow());
	}
}
