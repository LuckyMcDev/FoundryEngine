package de.luckymcdev.foundryengine.common.font;

import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.network.chat.FontDescription;

import java.util.List;

/**
 * Font definition IDs for the built-in JetBrains Mono Nerd Font family.
 * Each constant is a font definition JSON identifier (namespace:path, no "font/" prefix or ".json" suffix).
 */
public class BuiltInFonts {
	public static final FontDescription.Resource LIGHT = new FontDescription.Resource(Common.id("jbmononflight"));
	public static final FontDescription.Resource REGULAR = new FontDescription.Resource(Common.id("jbmononfregular"));
	public static final FontDescription.Resource MEDIUM = new FontDescription.Resource(Common.id("jbmononfmedium"));
	public static final FontDescription.Resource SEMIBOLD = new FontDescription.Resource(Common.id("jbmononfsemibold"));
	public static final FontDescription.Resource BOLD = new FontDescription.Resource(Common.id("jbmononfbold"));
	public static final FontDescription.Resource ITALIC = new FontDescription.Resource(Common.id("jbmononfitalic"));
	public static final FontDescription.Resource BOLD_ITALIC = new FontDescription.Resource(Common.id("jbmononfbolditalic"));
	public static final FontDescription.Resource FALLBACK = new FontDescription.Resource(Common.id("jbregular"));

	public static final List<FontDescription.Resource> ALL = List.of(LIGHT, REGULAR, MEDIUM, SEMIBOLD, BOLD, ITALIC, BOLD_ITALIC);
	public static final List<FontDescription.Resource> MINIMAL = List.of(FALLBACK);

	public static FontDescription face(boolean bold, boolean italic) {
		if (bold && italic) {
			return BOLD_ITALIC;
		}
		if (bold) {
			return BOLD;
		}
		if (italic) {
			return ITALIC;
		}
		return REGULAR;
	}
}
