package de.luckymcdev.foundryengine.client.imgui.text.preset.toml;

import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.AutocompleteItem;
import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.IAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorCoordinates;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.ArrayList;
import java.util.List;

public final class TomlAutocompleteProvider implements IAutocompleteProvider {

	private static final Color BOOL_COLOR = TomlColorizer.COLOR_BOOLEAN;

	private final TomlColorizer colorizer;

	public TomlAutocompleteProvider(TomlColorizer colorizer) {
		this.colorizer = colorizer;
	}

	@Override
	public List<AutocompleteItem> getCandidates(String prefix, List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		List<AutocompleteItem> items = new ArrayList<>();
		String lower = prefix.toLowerCase();
		for (String b : new String[]{"true", "false"}) {
			if (b.startsWith(lower)) {
				items.add(new AutocompleteItem(b, "constant", null, BOOL_COLOR));
			}
		}
		return items;
	}

	@Override
	public boolean shouldSuppress(String prefix, List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		return false;
	}

	@Override
	public int minPrefixLength() {
		return 2;
	}

	@Override
	public boolean appendParens() {
		return false;
	}
}