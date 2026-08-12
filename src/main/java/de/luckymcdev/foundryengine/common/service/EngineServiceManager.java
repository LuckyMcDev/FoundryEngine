package de.luckymcdev.foundryengine.common.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EngineServiceManager {
	private final Map<Class<? extends EngineService>, EngineService> services = new HashMap<>();

	public EngineServiceManager() {
		register(GitService.class, new GitService.Default());
		// potential Pakku Service or other pack management software.
	}

	public <T extends EngineService> void register(Class<T> type, T service) {
		if (service.isAvailable()) {
			services.put(type, service);
		}
	}

	@SuppressWarnings("unchecked")
	public <T extends EngineService> Optional<T> get(Class<T> type) {
		return Optional.ofNullable((T) services.get(type));
	}

	public boolean has(Class<? extends EngineService> type) {
		return services.containsKey(type);
	}
}