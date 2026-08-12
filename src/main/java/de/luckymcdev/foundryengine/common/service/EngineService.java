package de.luckymcdev.foundryengine.common.service;

import java.util.concurrent.CompletableFuture;

public interface EngineService {

	String name();

	boolean isAvailable();

	CompletableFuture<EngineServiceResult> execute(String... args);
}