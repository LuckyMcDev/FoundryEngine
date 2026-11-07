package de.luckymcdev.foundryengine.manager.api;

public interface ServerLifecycle extends EngineManager {
	default void onServerStarting() {}
	default void onServerStopping() {}
}