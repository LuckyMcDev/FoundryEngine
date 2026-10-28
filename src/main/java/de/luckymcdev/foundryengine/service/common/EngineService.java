package de.luckymcdev.foundryengine.service.common;

import java.util.concurrent.CompletableFuture;

public interface EngineService {

	String name();

	boolean isAvailable();

	CompletableFuture<EngineServiceResult> execute(String... args);
}