package de.luckymcdev.foundryengine.client.imgui.text.preset.json;

import de.luckymcdev.foundryengine.client.imgui.text.color.AbstractBaseColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class JsonColorizer extends AbstractBaseColorizer {

	public static final Color COLOR_DEFAULT = Color.ofABGR(0xFFC6B7A9);
	public static final Color COLOR_KEYWORD = Color.ofABGR(0xFF8585FC);
	public static final Color COLOR_STRING = Color.ofABGR(0xFF74DBE6);
	public static final Color COLOR_NUMBER = Color.ofABGR(0xFF5DACA2);
	public static final Color COLOR_COMMENT = Color.ofABGR(0xFF888888);
	public static final Color COLOR_PUNCTUATION = Color.ofABGR(0xFFC6B7A9);

	private static final Set<String> KEYWORDS = new HashSet<>(Set.of("true", "false", "null"));

	private static final Pattern TOKEN_PATTERN = Pattern.compile(
		"(\"[^\"]*\"(?:\\\\\"[^\"]*\")*)"          // group 1: string
			+ "|(true|false|null)"                     // group 2: keywords
			+ "|(\\d+\\.?\\d*[eE]?[+-]?\\d*)"         // group 3: numbers
			+ "|(//.*?$)"                             // group 4: line comment
			+ "|([{}\\[\\],:])"                       // group 5: punctuation
			+ "|(\\s+)"                               // group 6: whitespace
	);

	@Override
	public Color getDefaultColor() {
		return COLOR_DEFAULT;
	}

	@Override
	protected void analyzeDocument(List<List<EditorGlyph>> lines) {
		// JSON has no user‑defined symbols
	}

	@Override
	protected void colorizeLineImpl(List<EditorGlyph> line, int lineIdx, String text) {
		Matcher m = TOKEN_PATTERN.matcher(text);
		int idx = 0;
		while (m.find()) {
			// Fill gaps with default color
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
		if (m.group(2) != null) return COLOR_KEYWORD;
		if (m.group(3) != null) return COLOR_NUMBER;
		if (m.group(4) != null) return COLOR_COMMENT;
		if (m.group(5) != null) return COLOR_PUNCTUATION;
		return COLOR_DEFAULT;
	}
}