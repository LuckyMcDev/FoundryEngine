package de.luckymcdev.foundryengine.client.opengl.uniform;

import java.util.function.Supplier;

/**
 * A Record to keep Track of a Uniform.
 *
 * @param name          uniform name in the shader
 * @param type          the explicit {@link SupportedUniformTypes}
 * @param <V>           the type of the value
 * @param valueSupplier the logic to fetch the current value
 */
public record Uniform<V>(String name, SupportedUniformTypes type, Supplier<V> valueSupplier) {

    public Uniform(String name, Supplier<V> valueSupplier) {
        this(name, detectType(valueSupplier.get()), valueSupplier);
    }

    private static SupportedUniformTypes detectType(Object obj) {
        if (obj instanceof Boolean) return SupportedUniformTypes.BOOL;
        if (obj instanceof Integer) return SupportedUniformTypes.INT;
        if (obj instanceof Double) return SupportedUniformTypes.FLOAT;
        if (obj instanceof Float) return SupportedUniformTypes.FLOAT;
        if (obj instanceof org.joml.Vector2f) return SupportedUniformTypes.VEC2;
        if (obj instanceof org.joml.Vector3f) return SupportedUniformTypes.VEC3;
        if (obj instanceof org.joml.Vector4f) return SupportedUniformTypes.VEC4;
        if (obj instanceof org.joml.Vector2i) return SupportedUniformTypes.IVEC2;
        if (obj instanceof org.joml.Vector3i) return SupportedUniformTypes.IVEC3;
        if (obj instanceof org.joml.Vector4i) return SupportedUniformTypes.IVEC4;
        if (obj instanceof org.joml.Matrix2f) return SupportedUniformTypes.MAT2;
        if (obj instanceof org.joml.Matrix3f) return SupportedUniformTypes.MAT3;
        if (obj instanceof org.joml.Matrix4f) return SupportedUniformTypes.MAT4;
        if (obj instanceof float[]) return SupportedUniformTypes.FLOAT_ARRAY;
        if (obj instanceof int[]) return SupportedUniformTypes.INT_ARRAY;
        throw new IllegalArgumentException("Unsupported uniform class: " + obj.getClass());
    }

    public V getValue() {
        return valueSupplier.get();
    }
}
