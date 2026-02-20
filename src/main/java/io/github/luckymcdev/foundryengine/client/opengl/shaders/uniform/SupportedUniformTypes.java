package io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform;

import org.joml.*;

/**
 * Represents the supported uniform types for shader uniforms.
 */
public enum SupportedUniformTypes {
    /**
     * Integer uniform type.
     */
    INT(Integer.class),

    /**
     * Float uniform type.
     */
    FLOAT(Float.class),

    /**
     * 2D vector of floats.
     */
    VEC2(Vector2f.class),

    /**
     * 3D vector of floats.
     */
    VEC3(Vector3f.class),

    /**
     * 4D vector of floats.
     */
    VEC4(Vector4f.class),

    /**
     * 2D vector of integers.
     */
    IVEC2(Vector2i.class),

    /**
     * 3D vector of integers.
     */
    IVEC3(Vector3i.class),

    /**
     * 4D vector of integers.
     */
    IVEC4(Vector4i.class),

    /**
     * 2x2 matrix of floats.
     */
    MAT2(Matrix2f.class),

    /**
     * 3x3 matrix of floats.
     */
    MAT3(Matrix3f.class),

    /**
     * 4x4 matrix of floats.
     */
    MAT4(Matrix4f.class);

    private final Class<?> type;

    SupportedUniformTypes(Class<?> type) {
        this.type = type;
    }

    /**
     * Gets the class associated with this uniform type.
     *
     * @return The class of the uniform type.
     */
    public Class<?> getType() {
        return type;
    }
}
