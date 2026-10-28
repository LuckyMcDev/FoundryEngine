package de.luckymcdev.foundryengine.imgui.client.text.autocomplete;

import de.luckymcdev.foundryengine.imgui.client.text.editor.EditorCoordinates;
import de.luckymcdev.foundryengine.imgui.client.text.editor.EditorGlyph;

import java.util.List;

public interface IAutocompleteProvider {
	List<AutocompleteItem> getCandidates(String prefix,
	                                     List<List<EditorGlyph>> lines,
	                                     EditorCoordinates cursor);

	// Return true to suppress the popup (e.g. after a dot for swizzles).
	boolean shouldSuppress(String prefix,
	                       List<List<EditorGlyph>> lines,
	                       EditorCoordinates cursor);

	default int minPrefixLength() {
		return 2;
	}

	// If true, accepted function completions get "()" appended.
	default boolean appendParens() {
		return true;
	}
}