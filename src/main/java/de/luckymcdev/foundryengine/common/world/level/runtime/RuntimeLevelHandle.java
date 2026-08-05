package de.luckymcdev.foundryengine.common.world.level.runtime;

import de.luckymcdev.foundryengine.common.world.level.EngineLevels;
import de.luckymcdev.foundryengine.interfaces.level.EngineLevelAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public class RuntimeLevelHandle {
	private final EngineLevels engineLevels;
	private final ServerLevel level;

	public RuntimeLevelHandle(EngineLevels engineLevels, ServerLevel level) {
		this.engineLevels = engineLevels;
		this.level = level;
	}

	public void setTickWhenEmpty(boolean tickWhenEmpty) {
		((EngineLevelAccess) this.level).engine$setTickWhenEmpty(tickWhenEmpty);
	}

	/**
	 * Deletes the level, including all stored files
	 */
	public void delete() {
		this.engineLevels.enqueueLevelDeletion(this.level);
	}

	/**
	 * Unloads the level. It only deletes the files if the level is temporary.
	 */
	public void unload() {
		if (this.level instanceof RuntimeLevel runtimeLevel && runtimeLevel.style == RuntimeLevel.Style.TEMPORARY) {
			this.engineLevels.enqueueLevelDeletion(this.level);
		} else {
			this.engineLevels.enqueueLevelUnloading(this.level);
		}
	}

	public ServerLevel asLevel() {
		return this.level;
	}

	public ResourceKey<Level> getRegistryKey() {
		return this.level.dimension();
	}
}
