package de.luckymcdev.foundryengine.common.font;

import net.minecraft.resources.Identifier;

import java.util.List;

/**
 * Registry of built-in font identifiers for ImGui and game UI.
 */
public class BuiltInFonts {
	public static final Identifier LIGHT = TTFFile.JETBRAINS_MONO_NERD.light().id();
	public static final Identifier REGULAR = TTFFile.JETBRAINS_MONO_NERD.regular().id();
	public static final Identifier MEDIUM = TTFFile.JETBRAINS_MONO_NERD.medium().id();
	public static final Identifier SEMIBOLD = TTFFile.JETBRAINS_MONO_NERD.semibold().id();
	public static final Identifier BOLD = TTFFile.JETBRAINS_MONO_NERD.bold().id();
	public static final Identifier ITALIC = TTFFile.JETBRAINS_MONO_NERD.italic().id();
	public static final Identifier BOLD_ITALIC = TTFFile.JETBRAINS_MONO_NERD.boldItalic().id();
	public static final Identifier FALLBACK_JB = TTFFile.FALLBACK_JB.id();
	public static final List<Identifier> NORMAL_LIST = TTFFile.JETBRAINS_MONO_NERD.ids();
	public static final List<Identifier> MINIMAL_LIST = List.of(
		FALLBACK_JB
	);
}
