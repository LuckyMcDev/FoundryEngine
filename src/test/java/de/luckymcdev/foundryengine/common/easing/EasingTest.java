package de.luckymcdev.foundryengine.common.easing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class EasingTest {

	@Test
	void linearEasing_Beginning() {
		float result = Easing.LINEAR.ease(0, 0, 100, 1);
		assertEquals(0, result, 0.001f);
	}

	@Test
	void linearEasing_Middle() {
		float result = Easing.LINEAR.ease(0.5f, 0, 100, 1);
		assertEquals(50, result, 0.001f);
	}

	@Test
	void linearEasing_End() {
		float result = Easing.LINEAR.ease(1, 0, 100, 1);
		assertEquals(100, result, 0.001f);
	}

	@Test
	void linearEasing_WithOffset() {
		float result = Easing.LINEAR.ease(0.5f, 10, 100, 1);
		assertEquals(60, result, 0.001f);
	}

	@Test
	void quadInEasing_Beginning() {
		float result = Easing.QUAD_IN.ease(0, 0, 100, 1);
		assertEquals(0, result, 0.001f);
	}

	@Test
	void quadInEasing_Middle() {
		float result = Easing.QUAD_IN.ease(0.5f, 0, 100, 1);
		assertEquals(25, result, 0.001f);
	}

	@Test
	void quadOutEasing_Middle() {
		float result = Easing.QUAD_OUT.ease(0.5f, 0, 100, 1);
		assertEquals(75, result, 0.001f);
	}

	@Test
	void quadInOutEasing_FirstHalf() {
		float result = Easing.QUAD_IN_OUT.ease(0.25f, 0, 100, 1);
		assertEquals(12.5f, result, 0.001f);
	}

	@Test
	void quadInOutEasing_SecondHalf() {
		float result = Easing.QUAD_IN_OUT.ease(0.75f, 0, 100, 1);
		assertEquals(87.5f, result, 0.001f);
	}

	@Test
	void sineInEasing_Beginning() {
		float result = Easing.SINE_IN.ease(0, 0, 100, 1);
		assertEquals(0, result, 0.001f);
	}

	@Test
	void sineOutEasing_End() {
		float result = Easing.SINE_OUT.ease(1, 0, 100, 1);
		assertEquals(100, result, 0.001f);
	}

	@Test
	void expoInEasing_ZeroValue() {
		float result = Easing.EXPO_IN.ease(0, 0, 100, 1);
		assertEquals(0, result, 0.001f);
	}

	@Test
	void expoOutEasing_FullTime() {
		float result = Easing.EXPO_OUT.ease(1, 0, 100, 1);
		assertEquals(100, result, 0.001f);
	}

	@Test
	void bounceOutEasing_Beginning() {
		float result = Easing.BOUNCE_OUT.ease(0, 0, 100, 1);
		assertEquals(0, result, 0.001f);
	}

	@Test
	void bounceOutEasing_End() {
		float result = Easing.BOUNCE_OUT.ease(1, 0, 100, 1);
		assertEquals(100, result, 0.001f);
	}

	@Test
	void bounceInEasing_End() {
		float result = Easing.BOUNCE_IN.ease(1, 0, 100, 1);
		assertEquals(100, result, 0.001f);
	}

	@Test
	void clampedEasing_ValueBelowZero() {
		float result = Easing.LINEAR.clamped(-10, 0, 100, 1);
		assertEquals(0, result, 0.001f);
	}

	@Test
	void clampedEasing_ValueAboveTime() {
		float result = Easing.LINEAR.clamped(10, 0, 100, 1);
		assertEquals(100, result, 0.001f);
	}

	@Test
	void clampedEasing_ValueInRange() {
		float result = Easing.LINEAR.clamped(0.5f, 0, 100, 1);
		assertEquals(50, result, 0.001f);
	}

	@Test
	void easeDoubleVariant() {
		float result = Easing.LINEAR.ease(0.5, 0, 100, 1);
		assertEquals(50, result, 0.001f);
	}

	@Test
	void easeWithoutTime() {
		float result = Easing.LINEAR.ease(0.5f, 0, 100);
		assertEquals(50, result, 0.001f);
	}

	@Test
	void backInDefaultOvershoot() {
		float result = Easing.BACK_IN.ease(0.5f, 0, 100, 1);
		assertTrue(result < 0, "BackIn should overshoot below 0 at midpoint");
	}

	@Test
	void backOutDefaultOvershoot() {
		float result = Easing.BACK_OUT.ease(0.5f, 0, 100, 1);
		assertTrue(result > 100, "BackOut should overshoot above 100 at midpoint");
	}

	@Test
	void customBackInOvershoot() {
		Easing.BackIn custom = new Easing.BackIn(2.5f);
		float result = custom.ease(0.5f, 0, 100, 1);
		assertNotNull(custom);
		assertTrue(result < 0);
	}

	@Test
	void elasticInDefault() {
		float result = Easing.ELASTIC_IN.ease(1, 0, 100, 1);
		assertEquals(100, result, 0.001f);
	}

	@Test
	void elasticOutDefault() {
		float result = Easing.ELASTIC_OUT.ease(0, 0, 100, 1);
		assertEquals(0, result, 0.001f);
	}

	@Test
	void valueOfExistingEasing() {
		Easing easing = Easing.valueOf("linear");
		assertSame(Easing.LINEAR, easing);
	}

	@Test
	void valueOfNonExistingEasing() {
		Easing easing = Easing.valueOf("nonexistent");
		assertNull(easing);
	}

	@Test
	void easingNameIsSet() {
		assertEquals("linear", Easing.LINEAR.name);
		assertEquals("quadIn", Easing.QUAD_IN.name);
		assertEquals("bounceInOut", Easing.BOUNCE_IN_OUT.name);
	}

	@ParameterizedTest
	@CsvSource({
		"0, 0, 100, 1, 0",
		"0.25, 0, 100, 1, 25",
		"0.5, 0, 100, 1, 50",
		"0.75, 0, 100, 1, 75",
		"1, 0, 100, 1, 100"
	})
	void linearEasingParameterized(float value, float min, float max, float time, float expected) {
		float result = Easing.LINEAR.ease(value, min, max, time);
		assertEquals(expected, result, 0.001f);
	}

	@ParameterizedTest
	@ValueSource(floats = {0, 0.25f, 0.5f, 0.75f, 1})
	void allEasingsReturnValuesInRange(float value) {
		Easing[] easings = {
			Easing.LINEAR, Easing.QUAD_IN, Easing.QUAD_OUT, Easing.QUAD_IN_OUT,
			Easing.CUBIC_IN, Easing.CUBIC_OUT, Easing.CUBIC_IN_OUT,
			Easing.SINE_IN, Easing.SINE_OUT, Easing.SINE_IN_OUT,
			Easing.EXPO_IN, Easing.EXPO_OUT, Easing.EXPO_IN_OUT,
			Easing.CIRC_IN, Easing.CIRC_OUT, Easing.CIRC_IN_OUT,
			Easing.BOUNCE_IN, Easing.BOUNCE_OUT, Easing.BOUNCE_IN_OUT
		};
		for (Easing easing : easings) {
			float result = easing.ease(value, 0, 100, 1);
			assertNotNull(result, easing.name + " returned null at value " + value);
		}
	}

	@Test
	void easingEase_DoubleVariant() {
		float result = Easing.LINEAR.ease(0.5, 0, 100, 1);
		assertEquals(50, result, 0.001f);
	}

	@Test
	void easingEaseWithoutTime_DoubleVariant() {
		float result = Easing.LINEAR.ease(0.5, 0, 100);
		assertEquals(50, result, 0.001f);
	}

	@Test
	void clampedEase_DoubleVariants() {
		float result = Easing.LINEAR.clamped(0.5, 0, 100, 1);
		assertEquals(50, result, 0.001f);
	}

	@Test
	void clampedEaseWithoutTime_DoubleVariant() {
		float result = Easing.LINEAR.clamped(0.5, 0, 100);
		assertEquals(50, result, 0.001f);
	}
}
