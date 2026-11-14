package de.luckymcdev.foundryengine.manager.api;

import de.luckymcdev.foundryengine.savedata.common.SavedDataManager;
import net.minecraft.nbt.CompoundTag;

public interface Persistable extends EngineManager {
	String saveSection();

	default SavedDataManager.Scope saveScope() {
		return SavedDataManager.Scope.WORLD;
	}

	CompoundTag saveData();

	void loadData(CompoundTag data);
}