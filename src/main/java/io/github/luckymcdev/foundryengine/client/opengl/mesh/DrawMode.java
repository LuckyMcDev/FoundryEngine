package io.github.luckymcdev.foundryengine.client.opengl.mesh;

import static com.mojang.blaze3d.opengl.GlConst.*;

/**
 * Draw Mode, used in {@link Mesh}
 */
public enum DrawMode {
    TRIANGLES(GL_TRIANGLES),
    TRIANGLE_STRIP(GL_TRIANGLE_STRIP),
    TRIANGLE_FAN(GL_TRIANGLE_FAN),
    LINES(GL_LINES),
    LINE_STRIP(GL_LINE_STRIP),
    POINTS(GL_POINTS);

    public final int glEnum;

    DrawMode(int glEnum) {
        this.glEnum = glEnum;
    }

    public int glEnum() {
        return glEnum;
    }
}