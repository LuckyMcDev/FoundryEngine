package de.luckymcdev.foundryengine.common.world.level.util;

import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.Nullable;

public final class GameRuleStore {
	private final Reference2ObjectMap<GameRule<?>, Object> rules = new Reference2ObjectOpenHashMap<>();

	public <T> void set(GameRule<T> key, T value) {
		this.rules.put(key, value);
	}

	public <T> T get(GameRule<T> key) {
		return (T) this.rules.get(key);
	}

	public boolean contains(GameRule<?> key) {
		return this.rules.containsKey(key);
	}

	@SuppressWarnings("unchecked")
	public void applyTo(GameRules rules, @Nullable MinecraftServer server) {
		Reference2ObjectMaps.fastForEach(this.rules, entry -> {
			rules.set((GameRule<? super Object>) entry.getKey(), entry.getValue(), server);
		});

	}
}
