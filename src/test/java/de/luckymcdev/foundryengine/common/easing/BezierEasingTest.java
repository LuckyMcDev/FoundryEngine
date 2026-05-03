package de.luckymcdev.foundryengine.common.easing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BezierEasingTest {

    @Test
    void ease_Beginning_ReturnsMin() {
        float result = BezierEasing.EASE.ease(0, 0, 100, 1);
        assertEquals(0, result, 0.001f);
    }

    @Test
    void ease_End_ReturnsMinPlusMax() {
        float result = BezierEasing.EASE.ease(1, 0, 100, 1);
        assertEquals(100, result, 0.001f);
    }

    @Test
    void easeIn_Middle() {
        float result = BezierEasing.EASE_IN.ease(0.5f, 0, 100, 1);
        assertTrue(result >= 0 && result <= 100);
    }

    @Test
    void easeOut_Middle() {
        float result = BezierEasing.EASE_OUT.ease(0.5f, 0, 100, 1);
        assertTrue(result >= 0 && result <= 100);
    }

    @Test
    void easeInOut_Middle() {
        float result = BezierEasing.EASE_IN_OUT.ease(0.5f, 0, 100, 1);
        assertEquals(50, result, 5.0f);
    }

    @Test
    void ease_ClampsLowValues() {
        float result = BezierEasing.EASE.ease(-10, 0, 100, 1);
        assertEquals(0, result, 0.001f);
    }

    @Test
    void ease_ClampsHighValues() {
        float result = BezierEasing.EASE.ease(10, 0, 100, 1);
        assertEquals(100, result, 0.001f);
    }

    @Test
    void ease_WithOffset() {
        float result = BezierEasing.EASE.ease(0.5f, 10, 100, 1);
        assertTrue(result >= 10 && result <= 110);
    }

    @Test
    void customBezierEasing() {
        BezierEasing custom = new BezierEasing("custom", 0.5f, 0.0f, 0.5f, 1.0f);
        float result = custom.ease(0.5f, 0, 100, 1);
        assertTrue(result >= 0 && result <= 100);
        assertEquals("custom", custom.name);
    }

    @Test
    void bezierEasing_IsRegistered() {
        assertSame(BezierEasing.EASE, Easing.valueOf("ease"));
        assertSame(BezierEasing.EASE_IN, Easing.valueOf("easeIn"));
        assertSame(BezierEasing.EASE_OUT, Easing.valueOf("easeOut"));
        assertSame(BezierEasing.EASE_IN_OUT, Easing.valueOf("easeInOut"));
    }

    @Test
    void testPreset_Exists() {
        assertNotNull(BezierEasing.TEST);
        assertEquals("test", BezierEasing.TEST.name);
    }
}
