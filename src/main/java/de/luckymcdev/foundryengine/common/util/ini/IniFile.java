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

    /**
     * Returns an existing section or creates a new one.
     */
    public IniSection getOrCreateSection(String name) {
        return sections.computeIfAbsent(name, IniSection::new);
    }

    /**
     * Returns a section by name, if present.
     */
    public Optional<IniSection> getSection(String name) {
        return Optional.ofNullable(sections.get(name));
    }

    /**
     * Returns an unmodifiable view of all sections (name → section).
     */
    public Map<String, IniSection> getSections() {
        return Collections.unmodifiableMap(sections);
    }

    /**
     * Removes a section by name.
     *
     * @return true if the section existed and was removed, false otherwise
     */
    public boolean removeSection(String name) {
        return sections.remove(name) != null;
    }

    /**
     * Adds a comment that will appear at the top of the file (before any section).
     */
    public void addGlobalComment(String comment) {
        globalComments.add(comment);
    }

    /**
     * Returns the file path this INI file is bound to.
     */
    public Path getPath() {
        return filePath;
    }

    private void parseLines(List<String> lines) {
        IniSection currentSection = null;
        for (String rawLine : lines) {
            String line = rawLine.trim();

            if (line.isEmpty()) continue;

            if (line.startsWith(";") || line.startsWith("#")) {
                String comment = line.substring(1).trim();
                if (currentSection == null) {
                    globalComments.add(comment);
                } else {
                    currentSection.addComment(comment);
                }
                continue;
            }

            if (line.startsWith("[") && line.endsWith("]")) {
                String sectionName = line.substring(1, line.length() - 1).trim();
                currentSection = getOrCreateSection(sectionName);
                continue;
            }

            int delimiterIndex = findDelimiter(line);
            if (delimiterIndex > 0) {
                String key = line.substring(0, delimiterIndex).trim();
                String value = line.substring(delimiterIndex + 1).trim();
                if (currentSection == null) {
                    currentSection = getOrCreateSection("global");
                }
                currentSection.setValue(key, value);
            }
        }
    }

    private int findDelimiter(String line) {
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '=' || c == ':') return i;
            if (c == ';' || c == '#') break;
        }
        return -1;
    }
}