package de.luckymcdev.foundryengine.common.util.color;

/**
 * Utility class for handling Colors.
 */
public class Color {
	public static final Color TRANSPARENT = new Color(0, 0, 0, 0);
	public static final Color WHITE = new Color(255, 255, 255);
	public static final Color LIGHT_GRAY = new Color(192, 192, 192);
	public static final Color GRAY = new Color(128, 128, 128);
	public static final Color DARK_GRAY = new Color(64, 64, 64);
	public static final Color BLACK = new Color(0, 0, 0);
	public static final Color RED = new Color(255, 0, 0);
	public static final Color PINK = new Color(255, 175, 175);
	public static final Color ORANGE = new Color(255, 200, 0);
	public static final Color YELLOW = new Color(255, 255, 0);
	public static final Color GREEN = new Color(0, 255, 0);
	public static final Color MAGENTA = new Color(255, 0, 255);
	public static final Color CYAN = new Color(0, 255, 255);
	public static final Color BLUE = new Color(0, 0, 255);
	public static final Color PURPLE = new Color(128, 0, 128);
	public static final Color VIOLET = new Color(238, 130, 238);
	public static final Color BROWN = new Color(139, 69, 19);
	public static final Color TAN = new Color(210, 180, 140);
	public static final Color BEIGE = new Color(245, 245, 220);
	public static final Color TEAL = new Color(0, 128, 128);
	public static final Color TURQUOISE = new Color(64, 224, 208);
	public static final Color GOLD = new Color(255, 215, 0);
	public static final Color SILVER = new Color(192, 192, 192);
	public static final Color SALMON = new Color(250, 128, 114);
	public static final Color CORAL = new Color(255, 127, 80);
	public static final Color NAVY = new Color(0, 0, 128);
	public static final Color MAROON = new Color(128, 0, 0);
	public static final Color OLIVE = new Color(128, 128, 0);
	public static final Color LIME = new Color(0, 255, 0);
	public static final Color AQUAMARINE = new Color(127, 255, 212);
	public static final Color CORNFLOWER_BLUE = new Color(100, 149, 237);
	public static final Color SLATE_BLUE = new Color(106, 90, 205);
	public static final Color HOT_PINK = new Color(255, 105, 180);
	public static final Color INDIGO = new Color(75, 0, 130);
	public static final Color CRIMSON = new Color(220, 20, 60);
	public static final Color FOREST_GREEN = new Color(34, 139, 34);

	public final float r;
	public final float g;
	public final float b;
	public final float a;

	public Color(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public Color(int r, int g, int b) {
		this(r, g, b, 255);
	}

	public Color(int r, int g, int b, int a) {
		this.r = r / 255.0f;
		this.g = g / 255.0f;
		this.b = b / 255.0f;
		this.a = a / 255.0f;
	}

	public Color(int argb) {
		this.a = ((argb >> 24) & 0xFF) / 255.0f;
		this.r = ((argb >> 16) & 0xFF) / 255.0f;
		this.g = ((argb >> 8) & 0xFF) / 255.0f;
		this.b = (argb & 0xFF) / 255.0f;
	}

	public Color(float[] floats) {
		this(floats[0], floats[1], floats[2], 255);
	}


	public static Color hsb(float hue, float saturation, float brightness, int alpha) {
		if (saturation <= 0.0F) {
			int c = (int) (brightness * 255.0F + 0.5F);
			return of(alpha, c, c, c);
		}

		int r = 0;
		int g = 0;
		int b = 0;
		float h = (hue - (float) Math.floor(hue)) * 6.0F;
		float f = h - (float) Math.floor(h);
		float p = brightness * (1.0F - saturation);
		float q = brightness * (1.0F - saturation * f);
		float t = brightness * (1.0F - saturation * (1.0F - f));

		switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0F + 0.5F);
				g = (int) (t * 255.0F + 0.5F);
				b = (int) (p * 255.0F + 0.5F);
				break;
			case 1:
				r = (int) (q * 255.0F + 0.5F);
				g = (int) (brightness * 255.0F + 0.5F);
				b = (int) (p * 255.0F + 0.5F);
				break;
			case 2:
				r = (int) (p * 255.0F + 0.5F);
				g = (int) (brightness * 255.0F + 0.5F);
				b = (int) (t * 255.0F + 0.5F);
				break;
			case 3:
				r = (int) (p * 255.0F + 0.5F);
				g = (int) (q * 255.0F + 0.5F);
				b = (int) (brightness * 255.0F + 0.5F);
				break;
			case 4:
				r = (int) (t * 255.0F + 0.5F);
				g = (int) (p * 255.0F + 0.5F);
				b = (int) (brightness * 255.0F + 0.5F);
				break;
			case 5:
				r = (int) (brightness * 255.0F + 0.5F);
				g = (int) (p * 255.0F + 0.5F);
				b = (int) (q * 255.0F + 0.5F);
		}

		return of(alpha, r, g, b);
	}

	public static Color of(int argb) {
		return switch (argb) {
			case 0x00000000 -> TRANSPARENT;
			case 0xFFFFFFFF -> WHITE;
			case 0xFF000000 -> BLACK;
			case 0xFFFF0000 -> RED;
			case 0xFF00FF00 -> GREEN;
			case 0xFF0000FF -> BLUE;
			case 0xFFFFFF00 -> YELLOW;
			case 0xFFFF00FF -> MAGENTA;
			case 0xFF00FFFF -> CYAN;
			default -> new Color(argb);
		};
	}

	public static Color ofABGR(int abgr) {
		int a = (abgr >> 24) & 0xFF;
		int r = abgr & 0xFF;
		int g = (abgr >> 8) & 0xFF;
		int b = (abgr >> 16) & 0xFF;
		return of(a, r, g, b);
	}

	public static Color ofRGB(int rgb) {
		return of(0xFF000000 | rgb);
	}

	public static Color of(int a, int r, int g, int b) {
		return of(((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
	}

	public static Color of(int r, int g, int b) {
		return of(255, r, g, b);
	}

	public static Color of(float a, float r, float g, float b) {
		return of((int) (a * 255.0F), (int) (r * 255.0F), (int) (g * 255.0F), (int) (b * 255.0F));
	}

	/**
	 * Returns the color as an integer in RGB format (Alpha is forced to 255).
	 */
	public int rgb() {
		return (255 << 24) |
			((int) (r * 255) << 16) |
			((int) (g * 255) << 8) |
			(int) (b * 255);
	}

	/**
	 * Returns the color as an integer in ARGB format.
	 */
	public int argb() {
		return ((int) (a * 255) << 24) |
			((int) (r * 255) << 16) |
			((int) (g * 255) << 8) |
			(int) (b * 255);
	}

	/**
	 * Linearly interpolates between this color and another color.
	 */
	public Color lerp(float factor, Color other) {
		return new Color(
			r + factor * (other.r - r),
			g + factor * (other.g - g),
			b + factor * (other.b - b),
			a + factor * (other.a - a)
		);
	}

	public float[] toFloatArray() {
		float[] out = new float[3];
		out[0] = this.r();
		out[1] = this.g();
		out[2] = this.b();
		return out;
	}

	public float r() {
		return r;
	}

	public float g() {
		return g;
	}

	public float b() {
		return b;
	}

	public float a() {
		return a;
	}
}