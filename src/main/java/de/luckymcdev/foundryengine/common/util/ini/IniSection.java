package de.luckymcdev.foundryengine.common.util.ini;

import java.util.*;

/**
 * Represents a single section inside an INI file.
 * <p>
 * Stores keys, values, and comments that belong directly to the section.
 */
public class IniSection {
    private final String name;
    private final Map<String, String> values = new LinkedHashMap<>();
    private final List<String> comments = new ArrayList<>();

    public IniSection(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(String key, String value) {
        values.put(key, value);
    }

    public Optional<String> getValue(String key) {
        return Optional.ofNullable(values.get(key));
    }

    public void addComment(String comment) {
        comments.add(comment);
    }

    List<String> toLines() {
        List<String> lines = new ArrayList<>();
        for (String comment : comments) {
            lines.add("; " + comment);
        }
        for (Map.Entry<String, String> entry : values.entrySet()) {
            lines.add(entry.getKey() + "=" + entry.getValue());
        }
        return lines;
    }

    public void removeKey(String key) {
        values.remove(key);
    }

    public Set<String> getKeys() {
        return Collections.unmodifiableSet(values.keySet());
    }
}