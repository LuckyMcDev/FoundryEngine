package de.luckymcdev.foundryengine.common.easing;

import net.minecraft.util.Mth;

/**
 * A utility for Cubic Bézier easings.
 * This complements the standard Robert Penner equations in the Easing class.
 * For custom ones, use <a href="https://cubic-bezier.com/">cubic-bezier.com</a>
 */
public class BezierEasing extends Easing {

	public static final BezierEasing EASE = new BezierEasing("ease", 0.25f, 0.1f, 0.25f, 1.0f);
	public static final BezierEasing EASE_IN = new BezierEasing("easeIn", 0.42f, 0.0f, 1.0f, 1.0f);
	public static final BezierEasing EASE_OUT = new BezierEasing("easeOut", 0.0f, 0.0f, 0.58f, 1.0f);
	public static final BezierEasing EASE_IN_OUT = new BezierEasing("easeInOut", 0.42f, 0.0f, 0.58f, 1.0f);
	public static final BezierEasing TEST = new BezierEasing("test", 1, -1.17f, 0.17f, 1.56f);


	private final float x1, y1, x2, y2;

	/**
	 * Creates a new Cubic Bezier easing.
	 * The points (0,0) and (1,1) are assumed as start and end.
	 *
	 * @param name Unique name for the registry
	 * @param x1   Control point 1 X (0-1 range)
	 * @param y1   Control point 1 Y
	 * @param x2   Control point 2 X (0-1 range)
	 * @param y2   Control point 2 Y
	 */
	public BezierEasing(String name, float x1, float y1, float x2, float y2) {
		super(name);
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}

	@Override
	public float ease(float value, float min, float max, float time) {
		// Normalize time to 0.0 - 1.0 range
		float t = Mth.clamp(value / time, 0f, 1f);

		// Solve for the Y value given the X (normalized time)
		float easedT = getBezierT(t);

		return min + (max * easedT);
	}

	/**
	 * Finds the T value of the cubic bezier curve for a given X using
	 * Newton-Raphson iteration for speed and accuracy.
	 */
	private float getBezierT(float x) {
		if (x <= 0) {
			return 0;
		}
		if (x >= 1) {
			return 1;
		}

		float t = x;
		for (int i = 0; i < 8; i++) {
			float currentX = getCubic(t, x1, x2) - x;
			if (Math.abs(currentX) < 1e-6) {
				break;
			}
			float derivative = getDerivative(t, x1, x2);
			if (Math.abs(derivative) < 1e-6) {
				break;
			}
			t -= currentX / derivative;
		}
		return getCubic(t, y1, y2);
	}

	private float getCubic(float t, float p1, float p2) {
		return 3 * (1 - t) * (1 - t) * t * p1 + 3 * (1 - t) * t * t * p2 + t * t * t;
	}

	private float getDerivative(float t, float p1, float p2) {
		return 3 * (1 - t) * (1 - t) * p1 + 6 * (1 - t) * t * (p2 - p1) + 3 * t * t * (1 - p2);
	}
}