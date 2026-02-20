package io.github.luckymcdev.foundryengine.client.opengl.objects;

import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlObject;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL33.GL_ARRAY_BUFFER;

/**
 * A wrapper around a VBO (Vertex Buffer Object) OpenGL Object.
 */
public class VertexBufferObject extends OpenGlObject {

    /**
     * Creates a new Vertex Buffer Object and generates its pointer.
     */
    public VertexBufferObject() {
        this.pointer = GlDispatch.glGenBuffers();
    }

    /**
     * Binds this buffer to the GL_ARRAY_BUFFER target.
     */
    public void bind() {
        GlDispatch.glBindBuffer(GL_ARRAY_BUFFER, this.pointer);
    }

    /**
     * Unbinds the current array buffer.
     */
    public void unbind() {
        GlDispatch.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    /**
     * Uploads float array data to the buffer.
     *
     * @param data  the array of vertex data
     * @param usage the usage pattern (e.g., GL_STATIC_DRAW)
     */
    public void uploadData(float[] data, int usage) {
        bind();
        GlDispatch.glBufferData(GL_ARRAY_BUFFER, data, usage);
    }

    /**
     * Uploads FloatBuffer data to the buffer.
     * @param data  the buffer containing vertex data
     * @param usage the expected usage pattern
     */
    public void uploadData(FloatBuffer data, int usage) {
        bind();
        GlDispatch.glBufferData(GL_ARRAY_BUFFER, data, usage);
    }

    /**
     * Deletes the buffer object from OpenGL memory.
     */
    public void delete() {
        GlDispatch.glDeleteBuffers(this.pointer);
    }

    /**
     * Frees the OpenGL resources associated with this VBO.
     */
    @Override
    public void free() {
        delete();
    }
}