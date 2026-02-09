package io.github.luckymcdev.client.gl.shaders.exeption;

import org.jetbrains.annotations.Nullable;

public class ShaderException extends Exception {

    private final String glError;

    public ShaderException(String error) {
        this(error, null);
    }

    public ShaderException(String error, @Nullable String glError) {
        super(error);
        this.glError = glError;
    }

    public @Nullable String getGlError() {
        return this.glError;
    }
}
