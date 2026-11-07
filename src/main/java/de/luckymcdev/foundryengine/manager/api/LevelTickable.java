package de.luckymcdev.foundryengine.manager.api;

import net.neoforged.neoforge.event.tick.LevelTickEvent;

public interface LevelTickable extends EngineManager {
	void onLevelTick(LevelTickEvent.Post event);
}