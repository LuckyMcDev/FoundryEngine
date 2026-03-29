package de.luckymcdev.foundryengine.common.bundle.config;

/**
 * A single typed config value belonging to a {@link BundleConfig}.
 * Obtain instances via {@link BundleConfigSpec}.
 *
 * @param <T> the value type (Boolean, Integer, Double, String)
 */
public class ConfigValue<T> {
    private final String key;
    private final T defaultValue;
    private final String comment;
    private T currentValue;

    ConfigValue(String key, T defaultValue, String comment) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.comment = comment;
        this.currentValue = defaultValue;
    }

    /**
     * Returns the current value, falling back to the default if unset.
     */
    public T get() {
        return currentValue;
    }

    /**
     * Returns the default value for this config entry.
     */
    public T getDefault() {
        return defaultValue;
    }

    public String getKey() {
        return key;
    }

    public String getComment() {
        return comment;
    }

    /**
     * Sets the current value. Called internally when loading from file.
     */
    @SuppressWarnings("unchecked")
    void set(Object raw) {
        // NightConfig may widen integers to Long — narrow back safely
        if (defaultValue instanceof Integer && raw instanceof Long l) {
            this.currentValue = (T) Integer.valueOf(l.intValue());
        } else {
            this.currentValue = (T) raw;
        }
    }

    void resetToDefault() {
        this.currentValue = defaultValue;
    }

    @Override
    public String toString() {
        return key + "=" + currentValue + " (default: " + defaultValue + ")";
    }
}