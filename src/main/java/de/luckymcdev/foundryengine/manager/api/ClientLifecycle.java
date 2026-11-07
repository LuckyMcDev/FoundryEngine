package de.luckymcdev.foundryengine.manager.api;

public interface ClientLifecycle extends EngineManager {
	default void onClientStarting() {}
	default void onClientStopping() {}
}