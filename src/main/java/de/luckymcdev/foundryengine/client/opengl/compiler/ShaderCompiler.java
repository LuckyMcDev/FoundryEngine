package de.luckymcdev.foundryengine.client.opengl.compiler;

import de.luckymcdev.foundryengine.client.opengl.exeption.ShaderException;
import de.luckymcdev.foundryengine.client.opengl.shaders.ExtendedShaderType;
import de.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;

/**
 * Shader Compiler.
 * Compiles and Caches {@link Shader}
 */
public final class ShaderCompiler {
    private final GenericRegistry<ShaderKey, Shader> cache = new GenericRegistry<>();

    /**
     * Gets or compiles a Shader.
     *
     * @param shader the shader
     * @return the compiled shader.
     * @throws ShaderException exeption if something goes wrong.
     */
    public Shader getOrCompile(Shader shader) throws ShaderException {
        ShaderKey key = ShaderKey.of(shader);
        Shader cached = cache.getRef(key).get();
        if (cached != null) {
            return cached;
        }

        shader.bindSource();
        shader.compile();

        cache.register(key, shader);
        return shader;
    }

    /**
     * Clears the cache.
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Private record for holding A Shader.
     *
     * @param id     the unique {@link Identifier}
     * @param type   the type {@link ExtendedShaderType}
     * @param source the source code as a {@link String}
     */
    private record ShaderKey(Identifier id, ExtendedShaderType type, String source) {
        /**
         * ShaderKey via Shader.
         *
         * @param shader the shader
         * @return the shader key
         */
        static ShaderKey of(Shader shader) {
            return new ShaderKey(shader.getId(), shader.getType(), shader.getSource());
        }
    }
}
