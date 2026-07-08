package de.luckymcdev.foundryengine.common.game.stage.table;

import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StageTable {
	private final Identifier name;
	private final List<WeightedEntry> entries = new ArrayList<>();
	private int totalWeight;

	public StageTable(Identifier name) {
		this.name = name;
	}

	public Identifier getName() {
		return name;
	}

	public StageEntry getRandomEntry(RandomSource random) {
		if (entries.isEmpty() || totalWeight <= 0) {
			return null;
		}
		int roll = random.nextInt(totalWeight);
		int cumulative = 0;
		for (var entry : entries) {
			cumulative += entry.weight;
			if (roll < cumulative) {
				return entry.entry;
			}
		}
		return entries.getLast().entry;
	}

	public int getTotalWeight() {
		return totalWeight;
	}

	public List<WeightedEntry> getEntries() {
		return Collections.unmodifiableList(entries);
	}

	public boolean canPlayerUse(Player player) {
		for (var entry : entries) {
			if (entry.entry.canPlayerObtain(player)) {
				return true;
			}
		}
		return false;
	}

	public StageEntry createEntry(Identifier stage, int weight) {
		var entry = new StageEntry(stage, weight);
		entries.add(new WeightedEntry(entry, weight));
		totalWeight += weight;
		return entry;
	}

	public void removeEntry(StageEntry entry) {
		entries.removeIf(e -> e.entry == entry);
		recalculateWeight();
	}

	private void recalculateWeight() {
		totalWeight = entries.stream().mapToInt(e -> e.weight).sum();
	}

	public record WeightedEntry(StageEntry entry, int weight) {
	}
}
