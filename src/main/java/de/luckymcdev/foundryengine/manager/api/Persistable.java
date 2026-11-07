package de.luckymcdev.foundryengine.manager.api;

import net.minecraft.nbt.CompoundTag;
import de.luckymcdev.foundryengine.savedata.common.SavedDataManager;

public interface Persistable extends EngineManager {
	String saveSection();
	default SavedDataManager.Scope saveScope() {
		return SavedDataManager.Scope.WORLD;
	}
	CompoundTag saveData();
	void loadData(CompoundTag data);
}