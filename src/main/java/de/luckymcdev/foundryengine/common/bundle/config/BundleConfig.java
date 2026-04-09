package de.luckymcdev.foundryengine.common.bundle.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds the configuration for a single Bundle.
 * Values are defined programmatically via {@link BundleConfigSpec} and persisted to
 * {@code FoundryEngine/config/<bundleId>.toml}.
 */
public class BundleConfig {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final String bundleId;
    private final Path configPath;
    private final Map<String, ConfigValue<?>> values = new LinkedHashMap<>();
    private boolean loaded = false;

    public BundleConfig(String bundleId, Path configDirectory) {
        this.bundleId = bundleId;
        this.configPath = configDirectory.resolve(bundleId + ".toml");
    }

    /**
     * Registers a config value. Called by {@link BundleConfigSpec} during spec building.
     */
    <T> void register(ConfigValue<T> value) {
        if (loaded) {
            throw new IllegalStateException(
                    "Cannot register config values after the config has been loaded for bundle: " + bundleId
            );
        }
        if (values.containsKey(value.getKey())) {
            throw new IllegalArgumentException(
                    "Duplicate config key '" + value.getKey() + "' in bundle: " + bundleId
            );
        }
        values.put(value.getKey(), value);
    }

    /**
     * Loads values from the TOML file on disk. Missing keys are filled with defaults.
     * Should be called once after all values have been registered.
     */
    public void load() {
        if (values.isEmpty()) {
            loaded = true;
            return;
        }

        try (CommentedFileConfig fileConfig = CommentedFileConfig.builder(configPath.toFile())
                .preserveInsertionOrder()
                .autosave()
                .build()) {

            fileConfig.load();
            applyFromFile(fileConfig);
            writeDefaults(fileConfig);
        } catch (Exception e) {
            LOGGER.error("Failed to load config for bundle '{}', using defaults", bundleId, e);
            applyDefaults();
        }

        loaded = true;
        LOGGER.debug("Loaded config for bundle '{}' from {}", bundleId, configPath);
    }

    /**
     * Saves all current values back to the TOML file.
     */
    public void save() {
        if (values.isEmpty()) return;

        try (CommentedFileConfig fileConfig = CommentedFileConfig.builder(configPath.toFile())
                .preserveInsertionOrder()
                .build()) {

            for (ConfigValue<?> value : values.values()) {
                fileConfig.set(value.getKey(), value.get());
                if (value.getComment() != null) {
                    fileConfig.setComment(value.getKey(), value.getComment());
                }
            }

            fileConfig.save();
            LOGGER.debug("Saved config for bundle '{}'", bundleId);
        } catch (Exception e) {
            LOGGER.error("Failed to save config for bundle '{}'", bundleId, e);
        }
    }

    private void applyFromFile(CommentedConfig fileConfig) {
        for (ConfigValue<?> value : values.values()) {
            Object raw = fileConfig.get(value.getKey());
            if (raw != null) {
                try {
                    value.set(raw);
                } catch (Exception e) {
                    LOGGER.warn("Invalid value for key '{}' in bundle '{}', using default: {}",
                            value.getKey(), bundleId, value.getDefault());
                }
            }
        }
    }

    private void writeDefaults(CommentedFileConfig fileConfig) {
        boolean changed = false;
        for (ConfigValue<?> value : values.values()) {
            if (!fileConfig.contains(value.getKey())) {
                fileConfig.set(value.getKey(), value.getDefault());
                if (value.getComment() != null) {
                    fileConfig.setComment(value.getKey(), value.getComment());
                }
                changed = true;
            }
        }
        if (changed) {
            fileConfig.save();
        }
    }

    private void applyDefaults() {
        for (ConfigValue<?> value : values.values()) {
            value.resetToDefault();
        }
    }

    public String getBundleId() {
        return bundleId;
    }

    public Path getConfigPath() {
        return configPath;
    }

    public boolean isLoaded() {
        return loaded;
    }
}