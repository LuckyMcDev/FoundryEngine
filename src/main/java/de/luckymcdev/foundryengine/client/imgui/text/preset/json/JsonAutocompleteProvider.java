package de.luckymcdev.foundryengine.client.imgui.text.preset.json;

import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.AutocompleteItem;
import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.IAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorCoordinates;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.ArrayList;
import java.util.List;

public final class JsonAutocompleteProvider implements IAutocompleteProvider {

	private static final Color KEYWORD_COLOR = JsonColorizer.COLOR_KEYWORD;

	private final JsonColorizer colorizer;

	public JsonAutocompleteProvider(JsonColorizer colorizer) {
		this.colorizer = colorizer;
	}

	@Override
	public List<AutocompleteItem> getCandidates(String prefix, List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		List<AutocompleteItem> items = new ArrayList<>();
		String lower = prefix.toLowerCase();
		for (String kw : new String[]{"true", "false", "null"}) {
			if (kw.startsWith(lower)) {
				items.add(new AutocompleteItem(kw, "keyword", null, KEYWORD_COLOR));
			}
		}
		return items;
	}

	@Override
	public boolean shouldSuppress(String prefix, List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		return false;
	}

	@Override
	public boolean appendParens() {
		return false;
	}
}