package io.github.luckymcdev.foundryengine.client.opengl.shaders;

import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlObject;
import io.github.luckymcdev.foundryengine.client.opengl.compiler.ShaderCompiler;
import io.github.luckymcdev.foundryengine.client.opengl.exeption.ShaderException;
import net.minecraft.resources.Identifier;

import static org.lwjgl.opengl.GL43C.*;

/**
 * A Wrapper Around an OpenGl Shader Object.
 */
public class Shader extends OpenGlObject {
    private final Identifier id;
    private final ExtendedShaderType type;
    private final Identifier location;
    private String source;

    /**
     * Creates a new Shader From A {@link ExtendedShaderType} and a {@link ShaderSource} source.
     *
     * @param shaderType the ShaderType
     * @param source     the Shader Source.
     */
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
     * Only recompiles if the source code has actually changed.
     * @throws ShaderException if the new source fails to compile.
     */
    public void reload() throws ShaderException {
        String newSource = loadSource();

        // Only recompile if source actually changed
        if (!newSource.equals(this.source)) {
            this.source = newSource;
            this.bindSource();
            this.compile();
        }
    }

    /**
     * Loads and Processes a Shader Source Code.
     * Uses cached source from ShaderManager to avoid repeated file I/O.
     * @return the processed source code.
     */
    private String loadSource() {
        String unprocessedSource = Client.getShaderManager().getCachedSource(this.location);
        return Client.getShaderManager().getPreProcessorManager().processAll(unprocessedSource);
    }

    /**
     * Binds the Shaders source code to the Shader Object.
     */
    public void bindSource() {
        GlDispatch.glBindShaderSource(this.pointer, this.source);
    }

    /**
     * Compiles the Shader.
     * <br>
     * {@link ShaderCompiler}
     * @throws ShaderException exception to throw if something is wrong.
     */
    public void compile() throws ShaderException {
        GlDispatch.glCompileShader(this.pointer);

        int compileStatus = GlDispatch.glGetShaderi(this.pointer, GL_COMPILE_STATUS);
        if (compileStatus != GL_TRUE) {
            String log = GlDispatch.glGetShaderInfoLog(this.pointer);
            throw new ShaderException("Failed to compile shader: " + this.id.toString(), log);
        }
    }

    /**
     * Returns the Shader type
     * @return Shader Type
     */
    public ExtendedShaderType getType() {
        return type;
    }

    /**
     * Returns the Shaders Identifier.
     * @return the Shaders Identifier
     */
    public Identifier getId() {
        return id;
    }

    /**
     * The Shaders Source Code.
     * @return the Source Code.
     */
    public String getSource() {
        return source;
    }

    /**
     * The File System Location via {@link Identifier}
     * @return the location
     */
    public Identifier getLocation() {
        return location;
    }

    /**
     * Sets a Debug Label to the Shader.
     * @param label the label to set.
     */
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