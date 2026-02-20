package io.github.luckymcdev.foundryengine.client.opengl.mesh;

import static com.mojang.blaze3d.opengl.GlConst.*;

/**
 * Draw Mode, used in {@link Mesh}
 * For most usages use {@link #TRIANGLES}
 */
public enum DrawMode {
    /**
     * Triangle Draw Mode
     */
    TRIANGLES(GL_TRIANGLES),
    /** Triangle Strip Draw Mode */
    TRIANGLE_STRIP(GL_TRIANGLE_STRIP),
    /** Triangle Fan Draw Mode */
    TRIANGLE_FAN(GL_TRIANGLE_FAN),
    /** Lines Draw Mode */
    LINES(GL_LINES),
    /** Line Strip Draw Mode */
    LINE_STRIP(GL_LINE_STRIP),
    /** Points Draw Mode */
    POINTS(GL_POINTS);

    private final int glEnum;

    DrawMode(int glEnum) {
        this.glEnum = glEnum;
    }

    /**
     * Get the underlying Gl Type as an int.
     * @return the gl type as int.
     */
    public int glEnum() {
        return glEnum;
    }
}