package io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform;

/**
 * A Record to keep Track of a Uniform.
 *
 * @param name  uniform name in the {@link io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader}
 * @param value Any Value.
 * @param <V>   Can be replaced by ? when using.
 */
public record Uniform<V>(String name, V value) {
}
