package de.luckymcdev.foundryengine.client.imgui.text.preset.diff;

import de.luckymcdev.foundryengine.client.imgui.text.color.AbstractBaseColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.List;

public final class DiffColorizer extends AbstractBaseColorizer {

	public static final Color COLOR_DEFAULT      = Color.ofABGR(0xFFD4D4D4);
	public static final Color COLOR_ADDED        = Color.ofABGR(0xFF5BA85B); // green
	public static final Color COLOR_REMOVED      = Color.ofABGR(0xFFB05252); // red
	public static final Color COLOR_HUNK_HEADER  = Color.ofABGR(0xFF7EB8D4); // cyan-blue  (@@ ... @@)
	public static final Color COLOR_FILE_HEADER  = Color.ofABGR(0xFFDDA85A); // orange     (--- +++ diff --git)
	public static final Color COLOR_INDEX        = Color.ofABGR(0xFF888888); // grey        (index / similarity)
	public static final Color COLOR_BINARY       = Color.ofABGR(0xFFCC88CC); // purple      (Binary files ...)
	public static final Color COLOR_NO_NEWLINE   = Color.ofABGR(0xFF999955); // olive       (\ No newline at end)

	@Override
	public Color getDefaultColor() {
		return COLOR_DEFAULT;
	}

	@Override
	protected void analyzeDocument(List<List<EditorGlyph>> lines) {}

	@Override
	protected void colorizeLineImpl(List<EditorGlyph> line, int lineIdx, String text) {
		Color color = pickColor(text);
		for (EditorGlyph g : line) {
			g.color = color;
		}
	}

	private static Color pickColor(String text) {
		if (text.isEmpty()) return COLOR_DEFAULT;

		if (text.startsWith("@@")) return COLOR_HUNK_HEADER;

		if (text.startsWith("--- ") || text.startsWith("+++ ")) return COLOR_FILE_HEADER;
		if (text.startsWith("diff ") || text.startsWith("index ") || text.startsWith("new file")
			|| text.startsWith("deleted file") || text.startsWith("rename ")
			|| text.startsWith("similarity ") || text.startsWith("old mode")
			|| text.startsWith("new mode")) return COLOR_INDEX;

		if (text.startsWith("Binary files")) return COLOR_BINARY;

		if (text.startsWith("\\ No newline")) return COLOR_NO_NEWLINE;

		if (text.startsWith("+")) return COLOR_ADDED;
		if (text.startsWith("-")) return COLOR_REMOVED;

		return COLOR_DEFAULT;
	}
}