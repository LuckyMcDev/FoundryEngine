package io.github.luckymcdev.client.opengl.shaders;

import io.github.luckymcdev.client.opengl.GlDispatch;
import io.github.luckymcdev.client.opengl.OpenGlObject;
import io.github.luckymcdev.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.client.opengl.shaders.preprocessing.GLSLPreProcessorManager;
import io.github.luckymcdev.common.Commons;
import io.github.luckymcdev.common.Instances;
import net.minecraft.resources.ResourceLocation;

import static org.lwjgl.opengl.GL43C.*;

public class Shader extends OpenGlObject {
    private final ResourceLocation id;
    private final ExtendedShaderType type;
    private final ResourceLocation location;
    private String source;

    public Shader(ExtendedShaderType shaderType, ShaderSource source) {
        this.id = source.id();
        this.type = shaderType;
        this.location = source.location();
        this.source = loadSource();
        this.pointer = GlDispatch.glCreateShader(shaderType.glType());
        setDebugLabel(this.id.toString());
    }

    /**
     * Reloads the shader source from the file system and recompiles.
     * @throws ShaderException if the new source fails to compile.
     */
    public void reload() throws ShaderException {
        this.source = loadSource();
        this.bindSource();
        this.compile();
    }

    private String loadSource() {
        String unprocessedSource = Commons.getRlSource(this.location);
        return Instances.getShaderManager().getPreProcessorManager().processAll(unprocessedSource);
    }

    public void bindSource() {
        GlDispatch.glBindShaderSource(this.pointer, this.source);
    }

    public void compile() throws ShaderException {
        GlDispatch.glCompileShader(this.pointer);

        int compileStatus = GlDispatch.glGetShaderi(this.pointer, GL_COMPILE_STATUS);
        if (compileStatus != GL_TRUE) {
            String log = GlDispatch.glGetShaderInfoLog(this.pointer);
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

    private void setDebugLabel(String label) {
        if (label != null && this.pointer != 0) {
            GlDispatch.safeGlObjectLabel(GL_SHADER, this.pointer, label);
        }
    }

    @Override
    public void free() {
        GlDispatch.glDeleteShader(this.pointer);
    }
}
