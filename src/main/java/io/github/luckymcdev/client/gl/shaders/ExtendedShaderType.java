package io.github.luckymcdev.client.gl.shaders;

import static org.lwjgl.opengl.GL43C.*;

public enum ExtendedShaderType {
    VERTEX("vertex", GL_VERTEX_SHADER),
    FRAGMENT("fragment", GL_FRAGMENT_SHADER),
    GEOMETRY("geometry", GL_GEOMETRY_SHADER),
    TESS_EVAL("tess_eval", GL_TESS_EVALUATION_SHADER),
    TESS_CONTROL("tess_control", GL_TESS_CONTROL_SHADER),
    COMPUTE("compute", GL_COMPUTE_SHADER);

    private final String typeName;
    private final int glType;

    ExtendedShaderType(String typeName, int glType) {
        this.typeName = typeName;
        this.glType = glType;
    }

    public String getName() {
        return typeName;
    }

    public int glType() {
        return glType;
    }

    @Override
    public String toString() {
        return Integer.toString(glType);
    }
}
