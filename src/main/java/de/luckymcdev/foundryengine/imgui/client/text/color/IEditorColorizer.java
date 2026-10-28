package de.luckymcdev.foundryengine.imgui.client.text.color;

import de.luckymcdev.foundryengine.imgui.client.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.util.common.color.Color;

import java.util.List;

public interface IEditorColorizer {
	void colorizeVisibleLines(List<List<EditorGlyph>> lines, int firstVisibleLine, int lastVisibleLine);

	void markLineDirty(int lineIdx);

	void markLinesDirty(int startLine, int endLine);

	void invalidateAll();

	void colorizeLine(List<List<EditorGlyph>> lines, int lineIdx);

	Color getDefaultColor();
}