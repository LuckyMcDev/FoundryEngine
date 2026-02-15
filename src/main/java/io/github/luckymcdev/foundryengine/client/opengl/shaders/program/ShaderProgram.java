package io.github.luckymcdev.foundryengine.client.opengl.shaders.program;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlObject;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.Shader;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.compiler.ShaderCompiler;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.exeption.ShaderException;
import io.github.luckymcdev.foundryengine.client.opengl.shaders.uniform.Uniform;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.minecraft.resources.Identifier;
import org.joml.*;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class ShaderProgram extends OpenGlObject {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ArrayList<Shader> shaders = new ArrayList<>();
    private final Identifier id;

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

    public void attach() throws ShaderException {
        ShaderCompiler compiler = Instances.getShaderManager().getCompiler();
        for (Shader shader : this.shaders) {
            Shader compiled = compiler.getOrCompile(shader);
            GlDispatch.glAttachShader(this.pointer, compiled.pointer());
        }
    }

    public void link() throws ShaderException {
        GlDispatch.glLinkProgram(this.pointer);

        int linkStatus = GlDispatch.glGetProgrami(this.pointer, GlConst.GL_LINK_STATUS);
        String log = GlDispatch.glGetProgramInfoLog(this.pointer);

        if (linkStatus != GlConst.GL_TRUE) {
            throw new ShaderException("Failed to link program: " + this.id.toString() + " Log: " + log);
        }
    }

    public void reload() throws ShaderException {
        for(Shader shader : shaders) {
            GlDispatch.glDetachShader(this.pointer, shader.pointer());
        }

        attach();

        link();

    }

    public void use() {
        GlDispatch.glUseProgram(this.pointer);
    }

    public void disable() {
        GlDispatch.glUseProgram(0);
    }

    public void delete() {
        GlDispatch.glDeleteProgram(this.pointer);
    }

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

    public void setUniform(Uniform<?> uniform) {
        int location = getUniform(uniform);
        if (location == -1) {
            return;
        }

        Object value = uniform.value();
        if (value instanceof Integer i) {
            GlDispatch.glUniform1i(location, i);
        } else if (value instanceof Float f) {
            GlDispatch.glUniform1f(location, f);
        } else if (value instanceof Vector2f v) {
            GlDispatch.glUniform2f(location, v);
        } else if (value instanceof Vector3f v) {
            GlDispatch.glUniform3f(location, v);
        } else if (value instanceof Vector4f v) {
            GlDispatch.glUniform4f(location, v);
        } else if (value instanceof Vector2i v) {
            GlDispatch.glUniform2i(location, v);
        } else if (value instanceof Vector3i v) {
            GlDispatch.glUniform3i(location, v);
        } else if (value instanceof Vector4i v) {
            GlDispatch.glUniform4i(location, v);
        } else if (value instanceof Matrix2f m) {
            GlDispatch.glUniformMatrix2f(location, m);
        } else if (value instanceof Matrix3f m) {
            GlDispatch.glUniformMatrix3f(location, m);
        } else if (value instanceof Matrix4f m) {
            GlDispatch.glUniformMatrix4f(location, m);
        }
    }

    public Identifier getId() {
        return id;
    }

    @Override
    public void free() {
        delete();
    }
}
