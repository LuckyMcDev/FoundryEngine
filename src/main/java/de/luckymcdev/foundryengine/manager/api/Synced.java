package de.luckymcdev.foundryengine.manager.api;

/**
 * Broadcasts persisted state to all clients.
 */
public interface Synced extends EngineManager {
	void syncToAll();
}