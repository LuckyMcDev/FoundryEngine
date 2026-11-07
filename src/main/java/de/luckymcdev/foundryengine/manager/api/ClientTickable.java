package de.luckymcdev.foundryengine.manager.api;

import net.neoforged.neoforge.client.event.ClientTickEvent;

public interface ClientTickable extends EngineManager {
	void onClientTick(ClientTickEvent.Pre event);
}