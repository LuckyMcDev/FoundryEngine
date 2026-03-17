package io.github.luckymcdev.foundryengine.common.util.color;

/**
 * Utility class for handling Colors.
 */
public class Color {
    public static final Color WHITE = new Color(1.0f, 1.0f, 1.0f, 1.0f);
    public static final Color RED = new Color(1.0f, 0.0f, 0.0f, 1.0f);
    public static final Color GREEN = new Color(0.0f, 1.0f, 0.0f, 1.0f);
    public static final Color BLUE = new Color(0.0f, 0.0f, 1.0f, 1.0f);
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

    public Color(int argb) {
        this.a = ((argb >> 24) & 0xFF) / 255.0f;
        this.r = ((argb >> 16) & 0xFF) / 255.0f;
        this.g = ((argb >> 8) & 0xFF) / 255.0f;
        this.b = (argb & 0xFF) / 255.0f;
    }

    /**
     * Returns the color as an integer in ARGB format.
     * The resulting integer has the following format:
     * <code>0xAARRGGB</code>
     * Where <code>A</code> is the alpha component, <code>R</code> is the red component,
     * <code>G</code> is the green component, and <code>B</code> is the blue component.
     * Each component is an 8-bit unsigned integer, ranging from 0 (minimum intensity)
     * to 255 (maximum intensity).
     *
     * @return The color as an integer in ARGB format.
     */
    public int argb() {
        return ((int) (a * 255) << 24) |
                ((int) (r * 255) << 16) |
                ((int) (g * 255) << 8) |
                (int) (b * 255);
    }

    /**
     * Linearly interpolates between this color and another color.
     * The resulting color is a linear interpolation between this color and the given color,
     * with the given factor determining the position of the interpolation.
     * A factor of 0.0 yields this color, while a factor of 1.0 yields the given color.
     * Factors between 0.0 and 1.0 yield colors that are linearly interpolated between
     * this color and the given color.
     *
     * @param factor The factor to use for linear interpolation.
     * @param other  The color to interpolate towards.
     * @return The interpolated color.
     */
    public Color lerp(float factor, Color other) {
        return new Color(
                r + factor * (other.r - r),
                g + factor * (other.g - g),
                b + factor * (other.b - b),
                a + factor * (other.a - a)
        );
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
