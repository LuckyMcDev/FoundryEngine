package de.luckymcdev.foundryengine.client.imgui.text.editor;

import de.luckymcdev.foundryengine.common.util.color.Color;
import imgui.ImGui;

import java.util.List;

// All colours stored as Color (AARRGGBB). The static factory methods
// accept raw ABGR packed ints for compatibility with existing themes.
public final class EditorTheme {
	public static final List<String> THEME_NAMES = List.of(
		"dark", "monokai", "light", "dracula", "nord", "solarizedDark", "solarizedLight", "oneDark", "githubLight"
	);
	public final Color backgroundColor;
	public final Color currentLineColor;
	public final Color selectionColor;
	public final Color cursorColor;
	public final int cursorBlinkMs; // 0 = no blink
	public final float cursorWidth;
	public final Color lineNumberColor;
	public final Color gutterSeparatorColor;
	public final Color errorColor;
	public final Color errorMarkerColor;
	public final float gutterPaddingRight;
	public final float hScrollbarHeight;
	public final float lineSpacing;
	public final int tabSize;

	private EditorTheme(Builder b) {
		this.backgroundColor = b.backgroundColor;
		this.currentLineColor = b.currentLineColor;
		this.selectionColor = b.selectionColor;
		this.cursorColor = b.cursorColor;
		this.cursorBlinkMs = b.cursorBlinkMs;
		this.cursorWidth = b.cursorWidth;
		this.lineNumberColor = b.lineNumberColor;
		this.gutterSeparatorColor = b.gutterSeparatorColor;
		this.errorColor = b.errorColor;
		this.errorMarkerColor = b.errorMarkerColor;
		this.gutterPaddingRight = b.gutterPaddingRight;
		this.hScrollbarHeight = b.hScrollbarHeight;
		this.lineSpacing = b.lineSpacing;
		this.tabSize = b.tabSize;
	}

	public static int toImU32(Color c) {
		return ImGui.getColorU32(c.r(), c.g(), c.b(), c.a());
	}

