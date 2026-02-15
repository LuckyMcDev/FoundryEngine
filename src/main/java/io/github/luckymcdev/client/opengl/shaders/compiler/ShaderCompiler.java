package io.github.luckymcdev.client.opengl.shaders.compiler;

import io.github.luckymcdev.client.opengl.shaders.ExtendedShaderType;
import io.github.luckymcdev.client.opengl.shaders.Shader;
import io.github.luckymcdev.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;

/**
 * A class which compiles and caches Shaders.
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
        Shader cached = cache.get(key);
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
