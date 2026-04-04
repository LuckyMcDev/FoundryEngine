package de.luckymcdev.foundryengine.client.opengl.objects;

import de.luckymcdev.foundryengine.client.opengl.GlDispatch;
import de.luckymcdev.foundryengine.client.opengl.OpenGlObject;
import org.lwjgl.opengl.GL31;

import static org.lwjgl.opengl.GL33.GL_DYNAMIC_DRAW;

/**
 * A wrapper around a Uniform Buffer Object (UBO) OpenGL wrapper.
 * USES RAW OPENGL CALLS.
 */
public class UniformBufferObject extends OpenGlObject {

    /**
     * Creates a UBO with a specific size and usage pattern.
     *
     * @param size  the size in bytes of the buffer
     * @param usage the usage pattern (e.g., GL_DYNAMIC_DRAW)
     */
    public UniformBufferObject(int size, int usage) {
        this.pointer = GlDispatch.glGenBuffers();

        bind();
        GlDispatch.glBufferData(GL31.GL_UNIFORM_BUFFER, size, usage);
        unbind();
    }

    /**
     * Binds this buffer to the GL_UNIFORM_BUFFER target.
     */
    public void bind() {
        GlDispatch.glBindBuffer(GL31.GL_UNIFORM_BUFFER, this.pointer);
    }

    /**
     * Binds this buffer to a specific binding point.
     * Shaders will look for data at this index.
     *
     * @param bindingPoint the index of the binding point
     */
    public void bindBase(int bindingPoint) {
        GL31.glBindBufferBase(GL31.GL_UNIFORM_BUFFER, bindingPoint, this.pointer);
    }

    /**
     * Unbinds the current uniform buffer.
     */
    public void unbind() {
        GlDispatch.glBindBuffer(GL31.GL_UNIFORM_BUFFER, 0);
    }

    /**
     * Uploads float data to the uniform buffer using GL_DYNAMIC_DRAW.
     *
     * @param data the float array to upload
     */
    public void uploadData(float[] data) {
        bind();
        GlDispatch.glBufferData(GL31.GL_UNIFORM_BUFFER, data, GL_DYNAMIC_DRAW);
    }

    /**
     * Frees the OpenGL resources associated with this UBO.
     */
    @Override
    public void free() {
        GlDispatch.glDeleteBuffers(this.pointer);
    }
}