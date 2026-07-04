package de.luckymcdev.foundryengine.client.imgui.text.preset.groovy;

import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.AutocompleteItem;
import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.FunctionSignature;
import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.IAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorCoordinates;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class GroovyAutocompleteProvider implements IAutocompleteProvider {
	private static final List<FunctionSignature> SIGS = Arrays.asList(
		new FunctionSignature("each", "void", "Closure closure"),
		new FunctionSignature("eachWithIndex", "void", "Closure closure"),
		new FunctionSignature("collect", "Collection", "Closure closure"),
		new FunctionSignature("find", "Object", "Closure closure"),
		new FunctionSignature("findAll", "Collection", "Closure closure"),
		new FunctionSignature("inject", "Object", "Object initialValue, Closure closure"),
		new FunctionSignature("grep", "Collection", "Object filter"),
		new FunctionSignature("every", "boolean", "Closure closure"),
		new FunctionSignature("any", "boolean", "Closure closure"),
		new FunctionSignature("sort", "List", "Closure closure"),
		new FunctionSignature("groupBy", "Map", "Closure closure"),
		new FunctionSignature("count", "int", "Closure closure"),
		new FunctionSignature("with", "Object", "Closure closure"),
		new FunctionSignature("tap", "Object", "Closure closure"),
		new FunctionSignature("times", "void", "Closure closure"),
		new FunctionSignature("upto", "void", "Number to, Closure closure"),
		new FunctionSignature("downto", "void", "Number to, Closure closure"),
		new FunctionSignature("step", "void", "Number to, Number step, Closure closure"),
		new FunctionSignature("println", "void", "Object message"),
		new FunctionSignature("printf", "void", "String format, Object... args"),
		new FunctionSignature("sprintf", "String", "String format, Object... args"),
		new FunctionSignature("asType", "T", "Class<T> type"),
		new FunctionSignature("isCase", "boolean", "Object switchValue"),
		new FunctionSignature("dump", "String", ""),
		new FunctionSignature("inspect", "String", "")
	);

	public static AutocompleteItem item(String text, String type, String sig, Color color) {
		return new AutocompleteItem(text, type, sig, color);
	}

	private static String sigFor(String name) {
		for (FunctionSignature s : SIGS) {
			if (s.name().equals(name)) {
				return s.format();
			}
		}
		return "";
	}

	@Override
	public int minPrefixLength() {
		return 2;
	}

	@Override
	public boolean appendParens() {
		return true;
	}

	@Override
	public boolean shouldSuppress(String prefix, List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		if (cursor.line < lines.size()) {
			List<EditorGlyph> line = lines.get(cursor.line);
			int wordStart = cursor.column - prefix.length();
			return wordStart > 0 && line.get(wordStart - 1).ch == '.';
		}
		return false;
	}

	@Override
	public List<AutocompleteItem> getCandidates(String prefix, List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		String lower = prefix.toLowerCase();
		List<AutocompleteItem> out = new ArrayList<>();

		for (String kw : GroovyColorizer.KEYWORDS) {
			if (kw.toLowerCase().startsWith(lower)) {
				out.add(item(kw, "keyword", "", GroovyColorizer.COLOR_KEYWORD));
			}
		}

		for (String t : GroovyColorizer.BUILT_IN_TYPES) {
			if (t.toLowerCase().startsWith(lower)) {
				out.add(item(t, "type", "", GroovyColorizer.COLOR_BUILT_IN_TYPE));
			}
		}

		for (String fn : GroovyColorizer.GDK_METHODS) {
			if (fn.toLowerCase().startsWith(lower)) {
				out.add(item(fn, "function", sigFor(fn), GroovyColorizer.COLOR_FUNCTION_CALL));
			}
		}

		for (String c : GroovyColorizer.BUILT_IN_CONSTANTS) {
			if (c.toLowerCase().startsWith(lower)) {
				out.add(item(c, "constant", "", GroovyColorizer.COLOR_BUILT_IN_TYPE));
			}
		}

		return out;
	}
}
