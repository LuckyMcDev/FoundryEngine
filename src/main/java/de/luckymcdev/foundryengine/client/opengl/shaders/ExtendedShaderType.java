package de.luckymcdev.foundryengine.client.opengl.shaders;

import org.lwjgl.opengl.GL43;

import static org.lwjgl.opengl.GL43C.*;

/**
 * An Enum defining Shader Types. Supports all OpenGl {@link GL43} Shaders.
 */
public enum ExtendedShaderType {
    /**
     * Vertex Shader.
     */
    VERTEX("vertex", GL_VERTEX_SHADER),
    /**
     * Fragment Shader
     */
    FRAGMENT("fragment", GL_FRAGMENT_SHADER),
    /**
     * Geometry Shader.
     */
    GEOMETRY("geometry", GL_GEOMETRY_SHADER),
    /**
     * Tesselation Evaluation Shader
     */
    TESS_EVAL("tess_eval", GL_TESS_EVALUATION_SHADER),
    /**
     * Tesselation Control Shader.
     */
    TESS_CONTROL("tess_control", GL_TESS_CONTROL_SHADER),
    /**
     * Compute Shader.
     * Careful with this, it needs to be dispatched differently. No native impl currently in {@link Shader}
     */
    COMPUTE("compute", GL_COMPUTE_SHADER);

    private final String typeName;
    private final int glType;

    ExtendedShaderType(String typeName, int glType) {
        this.typeName = typeName;
        this.glType = glType;
    }

    /**
     * Returns the name of the Shader Type
     *
     * @return shader type name
     */
    public String getName() {
        return typeName;
    }

    /**
     * Gl Type
     *
     * @return the OpenGl Shader Type
     */
    public int glType() {
        return glType;
    }

    @Override
    public String toString() {
        return Integer.toString(glType);
    }
}
