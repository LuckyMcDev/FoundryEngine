package de.luckymcdev.foundryengine.common.util.color;

import de.luckymcdev.foundryengine.common.easing.Easing;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorGradientTest {

	@Test
	void emptyGradient_ReturnsWhite() {
		ColorGradient gradient = new ColorGradient();
		Color result = gradient.getColor(0.5f);
		assertSame(Color.WHITE, result);
	}

	@Test
	void singleStep_ReturnsThatColor() {
		ColorGradient gradient = new ColorGradient();
		gradient.addStep(Color.RED, 0.5f, Easing.LINEAR);

		assertEquals(Color.RED.r, gradient.getColor(0.0f).r, 0.001f);
		assertEquals(Color.RED.g, gradient.getColor(0.5f).g, 0.001f);
		assertEquals(Color.RED.b, gradient.getColor(1.0f).b, 0.001f);
	}

	@Test
	void progressBeforeFirstStep_ReturnsFirstColor() {
		ColorGradient gradient = new ColorGradient();
		gradient.addStep(Color.RED, 0.3f, Easing.LINEAR);
		gradient.addStep(Color.BLUE, 0.7f, Easing.LINEAR);

		Color result = gradient.getColor(0.1f);
		assertEquals(Color.RED.r, result.r, 0.001f);
	}

	@Test
	void progressAfterLastStep_ReturnsLastColor() {
		ColorGradient gradient = new ColorGradient();
		gradient.addStep(Color.RED, 0.3f, Easing.LINEAR);
		gradient.addStep(Color.BLUE, 0.7f, Easing.LINEAR);

		Color result = gradient.getColor(0.9f);
		assertEquals(Color.BLUE.b, result.b, 0.001f);
	}

	@Test
	void twoSteps_LinearInterpolation() {
		ColorGradient gradient = new ColorGradient();
		gradient.addStep(new Color(0, 0, 0, 255), 0.0f, Easing.LINEAR);
		gradient.addStep(new Color(255, 255, 255, 255), 1.0f, Easing.LINEAR);

		Color result = gradient.getColor(0.5f);
		assertEquals(0.5f, result.r(), 0.01f);
		assertEquals(0.5f, result.g(), 0.01f);
		assertEquals(0.5f, result.b(), 0.01f);
	}

	@Test
	void stepsAreSortedByPosition() {
		ColorGradient gradient = new ColorGradient();
		gradient.addStep(Color.BLUE, 0.7f, Easing.LINEAR);
		gradient.addStep(Color.RED, 0.3f, Easing.LINEAR);

		Color result = gradient.getColor(0.1f);
		assertEquals(Color.RED.r, result.r, 0.001f);
	}

	@Test
	void addStep_ReturnsThisForChaining() {
		ColorGradient gradient = new ColorGradient();
		ColorGradient result = gradient.addStep(Color.RED, 0.5f, Easing.LINEAR);
		assertSame(gradient, result);
	}

	@Test
	void withEasing_AffectsInterpolation() {
		ColorGradient gradient = new ColorGradient();
		gradient.addStep(new Color(0, 0, 0, 255), 0.0f, Easing.QUAD_IN);
		gradient.addStep(new Color(1.0f, 1.0f, 1.0f, 1.0f), 1.0f, Easing.QUAD_IN);

		Color linearResult = new Color(0, 0, 0, 255).lerp(0.25f, new Color(1, 1, 1, 255));
		Color easedResult = gradient.getColor(0.5f);

		assertNotEquals(linearResult.r, easedResult.r, 0.01f);
	}
}
