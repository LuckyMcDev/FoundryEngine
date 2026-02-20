package io.github.luckymcdev.foundryengine.client.opengl.shaders;

import net.minecraft.resources.Identifier;

/**
 * A record of a Shaders Identifier and its Location in the FileSystem.
 *
 * @param id       the Identifier of a Shader
 * @param location the FileSystem location of a Shader.
 */
public record ShaderSource(Identifier id, Identifier location) {
}
