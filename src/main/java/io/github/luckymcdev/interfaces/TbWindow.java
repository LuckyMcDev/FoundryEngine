package io.github.luckymcdev.interfaces;

public interface TbWindow {
    default void tb$setViewportArea(double xOffset, double yOffset, double xScale, double yScale) {
        // NO-OP
    }

    default double tb$getXOffset() {
        return 0;
    }

    default double tb$getYOffset() {
        return 0;
    }

    default double tb$getInverseYOffset() {
        return 0;
    }

    default int tb$getUnscaledWidth() {
        return 1;
    }

    default int tb$getUnscaledHeight() {
        return 1;
    }

    default int tb$getUnscaledFramebufferWidth() {
        return 1;
    }

    default int tb$getUnscaledFramebufferHeight() {
        return 1;
    }

    // EXACT vidlib implementation
    default double tb$modifyCursorX(double x) {
        return x - (tb$getXOffset() * tb$getUnscaledWidth());
    }

    default double tb$modifyCursorY(double y) {
        return y - (tb$getYOffset() * tb$getUnscaledHeight());
    }
}