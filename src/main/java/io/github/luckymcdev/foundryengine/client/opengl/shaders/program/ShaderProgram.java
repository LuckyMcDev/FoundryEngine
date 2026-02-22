package io.github.luckymcdev.foundryengine.client.opengl.shaders.program;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlObject;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.compiler.ShaderCompiler;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import net.minecraft.resources.Identifier;
import org.joml.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * A Wrapper around a OpenGl ShaderProgram.
 * Can attach X ammount of {@link Shader}, and manages them.
 * Uniforms should be set using a {@link Uniform}
 */
public class ShaderProgram extends OpenGlObject {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ArrayList<Shader> shaders = new ArrayList<>();
    private final Identifier id;

    /**
     * Creates a new ShaderProgram from an {@link Identifier} and any ammount of {@link Shader}
     *
     * @param id      the {@link Identifier} for this ShaderProgram.
     * @param shaders the {@link Shader} to add to ths Program.
     */
    public ShaderProgram(Identifier id, Shader... shaders) {
        this.id = id;
        this.pointer = GlDispatch.glCreateProgram();
        this.shaders.addAll(List.of(shaders));
        try {
            attach();
        } catch (ShaderException e) {
            LOGGER.error("{}{}", e.getMessage(), e.getGlError());
        }
    }

    /**
     * Attaches All Shaders to this Program.
     * @throws ShaderException throws a {@link ShaderException} from {@link ShaderCompiler#getOrCompile(Shader)} if anything goes wrong.
     */
    public void attach() throws ShaderException {
        ShaderCompiler compiler = Client.getShaderManager().getCompiler();
        for (Shader shader : this.shaders) {
            Shader compiled = compiler.getOrCompile(shader);
            GlDispatch.glAttachShader(this.pointer, compiled.pointer());
        }
    }

    /**
     * Links this Program.
     * @throws ShaderException throws this if something goes wrong.
     */
    public void link() throws ShaderException {
        GlDispatch.glLinkProgram(this.pointer);

        int linkStatus = GlDispatch.glGetProgrami(this.pointer, GlConst.GL_LINK_STATUS);
        String log = GlDispatch.glGetProgramInfoLog(this.pointer);

        if (linkStatus != GlConst.GL_TRUE) {
            throw new ShaderException("Failed to link program: " + this.id.toString() + " Log: " + log);
        }
    }

    /**
     * Reloads this program:
     * 1. Detaches all Shaders.
     * 2. Re-Attaches them
     * 3. Re-Links the Program.
     *
     * @throws ShaderException exception if something goes wrong.
     */
    public void reload() throws ShaderException {
        for (Shader shader : shaders) {
            GlDispatch.glDetachShader(this.pointer, shader.pointer());
        }

        attach();

        link();

    }

    /**
     * Use this Proram.
     */
    public void use() {
        GlDispatch.glUseProgram(this.pointer);
    }

    /**
     * Disable / Use Program 0
     */
    public void disable() {
        GlDispatch.glUseProgram(0);
    }

    /**
     * Deletes this Program
     */
    public void delete() {
        GlDispatch.glDeleteProgram(this.pointer);
    }

    /**
     * Binds a Uniform Block.
     * @param blockName the name
     * @param bindingPoint the point at which to bind.
     */
    public void bindUniformBlock(String blockName, int bindingPoint) {
        int blockIndex = GlDispatch.glGetUniformBlockIndex(this.pointer, blockName);
        if (blockIndex != -1) {
            GlDispatch.glUniformBlockBinding(this.pointer, blockIndex, bindingPoint);
        }
    }

    public int getUniform(Uniform<?> uniform) {
        return GlDispatch.glGetUniformLocation(this.pointer, uniform.name());
    }

    public void setUniforms(Iterable<Uniform<?>> uniforms) {
        for (Uniform<?> uniform : uniforms) {
            this.setUniform(uniform);
        }
    }

    /**
     * Sets a Uniform using its internal type definition.
     */
    public void setUniform(Uniform<?> uniform) {
        int location = getUniform(uniform);
        if (location == -1) return;

        Object value = uniform.getValue();
        if (value == null) return;

        switch (uniform.type()) {
            case BOOL -> GlDispatch.glUniform1i(location, (Boolean) value ? 1 : 0);
            case INT -> GlDispatch.glUniform1i(location, (Integer) value);
            case FLOAT -> GlDispatch.glUniform1f(location, (Float) value);
            case VEC2 -> GlDispatch.glUniform2f(location, (Vector2f) value);
            case VEC3 -> GlDispatch.glUniform3f(location, (Vector3f) value);
            case VEC4 -> GlDispatch.glUniform4f(location, (Vector4f) value);
            case IVEC2 -> GlDispatch.glUniform2i(location, (Vector2i) value);
            case IVEC3 -> GlDispatch.glUniform3i(location, (Vector3i) value);
            case IVEC4 -> GlDispatch.glUniform4i(location, (Vector4i) value);
            case MAT2 -> GlDispatch.glUniformMatrix2f(location, (Matrix2f) value);
            case MAT3 -> GlDispatch.glUniformMatrix3f(location, (Matrix3f) value);
            case MAT4 -> GlDispatch.glUniformMatrix4f(location, (Matrix4f) value);
            case FLOAT_ARRAY -> GlDispatch.glUniform1fv(location, (float[]) value);
            case INT_ARRAY -> GlDispatch.glUniform1iv(location, (int[]) value);
            default -> throw new IllegalStateException("Unexpected uniform type: " + uniform.type());
        }
    }

    public Identifier getId() {
        return id;
    }

    public ArrayList<Shader> shaders() {
        return shaders;
    }

    @Override
    public void free() {
        delete();
    }
}
