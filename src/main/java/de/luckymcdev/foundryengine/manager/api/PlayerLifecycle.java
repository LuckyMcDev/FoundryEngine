package de.luckymcdev.foundryengine.manager.api;

import net.minecraft.server.level.ServerPlayer;

public interface PlayerLifecycle extends EngineManager {
	default void onPlayerJoin(ServerPlayer player) {}
	default void onPlayerDisconnect(ServerPlayer player) {}
}