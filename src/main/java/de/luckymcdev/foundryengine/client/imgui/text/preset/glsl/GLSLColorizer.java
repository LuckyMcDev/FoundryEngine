package de.luckymcdev.foundryengine.client.imgui.text.preset.glsl;

import de.luckymcdev.foundryengine.client.imgui.text.color.AbstractBaseColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorGlyph;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class GLSLColorizer extends AbstractBaseColorizer {
	public static final Color COLOR_DEFAULT = Color.ofABGR(0xFFC6B7A9);
	public static final Color COLOR_KEYWORD = Color.ofABGR(0xFF8585FC);
	public static final Color COLOR_BUILT_IN_TYPE = Color.ofABGR(0xFF71C0F6);
	public static final Color COLOR_STRING = Color.ofABGR(0xFF74DBE6);
	public static final Color COLOR_PREPROCESSOR = Color.ofABGR(0xFF7426F9);
	public static final Color COLOR_NUMBER = Color.ofABGR(0xFF5DACA2);
	public static final Color COLOR_FUNCTION_CALL = Color.ofABGR(0xFF40A885);
	public static final Color COLOR_FUNCTION_NAME = Color.ofABGR(0xFFBA769A);
	public static final Color COLOR_FUNCTION_PARAM = Color.ofABGR(0xFF70D9BD);
	public static final Color COLOR_BUILT_IN_VAR = Color.ofABGR(0xFFCBE7A3);
	public static final Color COLOR_BUILT_IN_CONST = Color.ofABGR(0xFF86D9B9);
	public static final Color COLOR_USER_IDENT = Color.ofABGR(0xFFC79565);
	public static final Color COLOR_OPERATOR = Color.ofABGR(0xFFC6B7A9);
	public static final Color COLOR_COMMENT = Color.ofABGR(0xFF888888);
	public static final Color COLOR_COMMENT_MULTI = Color.ofABGR(0xFF557962);

	public static final Set<String> KEYWORDS = new HashSet<>(Arrays.asList(
		"in", "out", "inout", "uniform", "attribute", "varying",
		"for", "while", "do", "if", "else", "switch", "case", "default",
		"break", "continue", "return", "discard", "struct", "const"
	));

	public static final Set<String> BUILT_IN_TYPES = new HashSet<>(Arrays.asList(
		"void", "bool", "int", "uint", "float", "double",
		"vec2", "vec3", "vec4", "dvec2", "dvec3", "dvec4",
		"bvec2", "bvec3", "bvec4", "ivec2", "ivec3", "ivec4",
		"uvec2", "uvec3", "uvec4", "mat2", "mat3", "mat4",
		"mat2x2", "mat2x3", "mat2x4", "mat3x2", "mat3x3", "mat3x4",
		"mat4x2", "mat4x3", "mat4x4", "dmat2", "dmat3", "dmat4",
		"sampler2D", "sampler3D", "samplerCube", "sampler2DArray",
		"sampler1D", "sampler2DShadow", "samplerCubeShadow",
		"isampler2D", "isampler3D", "isamplerCube",
		"usampler2D", "usampler3D", "usamplerCube"
	));

	public static final Set<String> BUILT_IN_FUNCTIONS = new HashSet<>(Arrays.asList(
		"radians", "degrees", "sin", "cos", "tan", "asin", "acos", "atan", "atan2",
		"pow", "exp", "exp2", "log", "log2", "sqrt", "inversesqrt",
		"abs", "sign", "floor", "ceil", "fract", "mod", "modf",
		"min", "max", "clamp", "mix", "step", "smoothstep",
		"length", "distance", "dot", "cross", "normalize", "faceforward",
		"reflect", "refract", "matrixCompMult", "outerProduct", "transpose",
		"determinant", "inverse",
		"lessThan", "lessThanEqual", "greaterThan", "greaterThanEqual",
		"equal", "notEqual", "any", "all", "not",
		"texture", "texture2D", "textureCube", "texture3D", "textureLod",
		"textureProj", "textureGrad", "textureSize", "textureOffset",
		"dFdx", "dFdy", "fwidth",
		"EmitVertex", "EndPrimitive", "barrier"
	));

	public static final Set<String> BUILT_IN_VARIABLES = new HashSet<>(Arrays.asList(
		"gl_Position", "gl_PointSize", "gl_ClipDistance", "gl_VertexID", "gl_InstanceID",
		"gl_FragCoord", "gl_FragColor", "gl_FragData", "gl_FragDepth",
		"gl_FrontFacing", "gl_PointCoord", "gl_Normal", "gl_Vertex",
		"gl_ModelViewMatrix", "gl_ProjectionMatrix", "gl_ModelViewProjectionMatrix",
		"gl_NormalMatrix", "gl_in", "gl_out"
	));

	public static final Set<String> BUILT_IN_CONSTANTS = new HashSet<>(Arrays.asList(
		"true", "false", "gl_MaxVertexAttribs", "gl_MaxVertexUniformComponents",
		"GL_TRUE", "GL_FALSE", "GL_POINTS", "GL_LINES", "GL_TRIANGLES"
	));

	public static final Set<String> QUALIFIERS = new HashSet<>(Arrays.asList(
		"layout", "centroid", "flat", "smooth", "noperspective", "precision",
		"highp", "mediump", "lowp", "invariant", "patch", "sample",
		"coherent", "volatile", "restrict", "readonly", "writeonly", "shared", "buffer"
	));
	private static final Pattern CODE_PATTERN = Pattern.compile(
		"(\"[^\"]*\")"
			+ "|([a-zA-Z_][a-zA-Z0-9_]*)\\s*(?=\\()"
			+ "|([a-zA-Z_][a-zA-Z0-9_]*)"
			+ "|(\\d+\\.?\\d*[fFuU]?(?:[eE][+-]?\\d+)?)"
			+ "|([+\\-*/%=<>!&|^~?:]+)"
			+ "|([()\\[\\]{}.,;])"
			+ "|(\\s+)"
	);
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
		Pattern structPat = Pattern.compile("struct\\s+([a-zA-Z_][a-zA-Z0-9_]*)");
		Pattern funcPat = Pattern.compile(
			"(?:void|float|int|vec[234]|mat[234]|bool)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");
		for (List<EditorGlyph> line : lines) {
			String t = glyphsToString(line).trim();
			if (t.isEmpty() || t.startsWith("//")) {
				continue;
			}
			Matcher m = structPat.matcher(t);
			if (m.find()) {
				userDefinedTypes.add(m.group(1));
			}
			m = funcPat.matcher(t);
			if (m.find()) {
				String fn = m.group(1);
				if (!BUILT_IN_FUNCTIONS.contains(fn)) {
					userDefinedFunctions.add(fn);
				}
			}
		}
	}

	@Override
	protected void colorizeLineImpl(List<EditorGlyph> line, int lineIdx, String text) {
		String trimmed = text.stripLeading();
		if (trimmed.startsWith("#")) {
			for (EditorGlyph g : line) {
				g.color = COLOR_PREPROCESSOR;
			}
			return;
		}

		int commentAt = findLineCommentStart(text);
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
		if (m.group(1) != null) {
			return COLOR_STRING;
		}

		if (m.group(2) != null) {
			String tok = m.group(2);
			return BUILT_IN_FUNCTIONS.contains(tok) ? COLOR_FUNCTION_CALL : COLOR_FUNCTION_NAME;
		}

		if (m.group(3) != null) {
			String tok = m.group(3);
			if (KEYWORDS.contains(tok)) {
				return COLOR_KEYWORD;
			}
			if (BUILT_IN_TYPES.contains(tok)) {
				return COLOR_BUILT_IN_TYPE;
			}
			if (BUILT_IN_VARIABLES.contains(tok)) {
				return COLOR_BUILT_IN_VAR;
			}
			if (BUILT_IN_CONSTANTS.contains(tok)) {
				return COLOR_BUILT_IN_CONST;
			}
			if (QUALIFIERS.contains(tok)) {
				return COLOR_KEYWORD;
			}
			if (userDefinedTypes.contains(tok)) {
				return COLOR_BUILT_IN_TYPE;
			}
			if (userDefinedFunctions.contains(tok)) {
				return COLOR_FUNCTION_NAME;
			}
			return COLOR_USER_IDENT;
		}

		if (m.group(4) != null) {
			return COLOR_NUMBER;
		}
		if (m.group(5) != null) {
			return COLOR_OPERATOR;
		}
		if (m.group(6) != null) {
			return COLOR_OPERATOR;
		}
		return COLOR_DEFAULT;
	}
}