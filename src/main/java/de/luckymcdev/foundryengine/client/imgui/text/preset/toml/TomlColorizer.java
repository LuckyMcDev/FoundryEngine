package de.luckymcdev.foundryengine.client.imgui.text.preset.toml;

import de.luckymcdev.foundryengine.client.imgui.text.color.AbstractBaseColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TomlColorizer extends AbstractBaseColorizer {

	public static final Color COLOR_DEFAULT = Color.ofABGR(0xFFC6B7A9);
	public static final Color COLOR_KEYWORD = Color.ofABGR(0xFF8585FC);
	public static final Color COLOR_STRING = Color.ofABGR(0xFF74DBE6);
	public static final Color COLOR_NUMBER = Color.ofABGR(0xFF5DACA2);
	public static final Color COLOR_COMMENT = Color.ofABGR(0xFF888888);
	public static final Color COLOR_KEY = Color.ofABGR(0xFFE0C080);
	public static final Color COLOR_SECTION = Color.ofABGR(0xFFBA769A);
	public static final Color COLOR_BOOLEAN = Color.ofABGR(0xFF71C0F6);

	private static final Set<String> BOOLEANS = Set.of("true", "false");

	private static final Pattern TOKEN_PATTERN = Pattern.compile(
		"(\"[^\"]*\"(?:\\\\\"[^\"]*)*)"                  // group 1: string
			+ "|(#.*?$)"                                   // group 2: comment
			+ "|(\\btrue\\b|\\bfalse\\b)"                 // group 3: booleans
			+ "|(\\d+\\.?\\d*[eE]?[+-]?\\d*)"             // group 4: numbers
			+ "|(\\[\\s*[a-zA-Z_][a-zA-Z0-9_.]*\\s*\\])"  // group 5: section header
			+ "|([a-zA-Z_][a-zA-Z0-9_.]*)\\s*="            // group 6: key
			+ "|(\\s+)"                                   // group 7: whitespace
	);

	@Override
	public Color getDefaultColor() {
		return COLOR_DEFAULT;
	}

	@Override
	protected void analyzeDocument(List<List<EditorGlyph>> lines) {
		// no user symbols
	}

	@Override
	protected void colorizeLineImpl(List<EditorGlyph> line, int lineIdx, String text) {
		Matcher m = TOKEN_PATTERN.matcher(text);
		int idx = 0;
		while (m.find()) {
			while (idx < m.start() && idx < line.size()) {
				line.get(idx++).color = COLOR_DEFAULT;
			}
			Color color = resolveColor(m);
			for (int i = m.start(); i < m.end() && i < line.size(); i++) {
				line.get(i).color = color;
			}
			idx = m.end();
		}
		while (idx < line.size()) {
			line.get(idx++).color = COLOR_DEFAULT;
		}
	}

	private Color resolveColor(Matcher m) {
		if (m.group(1) != null) return COLOR_STRING;
		if (m.group(2) != null) return COLOR_COMMENT;
		if (m.group(3) != null) return COLOR_BOOLEAN;
		if (m.group(4) != null) return COLOR_NUMBER;
		if (m.group(5) != null) return COLOR_SECTION;
		if (m.group(6) != null) return COLOR_KEY;
		return COLOR_DEFAULT;
	}
}