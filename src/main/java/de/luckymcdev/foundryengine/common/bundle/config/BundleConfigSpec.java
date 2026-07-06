package de.luckymcdev.foundryengine.common.bundle.config;

/**
 * Builder used inside {@link de.luckymcdev.foundryengine.common.script.BundleEntrypoint#onLoad()}
 * to declare typed config entries for a bundle.
 *
 * <p>Example usage in a Groovy entrypoint:
 * <pre>{@code
 * class MyEntrypoint extends BundleEntrypoint {
 *     ConfigValue<Boolean> enableFeature
 *     ConfigValue<Integer> maxCount
 *
 *     void onLoad() {
 *         def spec = new BundleConfigSpec(bundleConfig)
 *         enableFeature = spec.defineBoolean("enable_feature", true, "Whether the feature is enabled")
 *         maxCount      = spec.defineInt("max_count", 10, "Maximum number of things")
 *         spec.build()
 *
 *         if (enableFeature.get()) { ... }
 *     }
 * }
 * }</pre>
 */
public class BundleConfigSpec {
	private final BundleConfig config;
	private boolean built = false;

	public BundleConfigSpec(BundleConfig config) {
		this.config = config;
	}

	/**
	 * Defines a boolean config value.
	 */
	public ConfigValue<Boolean> defineBoolean(String key, boolean defaultValue, String comment) {
		return define(key, defaultValue, comment);
	}

	public ConfigValue<Boolean> defineBoolean(String key, boolean defaultValue) {
		return defineBoolean(key, defaultValue, null);
	}

	/**
	 * Defines an integer config value.
	 */
	public ConfigValue<Integer> defineInt(String key, int defaultValue, String comment) {
		return define(key, defaultValue, comment);
	}

	public ConfigValue<Integer> defineInt(String key, int defaultValue) {
		return defineInt(key, defaultValue, null);
	}

	/**
	 * Defines a double config value.
	 */
	public ConfigValue<Double> defineDouble(String key, double defaultValue, String comment) {
		return define(key, defaultValue, comment);
	}

	public ConfigValue<Double> defineDouble(String key, double defaultValue) {
		return defineDouble(key, defaultValue, null);
	}

	/**
	 * Defines a string config value.
	 */
	public ConfigValue<String> defineString(String key, String defaultValue, String comment) {
		return define(key, defaultValue, comment);
	}

	public ConfigValue<String> defineString(String key, String defaultValue) {
		return defineString(key, defaultValue, null);
	}

	/**
	 * Finalizes the spec and triggers a load from disk.
	 * Must be called after all values are defined.
	 */
	public void build() {
		if (built) {
			throw new IllegalStateException("BundleConfigSpec.build() called more than once for bundle: "
				+ config.getBundleId());
		}
		built = true;
		config.load();
	}

	private <T> ConfigValue<T> define(String key, T defaultValue, String comment) {
		if (built) {
			throw new IllegalStateException(
				"Cannot define config values after build() has been called for bundle: " + config.getBundleId()
			);
		}
		ConfigValue<T> value = new ConfigValue<>(key, defaultValue, comment);
		config.register(value);
		return value;
	}
}