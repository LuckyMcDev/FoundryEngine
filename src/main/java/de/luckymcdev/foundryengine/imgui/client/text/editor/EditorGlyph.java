package de.luckymcdev.foundryengine.imgui.client.text.editor;

import de.luckymcdev.foundryengine.util.common.color.Color;

public final class EditorGlyph {
	public char ch;
	public Color color;

	public EditorGlyph(char ch, Color color) {
		this.ch = ch;
		this.color = color;
	}

	public EditorGlyph copy() {
		return new EditorGlyph(ch, color);
	}
}