package io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption;

import org.jetbrains.annotations.Nullable;

/**
 * An Exception for working with Shaders.
 */
public class ShaderException extends Exception {
    private final String glError;

    /**
     * Instantiates a new Shader exception.
     *
     * @param error the error
     */
    public ShaderException(String error) {
        this(error, null);
    }

    /**
     * Instantiates a new Shader exception.
     *
     * @param error   the error
     * @param glError the gl error
     */
    public ShaderException(String error, @Nullable String glError) {
        super(error);
        this.glError = glError;
    }

    /**
     * Gets gl error.
     *
     * @return the gl error
     */
    public @Nullable String getGlError() {
        return this.glError;
    }
}
