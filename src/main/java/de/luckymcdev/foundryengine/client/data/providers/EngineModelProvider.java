package de.luckymcdev.foundryengine.client.data.providers;

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import de.luckymcdev.foundryengine.common.builder.item.ItemBuilder;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.BlockModelDefinitionGenerator;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelDispatcher;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class EngineModelProvider implements DataProvider {
	private final String namespace;
	private final List<BlockBuilder> blockBuilders;
	private final List<ItemBuilder> itemBuilders;
	private final PackOutput.PathProvider blockStatePathProvider;
	private final PackOutput.PathProvider itemInfoPathProvider;
	private final PackOutput.PathProvider modelPathProvider;

	public EngineModelProvider(PackOutput output, String namespace, List<BlockBuilder> blockBuilders, List<ItemBuilder> itemBuilders) {
		this.namespace = namespace;
		this.blockBuilders = blockBuilders;
		this.itemBuilders = itemBuilders;
		this.blockStatePathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "blockstates");
		this.itemInfoPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "items");
		this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
	}

	@Override
	public CompletableFuture<?> run(CachedOutput cachedOutput) {
		BlockStateCollector blockStates = new BlockStateCollector();
		ItemInfoCollector itemInfos = new ItemInfoCollector(blockStates);
		SimpleModelCollector simpleModels = new SimpleModelCollector();

		BlockModelGenerators blockModels = new BlockModelGenerators(blockStates, itemInfos, simpleModels);
		ItemModelGenerators itemModels = new ItemModelGenerators(itemInfos, simpleModels);

		for (BlockBuilder builder : blockBuilders) {
			blockModels.createTrivialCube(builder.get());
		}

		for (ItemBuilder builder : itemBuilders) {
			Item item = builder.get();
			if (item instanceof BlockItem) {
				continue;
			}
			itemModels.generateFlatItem(item, ModelTemplates.FLAT_ITEM);
		}

		return CompletableFuture.allOf(
			blockStates.save(cachedOutput, blockStatePathProvider),
			simpleModels.save(cachedOutput, modelPathProvider),
			itemInfos.save(cachedOutput, itemInfoPathProvider)
		);
	}

	@Override
	public String getName() {
		return "Model Definitions - " + namespace;
	}

	private static final class BlockStateCollector implements Consumer<BlockModelDefinitionGenerator> {
		private final Map<Block, BlockModelDefinitionGenerator> generators = new HashMap<>();

		@Override
		public void accept(BlockModelDefinitionGenerator generator) {
			Block block = generator.block();
			BlockModelDefinitionGenerator previous = generators.put(block, generator);
			if (previous != null) {
				throw new IllegalStateException("Duplicate blockstate generator for " + block);
			}
		}

		public CompletableFuture<?> save(CachedOutput cachedOutput, PackOutput.PathProvider pathProvider) {
			Map<Block, BlockStateModelDispatcher> serialized = new HashMap<>();
			for (var entry : generators.entrySet()) {
				serialized.put(entry.getKey(), entry.getValue().create());
			}
			return DataProvider.saveAll(
				cachedOutput,
				BlockStateModelDispatcher.CODEC,
				block -> pathProvider.json(BuiltInRegistries.BLOCK.createIntrusiveHolder(block).key().identifier()),
				serialized
			);
		}

		public boolean contains(Block block) {
			return generators.containsKey(block);
		}
	}

	private static final class ItemInfoCollector implements ItemModelOutput {
		private final Map<Item, ClientItem> itemInfos = new HashMap<>();
		private final Map<Item, Item> copies = new HashMap<>();
		private final BlockStateCollector blockStates;

		private ItemInfoCollector(BlockStateCollector blockStates) {
			this.blockStates = blockStates;
		}

		@Override
		public void accept(Item item, ItemModel.Unbaked model, ClientItem.Properties properties) {
			registerItemInfo(item, new ClientItem(model, properties));
		}

		@Override
		public void copy(Item item, Item copy) {
			copies.put(copy, item);
		}

		private void registerItemInfo(Item item, ClientItem clientItem) {
			ClientItem previous = itemInfos.put(item, clientItem);
			if (previous != null) {
				throw new IllegalStateException("Duplicate item model info for " + item);
			}
		}

		public CompletableFuture<?> save(CachedOutput cachedOutput, PackOutput.PathProvider pathProvider) {
			Map<Item, ClientItem> resolved = new HashMap<>(itemInfos);

			for (Block block : blockStates.generators.keySet()) {
				Item item = block.asItem();
				if (item instanceof BlockItem && !resolved.containsKey(item)) {
					resolved.put(
						item,
						new ClientItem(
							ItemModelUtils.plainModel(ModelLocationUtils.getModelLocation(block)),
							ClientItem.Properties.DEFAULT
						)
					);
				}
			}

			for (var entry : copies.entrySet()) {
				Item target = entry.getKey();
				Item source = entry.getValue();
				ClientItem clientItem = resolved.get(source);
				if (clientItem == null) {
					throw new IllegalStateException("Missing copied item model source " + source + " for " + target);
				}
				resolved.put(target, clientItem);
			}

			return DataProvider.saveAll(
				cachedOutput,
				ClientItem.CODEC,
				item -> pathProvider.json(BuiltInRegistries.ITEM.createIntrusiveHolder(item).key().identifier()),
				resolved
			);
		}
	}

	private static final class SimpleModelCollector implements BiConsumer<Identifier, ModelInstance> {
		private final Map<Identifier, ModelInstance> models = new HashMap<>();

		@Override
		public void accept(Identifier id, ModelInstance model) {
			ModelInstance previous = models.put(id, model);
			if (previous != null) {
				throw new IllegalStateException("Duplicate model definition for " + id);
			}
		}

		public CompletableFuture<?> save(CachedOutput cachedOutput, PackOutput.PathProvider pathProvider) {
			return DataProvider.saveAll(
				cachedOutput,
				ModelInstance::get,
				id -> pathProvider.json(id),
				models
			);
		}
	}
}
