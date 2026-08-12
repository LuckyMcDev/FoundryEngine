package de.luckymcdev.foundryengine.client.imgui.text.preset.pakku;

import de.luckymcdev.foundryengine.client.imgui.text.color.AbstractBaseColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.List;
import java.util.regex.Pattern;

public final class PakkuColorizer extends AbstractBaseColorizer {

	public static final Color COLOR_DEFAULT   = Color.ofABGR(0xFFD4D4D4);
	public static final Color COLOR_HEADER    = Color.ofABGR(0xFF569CD6);
	public static final Color COLOR_PACKAGE   = Color.ofABGR(0xFF4EC9B0);
	public static final Color COLOR_VERSION   = Color.ofABGR(0xFFDCDCAA);
	public static final Color COLOR_STATUS    = Color.ofABGR(0xFF6A9955);
	public static final Color COLOR_ERROR     = Color.ofABGR(0xFFF44747);
	public static final Color COLOR_NUMBER    = Color.ofABGR(0xFFB5CEA8);
	public static final Color COLOR_COMMENT   = Color.ofABGR(0xFF6A9955);

	private static final Pattern PACKAGE_LINE = Pattern.compile("^\\s*[a-zA-Z0-9_.-]+\\s+[0-9.]+.*");
	private static final Pattern VERSION_LINE = Pattern.compile(".*[0-9]+\\.[0-9]+\\.[0-9]+.*");
	private static final Pattern STATUS_LINE  = Pattern.compile("^\\s*(installed|available|updating|removing).*", Pattern.CASE_INSENSITIVE);

	private static Color pickColor(String line) {
		if (line.isEmpty()) return COLOR_DEFAULT;

		if (line.startsWith("#")) return COLOR_COMMENT;
		if (line.startsWith("[") || line.startsWith("Repo") || line.startsWith("Repository")) return COLOR_HEADER;
		if (line.startsWith("Installing") || line.startsWith("Removing") || line.startsWith("Updating")) return COLOR_STATUS;
		if (line.contains("error") || line.contains("Error") || line.contains("failed")) return COLOR_ERROR;

		if (PACKAGE_LINE.matcher(line).matches()) {
			return COLOR_PACKAGE;
		}
		if (STATUS_LINE.matcher(line).matches()) return COLOR_STATUS;

		if (line.matches("^\\s*[a-zA-Z0-9_.-]+$")) return COLOR_PACKAGE; // single word – likely a package name

		return COLOR_DEFAULT;
	}

	@Override
	public Color getDefaultColor() {
		return COLOR_DEFAULT;
	}

	@Override
	protected void analyzeDocument(List<List<EditorGlyph>> lines) {
		// nothing
	}

	@Override
	protected void colorizeLineImpl(List<EditorGlyph> line, int lineIdx, String text) {
		Color color = pickColor(text);
		for (EditorGlyph g : line) {
			g.color = color;
		}
	}
}