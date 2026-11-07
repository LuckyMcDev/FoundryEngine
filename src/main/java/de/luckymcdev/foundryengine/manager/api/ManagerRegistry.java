package de.luckymcdev.foundryengine.manager.api;

import com.mojang.logging.LogUtils;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Registry for {@link EngineManager}s keyed by a stable string key.
 * Each side (common, client, server) owns its own instance.
 */
public final class ManagerRegistry {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, EngineManager> managers = new LinkedHashMap<>();

	/**
	 * Registers under an explicit key. Throws if the key is already taken.
	 */
	public <T extends EngineManager> T register(String key, T manager) {
		if (managers.putIfAbsent(key, manager) != null) {
			throw new IllegalStateException("Manager already registered under key: " + key);
		}
		return manager;
	}

	public <T extends EngineManager> T register(ManagerKey key, T manager) {
		return register(key.id(), manager);
	}

	/**
	 * Registers under the manager's simple class name.
	 */
	public <T extends EngineManager> T register(T manager) {
		return register(manager.getClass().getSimpleName(), manager);
	}

	public @Nullable EngineManager get(String key) {
		return managers.get(key);
	}

	public @Nullable <T extends EngineManager> T get(String key, Class<T> type) {
		EngineManager m = managers.get(key);
		return type.isInstance(m) ? type.cast(m) : null;
	}

	public @Nullable <T extends EngineManager> T get(ManagerKey key, Class<T> type) {
		return get(key.id(), type);
	}

	/**
	 * Like {@link #get(String, Class)} but throws if absent or wrong type.
	 */
	public <T extends EngineManager> T require(String key, Class<T> type) {
		T m = get(key, type);
		if (m == null) {
			throw new IllegalStateException(
				"No manager registered under '" + key + "' of type " + type.getName());
		}
		return m;
	}

	public <T extends EngineManager> T require(ManagerKey key, Class<T> type) {
		return require(key.id(), type);
	}

	/**
	 * Returns a manager when it is registered, otherwise logs the missing
	 * optional dependency and returns {@code null}.
	 */
	public <T extends EngineManager> @Nullable T requireOrNull(ManagerKey key, Class<T> type) {
		return requireOrNull(key.id(), type);
	}

	public <T extends EngineManager> @Nullable T requireOrNull(String key, Class<T> type) {
		T manager = get(key, type);
		if (manager == null) {
			LOGGER.error("Skipping operation because no manager is registered under '{}' of type {}",
				key, type.getName());
		}
		return manager;
	}

	/**
	 * Returns the first manager assignable to {@code type}, or {@code null}.
	 */
	public <T extends EngineManager> @Nullable T get(Class<T> type) {
		for (EngineManager m : managers.values()) {
			if (type.isInstance(m)) {
				return type.cast(m);
			}
		}
		return null;
	}

	public Collection<EngineManager> getAll() {
		return Collections.unmodifiableCollection(managers.values());
	}

	public <T extends EngineManager> Collection<T> getAll(Class<T> type) {
		return managers.values().stream()
			.filter(type::isInstance)
			.map(type::cast)
			.toList();
	}

	public boolean has(String key) {
		return managers.containsKey(key);
	}

	public boolean has(ManagerKey key) {
		return has(key.id());
	}
}