package de.luckymcdev.foundryengine.common.util.ini;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * Represents and manages a complete INI file.
 * <p>
 * Handles loading, saving, and providing access to sections.
 */
public class IniFile {
    private final Path filePath;
    private final Map<String, IniSection> sections = new LinkedHashMap<>();
    private final List<String> globalComments = new ArrayList<>();

    /**
     * Creates an instance and loads the file from the given path.
     *
     * @param filePath Path to the .ini file.
     * @throws IOException if file reading fails.
     */
    public IniFile(Path filePath) throws IOException {
        this.filePath = filePath;
        load();
    }

    /**
     * Loads (or reloads) the file from disk.
     */
    public void load() throws IOException {
        sections.clear();
        if (!Files.exists(filePath)) return;
        List<String> lines = Files.readAllLines(filePath);
        parseLines(lines);
    }

    /**
     * Writes the current in‑memory data back to the file.
     */
    public void save() throws IOException {
        List<String> lines = new ArrayList<>();
        // Add global comments
        for (String comment : globalComments) {
            lines.add("; " + comment);
        }
        if (!globalComments.isEmpty()) lines.add("");

        // Add each section
        for (IniSection section : sections.values()) {
            lines.add("[" + section.getName() + "]");
            lines.addAll(section.toLines());
            lines.add(""); // blank line between sections
        }

        // Remove trailing blank line
        while (!lines.isEmpty() && lines.get(lines.size() - 1).isEmpty()) {
            lines.remove(lines.size() - 1);
        }
        Files.write(filePath, lines);
    }

    public IniSection getOrCreateSection(String name) {
        return sections.computeIfAbsent(name, IniSection::new);
    }

    public Optional<IniSection> getSection(String name) {
        return Optional.ofNullable(sections.get(name));
    }

    /**
     * Adds a comment that will appear at the top of the file (before any section).
     */
    public void addGlobalComment(String comment) {
        globalComments.add(comment);
    }

    private void parseLines(List<String> lines) {
        IniSection currentSection = null;
        for (String rawLine : lines) {
            String line = rawLine.trim();

            // Skip empty lines
            if (line.isEmpty()) continue;

            // Comment lines (starts with ';' or '#')
            if (line.startsWith(";") || line.startsWith("#")) {
                String comment = line.substring(1).trim();
                if (currentSection == null) {
                    globalComments.add(comment);
                } else {
                    currentSection.addComment(comment);
                }
                continue;
            }

            // Section header (e.g., "[Database]")
            if (line.startsWith("[") && line.endsWith("]")) {
                String sectionName = line.substring(1, line.length() - 1).trim();
                currentSection = getOrCreateSection(sectionName);
                continue;
            }

            // Key-value pair (e.g., "host=localhost")
            int delimiterIndex = findDelimiter(line);
            if (delimiterIndex > 0) {
                String key = line.substring(0, delimiterIndex).trim();
                String value = line.substring(delimiterIndex + 1).trim();
                if (currentSection == null) {
                    currentSection = getOrCreateSection("global");
                }
                currentSection.setValue(key, value);
            }
            // If line is none of the above, it’s malformed – we simply ignore it.
        }
    }

    /**
     * Finds the first valid delimiter ('=' or ':') that separates key and value.
     */
    private int findDelimiter(String line) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '=' || c == ':') return i;
            // If we encounter a comment character before a delimiter, treat as no delimiter
            if (c == ';' || c == '#') break;
        }
        return -1;
    }
}