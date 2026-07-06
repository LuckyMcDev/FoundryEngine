package de.luckymcdev.foundryengine.common.util;

import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps file extensions to file icons.
 */
public class FileEndings {
	private static final Map<String, String> EXTENSION_TO_ICON = new HashMap<>();

	static {
		registerIcon(ImGraphicsExtractor.icon(ImIcons.FILE_CODE), "java", "groovy", "glsl", "vsh", "fsh", "js");
		registerIcon(ImGraphicsExtractor.icon(ImIcons.FILE_IMPORT), "json", "toml", "yaml", "yml");
		registerIcon(ImGraphicsExtractor.icon(ImIcons.FILE_IMAGE), "png", "jpg", "jpeg", "tga");
		registerIcon(ImGraphicsExtractor.icon(ImIcons.FILE_AUDIO), "ogg", "wav", "mp3");
		registerIcon(ImGraphicsExtractor.icon(ImIcons.FILE_ZIPPER), "zip", "jar", "tar", "gz");
		registerIcon(ImGraphicsExtractor.icon(ImIcons.FILE_TEXT), "txt", "log");
		registerIcon(ImGraphicsExtractor.icon(ImIcons.FILE_PEN), "md");
	}

	private static void registerIcon(String icon, String... extensions) {
		for (String ext : extensions) {
			EXTENSION_TO_ICON.put(ext.toLowerCase(), icon);
		}
	}

	/**
	 * Returns the FontAwesome icon string for the given file name based on its extension.
	 */
	public static String getFileIcon(String fileName) {
		String ext = getExtension(fileName);
		return EXTENSION_TO_ICON.getOrDefault(ext, ImGraphicsExtractor.icon(ImIcons.FILE_O));
	}

	public static String getExtension(String fileName) {
		if (fileName == null || !fileName.contains(".")) {
			return null;
		}
		return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
	}
}