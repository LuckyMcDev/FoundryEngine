package de.luckymcdev.foundryengine.common.util.ini;

import de.luckymcdev.foundryengine.common.Common;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manager that provides a simple database‑like interface to an INI file.
 * <p>
 * Supports typed getters/setters, default values, and auto‑creation of sections.
 * All changes are buffered; call {@link #save()} to persist to disk.
 */
public class IniFileManager {
    private final IniFile iniFile;
    private boolean autoSave = false;

    /**
     * Creates a manager for the given INI file.
     *
     * @param iniFile the underlying INI file (already loaded)
     */
    public IniFileManager(IniFile iniFile) {
        this.iniFile = iniFile;
    }

    /**
     * Sets a string value in a section. The section is created if it does not exist.
     *
     * @param section section name
     * @param key     key name
     * @param value   value to store
     */
    public void putString(@NotNull String section, @NotNull String key, @NotNull String value) {
        iniFile.getOrCreateSection(section).setValue(key, value);
        if (autoSave) save();
    }

    /**
     * Returns a string value from a section.
     *
     * @param section section name
     * @param key     key name
     * @return the value, or empty if not present
     */
    public Optional<String> getString(@NotNull String section, @NotNull String key) {
        return iniFile.getSection(section)
                .flatMap(s -> s.getValue(key));
    }

    /**
     * Returns a string value with a fallback default.
     *
     * @param section      section name
     * @param key          key name
     * @param defaultValue value to return if key/section missing
     * @return the stored value or defaultValue
     */
    public String getString(@NotNull String section, @NotNull String key, @NotNull String defaultValue) {
        return getString(section, key).orElse(defaultValue);
    }

    public void putInt(@NotNull String section, @NotNull String key, int value) {
        putString(section, key, Integer.toString(value));
    }

    public Optional<Integer> getInt(@NotNull String section, @NotNull String key) {
        return getString(section, key)
                .flatMap(s -> {
                    try {
                        return Optional.of(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        Common.LOGGER.warn("Invalid integer for {}.{}: '{}'", section, key, s);
                        return Optional.empty();
                    }
                });
    }

    public int getInt(@NotNull String section, @NotNull String key, int defaultValue) {
        return getInt(section, key).orElse(defaultValue);
    }

    public void putBoolean(@NotNull String section, @NotNull String key, boolean value) {
        putString(section, key, Boolean.toString(value));
    }

    public Optional<Boolean> getBoolean(@NotNull String section, @NotNull String key) {
        return getString(section, key)
                .map(String::toLowerCase)
                .flatMap(s -> {
                    if (s.equals("true") || s.equals("1") || s.equals("yes") || s.equals("on"))
                        return Optional.of(true);
                    if (s.equals("false") || s.equals("0") || s.equals("no") || s.equals("off"))
                        return Optional.of(false);
                    Common.LOGGER.warn("Invalid boolean for {}.{}: '{}'", section, key, s);
                    return Optional.empty();
                });
    }

    public boolean getBoolean(@NotNull String section, @NotNull String key, boolean defaultValue) {
        return getBoolean(section, key).orElse(defaultValue);
    }

    public void putDouble(@NotNull String section, @NotNull String key, double value) {
        putString(section, key, Double.toString(value));
    }

    public Optional<Double> getDouble(@NotNull String section, @NotNull String key) {
        return getString(section, key)
                .flatMap(s -> {
                    try {
                        return Optional.of(Double.parseDouble(s));
                    } catch (NumberFormatException e) {
                        Common.LOGGER.warn("Invalid double for {}.{}: '{}'", section, key, s);
                        return Optional.empty();
                    }
                });
    }

    public double getDouble(@NotNull String section, @NotNull String key, double defaultValue) {
        return getDouble(section, key).orElse(defaultValue);
    }

    public void putList(@NotNull String section, @NotNull String key, @NotNull List<String> values) {
        putString(section, key, String.join(",", values));
    }

    public List<String> getList(@NotNull String section, @NotNull String key) {
        return getString(section, key)
                .map(s -> Arrays.stream(s.split(","))
                        .map(String::trim)
                        .filter(p -> !p.isEmpty())
                        .collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    public List<String> getList(@NotNull String section, @NotNull String key, @NotNull List<String> defaultValue) {
        List<String> value = getList(section, key);
        return value.isEmpty() ? defaultValue : value;
    }

    /**
     * Checks if a section exists.
     */
    public boolean hasSection(@NotNull String section) {
        return iniFile.getSection(section).isPresent();
    }

    /**
     * Checks if a key exists inside a section.
     */
    public boolean hasKey(@NotNull String section, @NotNull String key) {
        return iniFile.getSection(section)
                .map(s -> s.getValue(key).isPresent())
                .orElse(false);
    }

    /**
     * Removes a key from a section. Does nothing if section or key missing.
     *
     * @return true if the key was removed
     */
    public boolean removeKey(@NotNull String section, @NotNull String key) {
        boolean removed = iniFile.getSection(section)
                .map(s -> {
                    if (s.getValue(key).isPresent()) {
                        s.removeKey(key);
                        return true;
                    }
                    return false;
                })
                .orElse(false);
        if (removed && autoSave) save();
        return removed;
    }

    /**
     * Removes an entire section and all its keys.
     *
     * @return true if the section existed and was removed
     */
    public boolean removeSection(@NotNull String section) {
        boolean removed = iniFile.removeSection(section);
        if (removed && autoSave) save();
        return removed;
    }

    /**
     * Returns all keys in a section (empty set if section missing).
     */
    public Set<String> getKeys(@NotNull String section) {
        return iniFile.getSection(section)
                .map(IniSection::getKeys)
                .orElse(Collections.emptySet());
    }

    /**
     * Returns all section names.
     */
    public Set<String> getSections() {
        return iniFile.getSections().keySet();
    }

    // --- Persistence ----------------------------------------------------------

    /**
     * Saves the current state to the INI file.
     */
    public void save() {
        try {
            iniFile.save();
        } catch (IOException e) {
            Common.LOGGER.error("Failed to save INI file: {}", iniFile.getPath(), e);
        }
    }

    /**
     * Reloads the INI file from disk, discarding any unsaved changes.
     */
    public void reload() {
        try {
            iniFile.load();
        } catch (IOException e) {
            Common.LOGGER.error("Failed to reload INI file: {}", iniFile.getPath(), e);
        }
    }

    /**
     * Enables or disables auto‑save after every write operation.
     * Default is {@code false} – you must call {@link #save()} manually.
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }

    /**
     * Returns the underlying {@link IniFile} for advanced operations.
     */
    public IniFile getUnderlyingFile() {
        return iniFile;
    }
}