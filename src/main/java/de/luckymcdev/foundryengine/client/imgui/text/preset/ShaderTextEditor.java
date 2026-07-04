package de.luckymcdev.foundryengine.client.imgui.text.preset;

import de.luckymcdev.foundryengine.client.imgui.text.ImGuiCoreTextEditor;
import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.IAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.color.IEditorColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorTheme;
import de.luckymcdev.foundryengine.client.imgui.text.preset.glsl.GLSLAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.preset.glsl.GLSLColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.preset.groovy.GroovyAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.preset.groovy.GroovyColorizer;

public enum ShaderTextEditor {
	;

	public static ImGuiCoreTextEditor create(Language language) {
		return create(language, EditorTheme.dark().build());
	}

	public static ImGuiCoreTextEditor create(Language language, EditorTheme theme) {
		IEditorColorizer colorizer;
		IAutocompleteProvider provider;

		switch (language) {
			case GROOVY: {
				GroovyColorizer c = new GroovyColorizer();
				colorizer = c;
				provider = new GroovyAutocompleteProvider();
				break;
			}
			case GLSL:
			default: {
				GLSLColorizer c = new GLSLColorizer();
				colorizer = c;
				provider = new GLSLAutocompleteProvider(c);
				break;
			}
		}

		return new ImGuiCoreTextEditor(colorizer, provider, theme);
	}

	public enum Language {GLSL, GROOVY}
}
