package de.luckymcdev.foundryengine.server.data.providers.loot;

import de.luckymcdev.foundryengine.common.builder.block.BlockBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import java.util.List;
import java.util.function.BiConsumer;

public class EngineLootTableSubProvider implements LootTableSubProvider {

	private final List<BlockBuilder> blockBuilders;

	public EngineLootTableSubProvider(HolderLookup.Provider registries, List<BlockBuilder> blockBuilders) {
		this.blockBuilders = blockBuilders;
	}

	private static LootTable.Builder createSingleItemTable(ItemLike drop) {
		return LootTable.lootTable()
			.withPool(LootPool.lootPool()
				.setRolls(ConstantValue.exactly(1.0F))
				.add(LootItem.lootTableItem(drop)));
	}

	@Override
	public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
		for (BlockBuilder builder : blockBuilders) {
			if (!builder.shouldGenerateData()) {
				continue;
			}
			Block block = builder.get();
			var lootTableKey = block.getLootTable().orElse(null);
			if (lootTableKey == null) {
				continue;
			}
			switch (builder.getDropType()) {
				case SELF -> output.accept(lootTableKey, createSingleItemTable(block));
				case ITEM -> {
					ItemLike drop = builder.getDropItem();
					if (drop != null) {
						output.accept(lootTableKey, createSingleItemTable(drop));
					}
				}
				case CUSTOM -> {
					var customizer = builder.getDropCustomizer();
					if (customizer != null) {
						output.accept(lootTableKey, customizer.apply(block));
					}
				}
			}
		}
	}
}
