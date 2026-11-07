package de.luckymcdev.foundryengine.manager.api;

import net.neoforged.neoforge.event.tick.ServerTickEvent;

public interface ServerTickable extends EngineManager {
	void onServerTick(ServerTickEvent.Post event);
}