package de.luckymcdev.foundryengine.common.cutscene;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.savedata.SavedDataManager;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CutsceneManager {
	public static final String SAVE_SECTION = "cutscenes";
	private final SavedDataManager savedDataManager;
	private final Map<ResourceKey<Level>, List<Cutscene>> cutscenesByDimension = new HashMap<>();

	public CutsceneManager(SavedDataManager savedDataManager) {
		this.savedDataManager = savedDataManager;
	}

	public boolean isLoaded(ResourceKey<Level> dimension) {
		return cutscenesByDimension.containsKey(dimension);
	}

	public List<Cutscene> getCutscenes(ResourceKey<Level> dimension) {
		return cutscenesByDimension.getOrDefault(dimension, Collections.emptyList());
	}

	public void replaceAll(ResourceKey<Level> dimension, List<Cutscene> cutscenes) {
		cutscenesByDimension.put(dimension, new ArrayList<>(cutscenes));
	}

	public @Nullable Cutscene find(ResourceKey<Level> dimension, String name) {
		for (Cutscene c : getCutscenes(dimension)) {
			if (c.getName().equals(name)) {
				return c;
			}
		}
		return null;
	}

	public Collection<Identifier> getSuggestions(ServerLevel level) {
		if (!isLoaded(level.dimension())) {
			var dim = level.dimension();
			var list = getCutscenes(dim);
			replaceAll(dim, list);
		}
		ArrayList<Identifier> out = new ArrayList<>();
		for (Cutscene c : getCutscenes(level.dimension())) {
			out.add(Common.id(c.getName()));
		}
		return out;
	}

	public void applyFullNbt(CompoundTag tag) {
		var list = new ArrayList<Cutscene>();
		var nbtList = tag.getListOrEmpty("CutsceneList");
		for (int i = 0; i < nbtList.size(); i++) {
			list.add(Cutscene.fromNbt(nbtList.getCompoundOrEmpty(i)));
		}
		replaceAll(Level.OVERWORLD, list);
	}

	public void fromNbt(CompoundTag tag) {
		cutscenesByDimension.clear();
		for (var dimKey : tag.keySet()) {
			var dimTag = tag.getCompoundOrEmpty(dimKey);
			var dim = ResourceKey.create(Registries.DIMENSION, Identifier.parse(dimKey));
			var list = new ArrayList<Cutscene>();
			var nbtList = dimTag.getListOrEmpty("CutsceneList");
			for (int i = 0; i < nbtList.size(); i++) {
				list.add(Cutscene.fromNbt(nbtList.getCompoundOrEmpty(i)));
			}
			cutscenesByDimension.put(dim, list);
		}
	}

	public CompoundTag toNbt() {
		var tag = new CompoundTag();
		for (var entry : cutscenesByDimension.entrySet()) {
			var dimTag = new CompoundTag();
			var list = new ListTag();
			for (Cutscene cutscene : entry.getValue()) {
				list.add(cutscene.toNbt());
			}
			dimTag.put("CutsceneList", list);
			tag.put(entry.getKey().identifier().toString(), dimTag);
		}
		return tag;
	}

	public boolean add(ResourceKey<Level> dimension, Cutscene cutscene) {
		var list = new ArrayList<>(getCutscenes(dimension));
		if (list.stream().anyMatch(c -> c.getName().equals(cutscene.getName()))) {
			return false;
		}
		list.add(cutscene);
		replaceAll(dimension, list);
		save();
		return true;
	}

	public boolean remove(ResourceKey<Level> dimension, String name) {
		var list = new ArrayList<>(getCutscenes(dimension));
		boolean removed = list.removeIf(c -> c.getName().equals(name));
		if (!removed) {
			return false;
		}
		replaceAll(dimension, list);
		save();
		return true;
	}

	public void clear(ResourceKey<Level> dimension) {
		replaceAll(dimension, List.of());
		save();
	}

	public void save() {
		savedDataManager.setSection(SAVE_SECTION, toNbt());
	}

	public void load() {
		fromNbt(savedDataManager.getSection(SAVE_SECTION));
	}

	public void syncToPlayer(ServerPlayer player) {
		savedDataManager.syncToPlayer(player);
	}

	public void syncToAll() {
		savedDataManager.syncToAll();
	}
}
