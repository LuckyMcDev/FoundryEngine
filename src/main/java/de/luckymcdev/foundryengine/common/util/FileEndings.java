package de.luckymcdev.foundryengine.common.util;

import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.editor.builtin.code.CodeEditorLanguageDefinitions;
import imgui.extension.texteditor.TextEditorLanguageDefinition;

import java.util.HashMap;
import java.util.Map;

public class FileEndings {

    private static final Map<String, TextEditorLanguageDefinition> EXTENSION_TO_LANG = new HashMap<>();
    private static final Map<String, String> EXTENSION_TO_ICON = new HashMap<>();

    static {
        // GLSL Shaders
        registerLanguage(CodeEditorLanguageDefinitions.glsl(), "glsl", "vsh", "fsh", "geom", "comp");
        // Java
        registerLanguage(CodeEditorLanguageDefinitions.java(), "java");
        // Groovy
        registerLanguage(CodeEditorLanguageDefinitions.groovy(), "groovy", "gradle");
        // JSON
        registerLanguage(CodeEditorLanguageDefinitions.json(), "json");
        // TOML
        registerLanguage(CodeEditorLanguageDefinitions.toml(), "toml");

        registerIcon(EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_CODE),
                "java", "groovy", "glsl", "vsh", "fsh", "js");

        registerIcon(EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_IMPORT),
                "json", "toml", "yaml", "yml");

        registerIcon(EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_IMAGE),
                "png", "jpg", "jpeg", "tga");

        registerIcon(EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_AUDIO),
                "ogg", "wav", "mp3");

        registerIcon(EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_ZIPPER),
                "zip", "jar", "tar", "gz");

        registerIcon(EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_TEXT),
                "txt", "log");

        registerIcon(EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_PEN),
                "md");
    }

    private static void registerLanguage(TextEditorLanguageDefinition def, String... extensions) {
        for (String ext : extensions) {
            EXTENSION_TO_LANG.put(ext.toLowerCase(), def);
        }
    }

    private static void registerIcon(String icon, String... extensions) {
        for (String ext : extensions) {
            EXTENSION_TO_ICON.put(ext.toLowerCase(), icon);
        }
    }

    /**
     * Gets the language definition for the editor based on file extension.
     */
    public static TextEditorLanguageDefinition getLanguageDefinitionByFileName(String fileName) {
        String ext = getExtension(fileName);
        return ext != null ? EXTENSION_TO_LANG.get(ext) : null;
    }

    /**
     * Gets the FontAwesome icon string based on file extension.
     */
    public static String getFileIcon(String fileName) {
        String ext = getExtension(fileName);
        return EXTENSION_TO_ICON.getOrDefault(ext, EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_O));
    }

    private static String getExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return null;
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }
}