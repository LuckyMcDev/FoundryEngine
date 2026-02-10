package io.github.luckymcdev.client.gl.shaders.uniform;

import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlObject;
import org.lwjgl.opengl.GL31;

import static org.lwjgl.opengl.GL33.*;

public class UniformBufferObject extends OpenGlObject {

    public UniformBufferObject(int size, int usage) {
        this.pointer = GlDispatch.glGenBuffers();

        bind();
        GlDispatch.glBufferData(GL31.GL_UNIFORM_BUFFER, size, usage);
        unbind();
    }

    public void bind() {
        GlDispatch.glBindBuffer(GL31.GL_UNIFORM_BUFFER, this.pointer);
    }

    /**
     * Binds this buffer to a specific binding point.
     * Shaders will look for data at this index.
     */
    public void bindBase(int bindingPoint) {
        GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, bindingPoint, this.pointer);
    }

    public void unbind() {
        GlDispatch.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
    }

    public void uploadData(float[] data) {
        bind();
        GlDispatch.glBufferData(GL31.GL_UNIFORM_BUFFER, data, GL_DYNAMIC_DRAW);
    }

    @Override
    public void free() {
        GlDispatch.glDeleteBuffers(this.pointer);
    }
}