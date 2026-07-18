package de.luckymcdev.foundryengine.common.util.color;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorTest {

	@Test
	void constructorRGB_SetsComponents() {
		Color c = new Color(255, 128, 64);
		assertEquals(1.0f, c.r, 0.001f);
		assertEquals(128 / 255.0f, c.g, 0.001f);
		assertEquals(64 / 255.0f, c.b, 0.001f);
		assertEquals(1.0f, c.a, 0.001f);
	}

	@Test
	void constructorRGBA_SetsAlpha() {
		Color c = new Color(100, 150, 200, 128);
		assertEquals(100 / 255.0f, c.r, 0.001f);
		assertEquals(150 / 255.0f, c.g, 0.001f);
		assertEquals(200 / 255.0f, c.b, 0.001f);
		assertEquals(128 / 255.0f, c.a, 0.001f);
	}

	@Test
	void constructorARGB_ParsesCorrectly() {
		int argb = 0x80FF8000;
		Color c = new Color(argb);
		assertEquals(1.0f, c.r, 0.001f);
		assertEquals(128 / 255.0f, c.g, 0.001f);
		assertEquals(0.0f, c.b, 0.001f);
		assertEquals(128 / 255.0f, c.a, 0.001f);
	}

	@Test
	void constructorFloatRGBA_SetsComponents() {
		Color c = new Color(0.5f, 0.25f, 0.75f, 0.5f);
		assertEquals(0.5f, c.r, 0.001f);
		assertEquals(0.25f, c.g, 0.001f);
		assertEquals(0.75f, c.b, 0.001f);
		assertEquals(0.5f, c.a, 0.001f);
	}

	@Test
	void rgb_ReturnsCorrectFormat() {
		Color c = new Color(255, 128, 64);
		int rgb = c.rgb();
		assertEquals(255, (rgb >> 16) & 0xFF);
		assertEquals(128, (rgb >> 8) & 0xFF);
		assertEquals(64, rgb & 0xFF);
		assertEquals(255, (rgb >> 24) & 0xFF);
	}

	@Test
	void argb_ReturnsCorrectFormat() {
		Color c = new Color(255, 128, 64, 128);
		int argb = c.argb();
		assertEquals(128, (argb >> 24) & 0xFF);
		assertEquals(255, (argb >> 16) & 0xFF);
		assertEquals(128, (argb >> 8) & 0xFF);
		assertEquals(64, argb & 0xFF);
	}

	@Test
	void lerp_FactorZero_ReturnsThis() {
		Color c1 = new Color(100, 200, 50, 255);
		Color c2 = new Color(200, 100, 150, 0);
		Color result = c1.lerp(0, c2);
		assertEquals(c1.r, result.r, 0.001f);
		assertEquals(c1.g, result.g, 0.001f);
		assertEquals(c1.b, result.b, 0.001f);
		assertEquals(c1.a, result.a, 0.001f);
	}

	@Test
	void lerp_FactorOne_ReturnsOther() {
		Color c1 = new Color(100, 200, 50, 255);
		Color c2 = new Color(200, 100, 150, 0);
		Color result = c1.lerp(1, c2);
		assertEquals(c2.r, result.r, 0.001f);
		assertEquals(c2.g, result.g, 0.001f);
		assertEquals(c2.b, result.b, 0.001f);
		assertEquals(c2.a, result.a, 0.001f);
	}

	@Test
	void lerp_FactorHalf_ReturnsMidpoint() {
		Color c1 = new Color(0, 0, 0, 0);
		Color c2 = new Color(255, 255, 255, 255);
		Color result = c1.lerp(0.5f, c2);
		assertEquals(0.5f, result.r, 0.001f);
		assertEquals(0.5f, result.g, 0.001f);
		assertEquals(0.5f, result.b, 0.001f);
		assertEquals(0.5f, result.a, 0.001f);
	}

	@Test
	void predefinedColors() {
		assertEquals(1.0f, Color.WHITE.r, 0.001f);
		assertEquals(1.0f, Color.WHITE.g, 0.001f);
		assertEquals(1.0f, Color.WHITE.b, 0.001f);

		assertEquals(0.0f, Color.BLACK.r, 0.001f);
		assertEquals(0.0f, Color.BLACK.g, 0.001f);
		assertEquals(0.0f, Color.BLACK.b, 0.001f);

		assertEquals(1.0f, Color.RED.r, 0.001f);
		assertEquals(0.0f, Color.RED.g, 0.001f);
		assertEquals(0.0f, Color.RED.b, 0.001f);

		assertEquals(0.0f, Color.GREEN.r, 0.001f);
		assertEquals(1.0f, Color.GREEN.g, 0.001f);
		assertEquals(0.0f, Color.GREEN.b, 0.001f);

		assertEquals(0.0f, Color.BLUE.r, 0.001f);
		assertEquals(0.0f, Color.BLUE.g, 0.001f);
		assertEquals(1.0f, Color.BLUE.b, 0.001f);
	}

	@Test
	void getters_ReturnCorrectValues() {
		Color c = new Color(0.1f, 0.2f, 0.3f, 0.4f);
		assertEquals(0.1f, c.r(), 0.001f);
		assertEquals(0.2f, c.g(), 0.001f);
		assertEquals(0.3f, c.b(), 0.001f);
		assertEquals(0.4f, c.a(), 0.001f);
	}

	@Test
	void lerp_BeyondRange_ClampsNotApplied() {
		Color c1 = new Color(100, 100, 100, 100);
		Color c2 = new Color(200, 200, 200, 200);
		Color result = c1.lerp(2.0f, c2);
		assertEquals(300 / 255.0f, result.r, 0.001f);
	}
}
