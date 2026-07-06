package de.luckymcdev.foundryengine.common.builder.tag;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Use {@link BlockTagBuilder} and {@link ItemTagBuilder} or this with a Wildcard if you know what you're doing
 *
 * @param <T> the Type
 */
public class TagBuilder<T> extends AbstractBuilder<TagKey<T>> {
	private final ResourceKey<? extends Registry<T>> registry;
	private final List<TagEntry> entries = new ArrayList<>();
	private final List<TagEntry> removeEntries = new ArrayList<>();
	private boolean replace = false;

	protected TagBuilder(Identifier id, ResourceKey<? extends Registry<T>> registry) {
		super(id);
		this.registry = registry;
	}

	public static <T> TagBuilder<T> create(Identifier id, ResourceKey<? extends Registry<T>> registry) {
		return new TagBuilder<>(id, registry);
	}

	public TagBuilder<T> add(ResourceKey<T> key) {
		entries.add(TagEntry.element(key.identifier()));
		return this;
	}

	public TagBuilder<T> add(Holder<T> holder) {
		return add(holder.unwrapKey().orElseThrow());
	}

	public TagBuilder<T> addOptional(ResourceKey<T> key) {
		entries.add(TagEntry.optionalElement(key.identifier()));
		return this;
	}

	public TagBuilder<T> addOptional(Holder<T> holder) {
		return addOptional(holder.unwrapKey().orElseThrow());
	}

	public TagBuilder<T> addTag(TagKey<T> tag) {
		entries.add(TagEntry.tag(tag.location()));
		return this;
	}

	public TagBuilder<T> addOptionalTag(TagKey<T> tag) {
		entries.add(TagEntry.optionalTag(tag.location()));
		return this;
	}

	public TagBuilder<T> remove(ResourceKey<T> key) {
		removeEntries.add(TagEntry.element(key.identifier()));
		return this;
	}

	public TagBuilder<T> remove(Holder<T> holder) {
		return remove(holder.unwrapKey().orElseThrow());
	}

	public TagBuilder<T> removeTag(TagKey<T> tag) {
		removeEntries.add(TagEntry.tag(tag.location()));
		return this;
	}

	public TagBuilder<T> replace() {
		this.replace = true;
		return this;
	}

	public TagBuilder<T> replace(boolean replace) {
		this.replace = replace;
		return this;
	}

	public ResourceKey<? extends Registry<T>> registry() {
		return registry;
	}

	public void applyTo(net.minecraft.tags.TagBuilder builder) {
		entries.forEach(builder::add);
		removeEntries.forEach(builder::remove);
		builder.setReplace(replace);
	}

	@Override
	protected TagKey<T> build() {
		return TagKey.create(registry, id);
	}
}
