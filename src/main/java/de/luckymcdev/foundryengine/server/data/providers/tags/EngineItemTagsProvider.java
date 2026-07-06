package de.luckymcdev.foundryengine.server.data.providers.tags;

import de.luckymcdev.foundryengine.common.builder.tag.TagBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EngineItemTagsProvider extends ItemTagsProvider {
	private final String namespace;
	private final Collection<TagBuilder<?>> tagBuilders;

	public EngineItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String namespace) {
		this(output, lookupProvider, namespace, List.of());
	}

	public EngineItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, String namespace, Collection<TagBuilder<?>> tagBuilders) {
		super(output, lookupProvider, namespace);
		this.namespace = namespace;
		this.tagBuilders = tagBuilders;
	}

	@Override
	protected void addTags(HolderLookup.Provider registries) {
		for (var tagBuilder : tagBuilders) {
			tagBuilder.applyTo(getOrCreateRawBuilder((TagKey<Item>) tagBuilder.getOrCreate()));
		}
	}
}