	public static Builder dark() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xB2120D0A))
			.withCurrentLineColor(Color.ofABGR(0x18FFFFFF))
			.withSelectionColor(Color.ofABGR(0x804060C0))
			.withCursorColor(Color.ofABGR(0xCCE0E0E0))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF858585))
			.withGutterSeparatorColor(Color.ofABGR(0xFF2A2A2A))
			.withErrorColor(Color.ofABGR(0xFF3333FF))
			.withErrorMarkerColor(Color.ofABGR(0xFF5555FF))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static Builder monokai() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xE6272822))
			.withCurrentLineColor(Color.ofABGR(0x20FFFFFF))
			.withSelectionColor(Color.ofABGR(0x8049483E))
			.withCursorColor(Color.ofABGR(0xCCF8F8F2))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF75715E))
			.withGutterSeparatorColor(Color.ofABGR(0xFF3E3D32))
			.withErrorColor(Color.ofABGR(0xFF3333FF))
			.withErrorMarkerColor(Color.ofABGR(0xFF5555FF))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static Builder light() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xFFFAFAFA))
			.withCurrentLineColor(Color.ofABGR(0x18000000))
			.withSelectionColor(Color.ofABGR(0x18000000))
			.withCursorColor(Color.ofABGR(0xCC1A1A1A))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF999999))
			.withGutterSeparatorColor(Color.ofABGR(0xFFDDDDDD))
			.withErrorColor(Color.ofABGR(0xFF3333FF))
			.withErrorMarkerColor(Color.ofABGR(0xFF5555FF))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static Builder dracula() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xFF36272A))
			.withCurrentLineColor(Color.ofABGR(0x2044475A))
			.withSelectionColor(Color.ofABGR(0x8044475A))
			.withCursorColor(Color.ofABGR(0xCCF8F8F2))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF6272A4))
			.withGutterSeparatorColor(Color.ofABGR(0xFF44475A))
			.withErrorColor(Color.ofABGR(0xFF3333FF))
			.withErrorMarkerColor(Color.ofABGR(0xFF5555FF))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static Builder nord() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xFF40342E))
			.withCurrentLineColor(Color.ofABGR(0x3052423B))
			.withSelectionColor(Color.ofABGR(0x60D0C088))
			.withCursorColor(Color.ofABGR(0xCCF4EFEC))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF6A564C))
			.withGutterSeparatorColor(Color.ofABGR(0xFF5E4C43))
			.withErrorColor(Color.ofABGR(0xFF3333FF))
			.withErrorMarkerColor(Color.ofABGR(0xFF5555FF))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static Builder solarizedDark() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xFF362B00))
			.withCurrentLineColor(Color.ofABGR(0x18FDF6E3))
			.withSelectionColor(Color.ofABGR(0x60586E75))
			.withCursorColor(Color.ofABGR(0xCC839496))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF586E75))
			.withGutterSeparatorColor(Color.ofABGR(0xFF073642))
			.withErrorColor(Color.ofABGR(0xFF3333FF))
			.withErrorMarkerColor(Color.ofABGR(0xFF5555FF))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static Builder solarizedLight() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xFFE3F6FD))
			.withCurrentLineColor(Color.ofABGR(0x18000000))
			.withSelectionColor(Color.ofABGR(0x5093A1A1))
			.withCursorColor(Color.ofABGR(0xCC657B83))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF93A1A1))
			.withGutterSeparatorColor(Color.ofABGR(0xFFEEE8D5))
			.withErrorColor(Color.ofABGR(0xFF3333FF))
			.withErrorMarkerColor(Color.ofABGR(0xFF5555FF))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static Builder oneDark() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xFF342C28))
			.withCurrentLineColor(Color.ofABGR(0x202C313C))
			.withSelectionColor(Color.ofABGR(0x603E4450))
			.withCursorColor(Color.ofABGR(0xCCABB2BF))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF4B5263))
			.withGutterSeparatorColor(Color.ofABGR(0xFF3B4048))
			.withErrorColor(Color.ofABGR(0xFF3333FF))
			.withErrorMarkerColor(Color.ofABGR(0xFF5555FF))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static Builder githubLight() {
		return new Builder()
			.withBackgroundColor(Color.ofABGR(0xFFFFFFFF))
			.withCurrentLineColor(Color.ofABGR(0x14000000))
			.withSelectionColor(Color.ofABGR(0x500489E0))
			.withCursorColor(Color.ofABGR(0xCC24292E))
			.withCursorBlinkMs(0)
			.withCursorWidth(2.0f)
			.withLineNumberColor(Color.ofABGR(0xFF959DA5))
			.withGutterSeparatorColor(Color.ofABGR(0xFFE1E4E8))
			.withGutterPaddingRight(10.0f)
			.withHScrollbarHeight(10.0f)
			.withLineSpacing(1.0f)
			.withTabSize(4);
	}

	public static EditorTheme getThemeByName(String name) {
		if (!THEME_NAMES.contains(name)) {
			return EditorTheme.dark().build();
		}

		return switch (name) {
			case "dark" -> EditorTheme.dark().build();
			case "monokai" -> EditorTheme.monokai().build();
			case "light" -> EditorTheme.light().build();
			case "dracula" -> EditorTheme.dracula().build();
			case "nord" -> EditorTheme.nord().build();
			case "solarizedDark" -> EditorTheme.solarizedDark().build();
			case "solarizedLight" -> EditorTheme.solarizedLight().build();
			case "oneDark" -> EditorTheme.oneDark().build();
			case "githubLight" -> EditorTheme.githubLight().build();
			default -> EditorTheme.dark().build();
		};
	}

	public static String getAvailableThemeNames() {
		StringBuilder names = new StringBuilder();
		for (String theme : THEME_NAMES) {
			if (!names.isEmpty()) {
				names.append(", ");
			}
			names.append(theme);
		}
		return names.toString();
	}

	public EditorTheme withCursorColor(Color c) {
		return toBuilder().withCursorColor(c).build();
	}

	public EditorTheme withSelectionColor(Color c) {
		return toBuilder().withSelectionColor(c).build();
	}

	public EditorTheme withBackgroundColor(Color c) {
		return toBuilder().withBackgroundColor(c).build();
	}

	public EditorTheme withLineNumberColor(Color c) {
		return toBuilder().withLineNumberColor(c).build();
	}

	public EditorTheme withErrorColor(Color c) {
		return toBuilder().withErrorColor(c).build();
	}

	public EditorTheme withErrorMarkerColor(Color c) {
		return toBuilder().withErrorMarkerColor(c).build();
	}

	public EditorTheme withLineSpacing(float s) {
		return toBuilder().withLineSpacing(s).build();
	}

	public EditorTheme withTabSize(int t) {
		return toBuilder().withTabSize(t).build();
	}

	private Builder toBuilder() {
		Builder b = new Builder();
		b.backgroundColor = backgroundColor;
		b.currentLineColor = currentLineColor;
		b.selectionColor = selectionColor;
		b.cursorColor = cursorColor;
		b.cursorBlinkMs = cursorBlinkMs;
		b.cursorWidth = cursorWidth;
		b.lineNumberColor = lineNumberColor;
		b.gutterSeparatorColor = gutterSeparatorColor;
		b.errorColor = errorColor;
		b.errorMarkerColor = errorMarkerColor;
		b.gutterPaddingRight = gutterPaddingRight;
		b.hScrollbarHeight = hScrollbarHeight;
		b.lineSpacing = lineSpacing;
		b.tabSize = tabSize;
		return b;
	}

	public static final class Builder {
		private Color backgroundColor = Color.ofABGR(0xB2120D0A);
		private Color currentLineColor = Color.ofABGR(0x18FFFFFF);
		private Color selectionColor = Color.ofABGR(0x804060C0);
		private Color cursorColor = Color.ofABGR(0xCCE0E0E0);
		private int cursorBlinkMs = 0;
		private float cursorWidth = 2.0f;
		private Color lineNumberColor = Color.ofABGR(0xFF858585);
		private Color gutterSeparatorColor = Color.ofABGR(0xFF2A2A2A);
		private Color errorColor = Color.ofABGR(0xFF3333FF);
		private Color errorMarkerColor = Color.ofABGR(0xFF5555FF);
		private float gutterPaddingRight = 10.0f;
		private float hScrollbarHeight = 10.0f;
		private float lineSpacing = 1.0f;
		private int tabSize = 4;

		public Builder withBackgroundColor(Color c) {
			backgroundColor = c;
			return this;
		}

		public Builder withCurrentLineColor(Color c) {
			currentLineColor = c;
			return this;
		}

		public Builder withSelectionColor(Color c) {
			selectionColor = c;
			return this;
		}

		public Builder withCursorColor(Color c) {
			cursorColor = c;
			return this;
		}

		public Builder withCursorBlinkMs(int ms) {
			cursorBlinkMs = ms;
			return this;
		}

		public Builder withCursorWidth(float w) {
			cursorWidth = w;
			return this;
		}

		public Builder withLineNumberColor(Color c) {
			lineNumberColor = c;
			return this;
		}

		public Builder withGutterSeparatorColor(Color c) {
			gutterSeparatorColor = c;
			return this;
		}

		public Builder withErrorColor(Color c) {
			errorColor = c;
			return this;
		}

		public Builder withErrorMarkerColor(Color c) {
			errorMarkerColor = c;
			return this;
		}

		public Builder withGutterPaddingRight(float p) {
			gutterPaddingRight = p;
			return this;
		}

		public Builder withHScrollbarHeight(float h) {
			hScrollbarHeight = h;
			return this;
		}

		public Builder withLineSpacing(float s) {
			lineSpacing = s;
			return this;
		}

		public Builder withTabSize(int t) {
			tabSize = t;
			return this;
		}

		public EditorTheme build() {
			return new EditorTheme(this);
		}
	}
}