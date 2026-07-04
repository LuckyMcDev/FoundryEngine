package de.luckymcdev.foundryengine.client.imgui.text.preset.groovy;

import de.luckymcdev.foundryengine.client.imgui.text.color.AbstractBaseColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GroovyColorizer extends AbstractBaseColorizer {
	public static final Color COLOR_DEFAULT = Color.ofABGR(0xFFC6B7A9);
	public static final Color COLOR_KEYWORD = Color.ofABGR(0xFF8585FC);
	public static final Color COLOR_BUILT_IN_TYPE = Color.ofABGR(0xFF71C0F6);
	public static final Color COLOR_STRING = Color.ofABGR(0xFF74DBE6);
	public static final Color COLOR_GSTRING = Color.ofABGR(0xFFD4A574);
	public static final Color COLOR_NUMBER = Color.ofABGR(0xFF5DACA2);
	public static final Color COLOR_FUNCTION_CALL = Color.ofABGR(0xFF40A885);
	public static final Color COLOR_FUNCTION_NAME = Color.ofABGR(0xFFBA769A);
	public static final Color COLOR_ANNOTATION = Color.ofABGR(0xFFE0C080);
	public static final Color COLOR_OPERATOR = Color.ofABGR(0xFFC6B7A9);
	public static final Color COLOR_COMMENT = Color.ofABGR(0xFF888888);
	public static final Color COLOR_COMMENT_MULTI = Color.ofABGR(0xFF557962);
	public static final Color COLOR_USER_IDENT = Color.ofABGR(0xFFC79565);

	public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		"if", "else", "for", "while", "do", "switch", "case", "default",
		"break", "continue", "return", "throw", "try", "catch", "finally",
		"new", "class", "interface", "enum", "extends", "implements",
		"import", "package", "as", "in", "it", "this", "super",
		"abstract", "final", "native", "private", "protected", "public",
		"static", "synchronized", "transient", "volatile", "assert",
		"var", "def", "trait", "mixin", "record", "sealed", "non-sealed",
		"permits", "assert", "with", "yield"
	));

	public static final Set<String> BUILT_IN_TYPES = new HashSet<>(Arrays.asList(
		"void", "boolean", "byte", "char", "short", "int", "long",
		"float", "double", "String", "Object", "Class",
		"List", "Set", "Map", "Queue", "Collection", "ArrayList",
		"LinkedList", "HashSet", "TreeSet", "HashMap", "TreeMap",
		"LinkedHashMap", "LinkedHashSet",
		"BigInteger", "BigDecimal",
		"boolean[]", "byte[]", "char[]", "short[]", "int[]", "long[]",
		"float[]", "double[]", "String[]", "Object[]",
		"Range", "IntRange", "EmptyRange",
		"Closure", "Closure<T>", "GString", "Writable"
	));

	public static final Set<String> GDK_METHODS = new HashSet<>(Arrays.asList(
		"each", "eachWithIndex", "collect", "find", "findAll", "findResult",
		"grep", "inject", "every", "any", "min", "max", "sum",
		"count", "size", "contains", "containsAll",
		"sort", "reverse", "unique", "flatten", "transpose",
		"groupBy", "countBy", "split", "collate", "chunk",
		"with", "withCloseable", "tap", "also",
		"print", "println", "printf", "sprintf",
		"asBoolean", "asType", "asWritable",
		"inspect", "dump", "toString",
		"isCase", "respondsTo", "hasProperty",
		"metaClass", "property", "getAt", "putAt",
		"leftShift", "rightShift", "rightShiftUnsigned",
		"multiply", "div", "mod", "power",
		"plus", "minus", "next", "previous",
		"upto", "downto", "step", "times",
		"use", "withTraits", "mixin"
	));

	public static final Set<String> BUILT_IN_CONSTANTS = new HashSet<>(Arrays.asList(
		"true", "false", "null", "this", "super"
	));

	private static final Pattern CODE_PATTERN = Pattern.compile(
		"(/\\*.*?\\*/)"                                      // group 1 - multi-line comment
			+ "|(\"\"\"(?:[^\"\\\\]|\\\\.)*\"\"\")"          // group 2 - triple-quoted string
			+ "|('''(?:[^'\\\\]|\\\\.)*''')"                // group 3 - triple-single-quoted string
			+ "|(\"(?:[^\"\\\\$]|\\\\.|\\$\\{.*?\\})*\")"  // group 4 - GString (with interpolation)
			+ "|('(?:[^'\\\\]|\\\\.)*')"                    // group 5 - single-quoted string
			+ "|(@[a-zA-Z_][a-zA-Z0-9_]*)"                  // group 6 - annotation
			+ "|([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()"       // group 7 - call before '('
			+ "|([a-zA-Z_][a-zA-Z0-9_]*)"                   // group 8 - plain identifier
			+ "|(0[xX][0-9a-fA-F]+[Ll]?)"                   // group 9 - hex literal
			+ "|(\\d+\\.?\\d*[fFdDgG]?(?:[eE][+-]?\\d+)?)" // group 10 - decimal
			+ "|([+\\-*/%=<>!&|^~?:]+)"                     // group 11 - operator
			+ "|([()\\[\\]{}.,;@])"                          // group 12 - punctuation
			+ "|(\\s+)"                                      // group 13 - whitespace
	);

	private static final Pattern SLASH_COMMENT = Pattern.compile("//.*$", Pattern.MULTILINE);

	public final Set<String> userDefinedTypes = new HashSet<>();
	public final Set<String> userDefinedFunctions = new HashSet<>();

	@Override
	public Color getDefaultColor() {
		return COLOR_DEFAULT;
	}

	@Override
	protected void analyzeDocument(List<List<EditorGlyph>> lines) {
		userDefinedTypes.clear();
		userDefinedFunctions.clear();
		Pattern classPat = Pattern.compile("(?:class|interface|trait|enum|record)\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
		Pattern funcPat = Pattern.compile(
			"(?:(?:def|void|int|long|float|double|boolean|char|byte|short|String|Object" +
				"|[a-zA-Z_][a-zA-Z0-9_]*<[^>]*>|[a-zA-Z_][a-zA-Z0-9_]*))" +
				"\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
		for (List<EditorGlyph> line : lines) {
			String t = glyphsToString(line);
			if (t.isBlank()) continue;
			String trimmed = t.stripLeading();
			if (trimmed.startsWith("//") || trimmed.startsWith("*")) continue;

			Matcher cm = classPat.matcher(t);
			if (cm.find()) {
				userDefinedTypes.add(cm.group(1));
			}
			Matcher fm = funcPat.matcher(t);
			if (fm.find()) {
				String fn = fm.group(1);
				if (!KEYWORDS.contains(fn) && !GDK_METHODS.contains(fn)) {
					userDefinedFunctions.add(fn);
				}
			}
		}
	}

	@Override
	protected void colorizeLineImpl(List<EditorGlyph> line, int lineIdx, String text) {
		Matcher slm = SLASH_COMMENT.matcher(text);
		int commentAt = slm.find() ? slm.start() : -1;
		String codePart = commentAt >= 0 ? text.substring(0, commentAt) : text;

		colorizeCode(line, codePart, 0);

		if (commentAt >= 0) {
			for (int i = commentAt; i < line.size(); i++) {
				line.get(i).color = COLOR_COMMENT;
			}
		}
	}

	private void colorizeCode(List<EditorGlyph> line, String code, int offset) {
		Matcher m = CODE_PATTERN.matcher(code);
		int idx = 0;
		while (m.find()) {
			while (idx < m.start() && offset + idx < line.size()) {
				line.get(offset + idx++).color = COLOR_DEFAULT;
			}
			Color color = resolveColor(m);
			for (int i = m.start(); i < m.end() && offset + i < line.size(); i++) {
				line.get(offset + i).color = color;
			}
			idx = m.end();
		}
		while (idx < code.length() && offset + idx < line.size()) {
			line.get(offset + idx++).color = COLOR_DEFAULT;
		}
	}

	private Color resolveColor(Matcher m) {
		if (m.group(1) != null) return COLOR_COMMENT_MULTI;
		if (m.group(2) != null) return COLOR_STRING;
		if (m.group(3) != null) return COLOR_STRING;
		if (m.group(4) != null) return COLOR_GSTRING;
		if (m.group(5) != null) return COLOR_STRING;
		if (m.group(6) != null) return COLOR_ANNOTATION;

		if (m.group(7) != null) {
			String tok = m.group(7);
			if (GDK_METHODS.contains(tok)) return COLOR_FUNCTION_CALL;
			if (userDefinedFunctions.contains(tok)) return COLOR_FUNCTION_NAME;
			return COLOR_FUNCTION_NAME;
		}

		if (m.group(8) != null) {
			String tok = m.group(8);
			if (KEYWORDS.contains(tok)) return COLOR_KEYWORD;
			if (BUILT_IN_TYPES.contains(tok)) return COLOR_BUILT_IN_TYPE;
			if (BUILT_IN_CONSTANTS.contains(tok)) return COLOR_BUILT_IN_TYPE;
			if (userDefinedTypes.contains(tok)) return COLOR_BUILT_IN_TYPE;
			if (userDefinedFunctions.contains(tok)) return COLOR_FUNCTION_NAME;
			return COLOR_USER_IDENT;
		}

		if (m.group(9) != null) return COLOR_NUMBER;
		if (m.group(10) != null) return COLOR_NUMBER;
		if (m.group(11) != null) return COLOR_OPERATOR;
		if (m.group(12) != null) return COLOR_OPERATOR;
		return COLOR_DEFAULT;
	}
}
