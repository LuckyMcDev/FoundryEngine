package de.luckymcdev.foundryengine.client.imgui.text.preset.groovy;

import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.AutocompleteItem;
import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.FunctionSignature;
import de.luckymcdev.foundryengine.client.imgui.text.autocomplete.IAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorCoordinates;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;
import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class GroovyReflectionAutocompleteProvider implements IAutocompleteProvider {
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

	private static final Pattern DECL_TYPED = Pattern.compile(
		"^(?:public|private|protected|static|final|def|var|abstract|native|synchronized)\\s+" +
			"(\\w+(?:\\.\\w+)*(?:<[^>]*>)?)\\s+(\\w+)\\s*[=;].*$"
	);
	private static final Pattern DECL_DEF_NEW = Pattern.compile(
		"^(?:public|private|protected|static|final|def|var)?\\s*" +
			"def\\s+(\\w+)\\s*=\\s*new\\s+(\\w+(?:\\.\\w+)*)\\b.*$"
	);
	private static final Pattern DECL_IMPORT = Pattern.compile(
		"^import\\s+(\\w+(?:\\.\\w+)*)\\.(\\w+)\\s*$"
	);

	private final GroovyColorizer colorizer;
	private final URL[] scriptRoots;

	public GroovyReflectionAutocompleteProvider(GroovyColorizer colorizer, URL... scriptRoots) {
		this.colorizer = colorizer;
		this.scriptRoots = scriptRoots;
	}

	public GroovyReflectionAutocompleteProvider(GroovyColorizer colorizer) {
		this(colorizer, new URL[0]);
	}

	private static AutocompleteItem item(String text, String type, String sig, Color color) {
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

	static String[] findDotContext(List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		if (cursor.line >= lines.size() || cursor.column <= 0) {
			return null;
		}
		List<EditorGlyph> line = lines.get(cursor.line);
		int maxCol = Math.min(cursor.column, line.size());

		int dotPos = -1;
		int col = maxCol - 1;
		int parenDepth = 0;
		while (col >= 0) {
			char c = line.get(col).ch;
			if (c == ')') {
				parenDepth++;
				col--;
				continue;
			}
			if (c == '(') {
				if (parenDepth > 0) {
					parenDepth--;
					col--;
					continue;
				}
				break;
			}
			if (parenDepth > 0) {
				col--;
				continue;
			}
			if (Character.isWhitespace(c)) {
				break;
			}
			if (c == '.') {
				dotPos = col;
				break;
			}
			if (!Character.isLetterOrDigit(c) && c != '_') {
				break;
			}
			col--;
		}
		if (dotPos < 0) {
			return null;
		}

		int start = dotPos;
		parenDepth = 0;
		while (start > 0) {
			start--;
			char c = line.get(start).ch;
			if (c == ')') {
				parenDepth++;
			} else if (c == '(') {
				if (parenDepth > 0) {
					parenDepth--;
				} else {
					start++;
					break;
				}
			} else if (parenDepth == 0 && !Character.isLetterOrDigit(c) && c != '_' && c != '.') {
				start++;
				break;
			}
		}

		StringBuilder sb = new StringBuilder();
		for (int i = start; i < dotPos; i++) {
			sb.append(line.get(i).ch);
		}
		String expr = sb.toString().strip();

		return expr.isEmpty() ? null : new String[]{expr};
	}

	static String linesToString(List<List<EditorGlyph>> lines) {
		StringBuilder sb = new StringBuilder();
		for (var line : lines) {
			for (var glyph : line) {
				sb.append(glyph.ch);
			}
			sb.append('\n');
		}
		return sb.toString();
	}

	static List<AutocompleteItem> getMethodCandidates(String prefix, String expr, String fullCode, URL... scriptRoots) {
		if (expr == null || expr.isEmpty()) {
			return List.of();
		}

		Class<?> resolved = resolveExpressionType(expr, fullCode, scriptRoots);
		if (resolved == null) {
			return List.of();
		}

		return methodsForClass(prefix, resolved);
	}

	static Class<?> resolveExpressionType(String expr, String fullCode, URL... scriptRoots) {
		String[] parts = expr.split("\\.");
		Class<?> current = null;
		Map<String, String> vars = parseVariableTypes(fullCode);

		for (int i = 0; i < parts.length; i++) {
			String part = parts[i];
			boolean isMethod = part.endsWith("()");
			String name = isMethod ? part.substring(0, part.length() - 2) : part;

			if (current == null) {
				String varType = vars.get(name);
				if (varType != null) {
					current = resolveType(varType, fullCode, scriptRoots);
				} else {
					current = resolveType(name, fullCode, scriptRoots);
				}
			} else if (isMethod) {
				current = resolveMethodReturnType(current, name);
			} else {
				Class<?> viaField = resolveFieldType(current, name);
				Class<?> viaGetter = resolveMethodReturnType(current, "get" + Character.toUpperCase(name.charAt(0)) + name.substring(1));
				current = viaField != null ? viaField : viaGetter;
			}

			if (current == null) {
				return null;
			}
		}

		return current;
	}

	static Class<?> resolveMethodReturnType(Class<?> owner, String methodName) {
		for (Method m : owner.getMethods()) {
			if (m.getName().equals(methodName) && Modifier.isPublic(m.getModifiers())) {
				return m.getReturnType();
			}
		}
		return null;
	}

	static Class<?> resolveFieldType(Class<?> owner, String fieldName) {
		try {
			Field f = owner.getField(fieldName);
			return f.getType();
		} catch (NoSuchFieldException ignored) {
		}
		return null;
	}

	static Map<String, String> parseVariableTypes(String code) {
		Map<String, String> vars = new HashMap<>();
		String[] lines = code.split("\n");

		for (int i = 0; i < lines.length; i++) {
			String line = lines[i].strip();

			var m = DECL_TYPED.matcher(line);
			if (m.matches()) {
				String typeName = m.group(1);
				String varName = m.group(2);
				if (!"def".equals(typeName) && !"var".equals(typeName)) {
					vars.put(varName, typeName);
				}
			}

			m = DECL_DEF_NEW.matcher(line);
			if (m.matches()) {
				String varName = m.group(1);
				String typeName = m.group(2);
				vars.put(varName, typeName);
			}

			m = DECL_IMPORT.matcher(line);
			if (m.matches()) {
				String pkg = m.group(1);
				String cls = m.group(2);
				vars.put(cls, pkg + "." + cls);
			}
		}

		return vars;
	}

	static Class<?> resolveType(String typeName, String fullCode, URL... scriptRoots) {
		if (typeName == null || typeName.isEmpty()) {
			return null;
		}

		switch (typeName) {
			case "String":
				return String.class;
			case "boolean":
				return boolean.class;
			case "int":
				return int.class;
			case "long":
				return long.class;
			case "float":
				return float.class;
			case "double":
				return double.class;
			case "char":
				return char.class;
			case "byte":
				return byte.class;
			case "short":
				return short.class;
			case "void":
				return void.class;
			case "Object":
				return Object.class;
			case "Class":
				return Class.class;
		}

		try {
			return Class.forName(typeName, false, Thread.currentThread().getContextClassLoader());
		} catch (ClassNotFoundException ignored) {
		}

		if (!typeName.contains(".")) {
			String[] packages = {
				"net.minecraft",
				"net.minecraft.world",
				"net.minecraft.world.entity",
				"net.minecraft.world.entity.player",
				"net.minecraft.world.item",
				"net.minecraft.world.level",
				"net.minecraft.world.level.block",
				"net.minecraft.core",
				"net.minecraft.resources",
				"net.minecraft.server",
				"net.minecraft.server.level",
				"net.minecraft.network.chat",
				"net.neoforged.neoforge",
				"net.neoforged.bus.api",
				"de.luckymcdev.foundryengine",
				"de.luckymcdev.foundryengine.common",
				"de.luckymcdev.foundryengine.api",
				"java.lang",
				"java.util"
			};

			for (String pkg : packages) {
				try {
					return Class.forName(pkg + "." + typeName, false, Thread.currentThread().getContextClassLoader());
				} catch (ClassNotFoundException ignored) {
				}
			}
		}

		try (GroovyClassLoader gcl = new GroovyClassLoader(
			Thread.currentThread().getContextClassLoader(),
			new CompilerConfiguration())) {
			for (URL root : scriptRoots) {
				if (root != null) {
					gcl.addURL(root);
				}
			}
			return gcl.loadClass(typeName);
		} catch (Exception ignored) {
		}

		return null;
	}

	static List<AutocompleteItem> methodsForClass(String prefix, Class<?> clazz) {
		String lower = prefix.toLowerCase();
		List<AutocompleteItem> out = new ArrayList<>();

		for (Method m : clazz.getMethods()) {
			int mod = m.getModifiers();
			if (!Modifier.isPublic(mod)) {
				continue;
			}
			String name = m.getName();
			if (lower.isEmpty() || name.toLowerCase().startsWith(lower)) {
				String sig = buildSignature(m);
				out.add(item(name, "function", sig, GroovyColorizer.COLOR_FUNCTION_CALL));
			}
		}

		for (Field f : clazz.getFields()) {
			int mod = f.getModifiers();
			if (!Modifier.isPublic(mod)) {
				continue;
			}
			String name = f.getName();
			if (lower.isEmpty() || name.toLowerCase().startsWith(lower)) {
				out.add(item(name, "variable", f.getType().getSimpleName(), GroovyColorizer.COLOR_USER_IDENT));
			}
		}

		out.sort((a, b) -> a.text().compareToIgnoreCase(b.text()));
		return out;
	}

	private static String buildSignature(Method m) {
		StringBuilder sb = new StringBuilder();
		sb.append(m.getReturnType().getSimpleName()).append(" ");
		sb.append(m.getName()).append("(");
		var params = m.getParameters();
		for (int i = 0; i < params.length; i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(params[i].getType().getSimpleName()).append(" ").append(params[i].getName());
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int minPrefixLength() {
		return 0;
	}


	@Override
	public boolean shouldSuppress(String prefix, List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		boolean afterDot = findDotContext(lines, cursor) != null;
		return !afterDot && (prefix == null || prefix.isEmpty());
	}

	@Override
	public List<AutocompleteItem> getCandidates(String prefix, List<List<EditorGlyph>> lines, EditorCoordinates cursor) {
		String[] dotContext = findDotContext(lines, cursor);
		if (dotContext != null) {
			String expr = dotContext[0];
			if (expr != null && !expr.isEmpty()) {
				return getMethodCandidates(prefix, expr, linesToString(lines), scriptRoots);
			}
		}

		return getDefaultCandidates(prefix);
	}

	private List<AutocompleteItem> getDefaultCandidates(String prefix) {
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