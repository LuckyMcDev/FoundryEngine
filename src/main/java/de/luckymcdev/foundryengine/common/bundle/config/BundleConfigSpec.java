package de.luckymcdev.foundryengine.common.bundle.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

public class BundleConfigSpec {
	private final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
	private ModConfigSpec spec;
	private boolean built = false;

	/**
	 * Throws an IllegalStateException if the spec has been built already.
	 */
	private void checkBuilt() {
		if (built) {
			throw new IllegalStateException("BundleConfigSpec already built");
		}
	}

	/**
	 * If you call this with comment = null, call the other method without the comment param.
	 */
	public ConfigValue<Boolean> defineBoolean(String key, boolean defaultValue, @Nullable String comment) {
		checkBuilt();
		if (comment != null) {
			builder.comment(comment);
		}
		var neoValue = builder.define(key, defaultValue);
		return new ConfigValue<>(neoValue);
	}

	public ConfigValue<Boolean> defineBoolean(String key, boolean defaultValue) {
		return defineBoolean(key, defaultValue, null);
	}

	/**
	 * If you call this with comment = null, call the other method without the comment param.
	 */
	public ConfigValue<Integer> defineInt(String key, int defaultValue, int minValue, int maxValue, @Nullable String comment) {
		checkBuilt();
		if (comment != null) {
			builder.comment(comment);
		}
		var neoValue = builder.defineInRange(key, defaultValue, minValue, maxValue);
		return new ConfigValue<>(neoValue);
	}

	public ConfigValue<Integer> defineInt(String key, int defaultValue, int minValue, int maxValue) {
		return defineInt(key, defaultValue, minValue, maxValue, null);
	}

	/**
	 * If you call this with comment = null, call the other method without the comment param.
	 */
	public ConfigValue<Double> defineDouble(String key, double defaultValue, double minValue, double maxValue, @Nullable String comment) {
		checkBuilt();
		if (comment != null) {
			builder.comment(comment);
		}
		var neoValue = builder.defineInRange(key, defaultValue, minValue, maxValue);
		return new ConfigValue<>(neoValue);
	}

	public ConfigValue<Double> defineDouble(String key, double defaultValue, double minValue, double maxValue) {
		return defineDouble(key, defaultValue, minValue, maxValue, null);
	}

	/**
	 * If you call this with comment = null, call the other method without the comment param.
	 */
	public ConfigValue<String> defineString(String key, String defaultValue, @Nullable String comment) {
		checkBuilt();
		if (comment != null) {
			builder.comment(comment);
		}
		var neoValue = builder.define(key, defaultValue);
		return new ConfigValue<>(neoValue);
	}

	public ConfigValue<String> defineString(String key, String defaultValue) {
		return defineString(key, defaultValue, null);
	}

	@ApiStatus.Internal
	public ModConfigSpec build() {
		checkBuilt();
		built = true;
		spec = builder.build();
		return spec;
	}
}