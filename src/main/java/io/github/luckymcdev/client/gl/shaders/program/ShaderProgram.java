package io.github.luckymcdev.client.gl.shaders.program;

import com.mojang.blaze3d.opengl.GlConst;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlObject;
import io.github.luckymcdev.client.gl.shaders.Shader;
import io.github.luckymcdev.client.gl.shaders.compiler.ShaderCompiler;
import io.github.luckymcdev.client.gl.shaders.exeption.ShaderException;
import io.github.luckymcdev.client.gl.shaders.uniform.Uniform;
import net.minecraft.resources.ResourceLocation;
import org.joml.*;
import org.lwjgl.opengl.GL43C;
import org.slf4j.Logger;

import java.util.ArrayList;

public class ShaderProgram extends OpenGlObject {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final ArrayList<Shader> shaders = new ArrayList<>();
    private final ShaderCompiler compiler;
    private final ResourceLocation id;

    public ShaderProgram(ResourceLocation id, Shader... shaders) {
        this.id = id;
        this.compiler = new ShaderCompiler();
        this.pointer = GlDispatch.glCreateProgram();

        for (Shader shader : shaders) {
            this.shaders.add(shader);
            try {
                Shader compiled = compiler.getOrCompile(shader);
                GlDispatch.glAttachShader(this.pointer, compiled.pointer());
            } catch (ShaderException e) {
                LOGGER.error(e.getMessage() + e.getGlError());
            }
        }
    }

    public void link() throws ShaderException {
        GlDispatch.glLinkProgram(this.pointer);

        int linkStatus = GlDispatch.glGetProgrami(this.pointer, GL43C.GL_LINK_STATUS);
        String log = GlDispatch.glGetProgramInfoLog(this.pointer);

        if(linkStatus != GlConst.GL_TRUE) {
            throw new ShaderException("Failed to link program: " + this.id.toString() + " Log: "+log);
        }
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

    public int getUniform(Uniform<?> uniform) {
        return GlDispatch.glGetUniformLocation(this.pointer, uniform.name());
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

    @Override
    public void free() {

    }
}
