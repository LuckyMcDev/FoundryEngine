package io.github.luckymcdev.client.gl.shaders;

import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlObject;
import io.github.luckymcdev.client.gl.shaders.exeption.ShaderException;
import io.github.luckymcdev.common.Commons;
import net.minecraft.resources.ResourceLocation;

import static org.lwjgl.opengl.GL43C.*;

public class Shader extends OpenGlObject {
    private final ResourceLocation id;
    private final ExtendedShaderType type;
    private final ResourceLocation location;
    private final String source;

    public Shader(ExtendedShaderType shaderType, ShaderSource source) {
        this.id = source.id();
        this.type = shaderType;
        this.location = source.location();
        this.source = Commons.getRlSource(location);
        this.pointer = GlDispatch.glCreateShader(shaderType.glType());
    }

    public void bindSource() {
        GlDispatch.glBindShaderSource(this.pointer, this.source);
    }

    public void compile() throws ShaderException {
        GlDispatch.glCompileShader(this.pointer);

        int compileStatus = GlDispatch.glGetShaderi(this.pointer, GL_COMPILE_STATUS);
        String log = GlDispatch.glGetShaderInfoLog(this.pointer);

        if (compileStatus != GL_TRUE) {
            throw new ShaderException("Failed to compile shader: " + this.id.toString(), log);
        }
    }

    public ExtendedShaderType getType() {
        return type;
    }

    public ResourceLocation getId() {
        return id;
    }

    public String getSource() {
        return source;
    }

    public ResourceLocation getLocation() {
        return location;
    }

    @Override
    public void free() {
        GlDispatch.glDeleteShader(this.pointer);
    }
}
