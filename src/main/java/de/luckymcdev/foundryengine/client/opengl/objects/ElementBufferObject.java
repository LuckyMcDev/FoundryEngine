package de.luckymcdev.foundryengine.client.opengl.objects;

import de.luckymcdev.foundryengine.client.opengl.GlDispatch;
import de.luckymcdev.foundryengine.client.opengl.OpenGlObject;

import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.GL_ELEMENT_ARRAY_BUFFER;

/**
 * A wrapper around a EBO (Element Buffer Object) OpenGL Object.
 * Used for storing indices that refer to vertex data.
 */
public class ElementBufferObject extends OpenGlObject {

    /**
     * Creates a new Element Buffer Object and generates its OpenGL pointer.
     */
    public ElementBufferObject() {
        this.pointer = GlDispatch.glGenBuffers();
    }

    /**
     * Binds this buffer to the GL_ELEMENT_ARRAY_BUFFER target.
     */
    public void bind() {
        GlDispatch.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.pointer);
    }

    /**
     * Unbinds the current element array buffer by binding to 0.
     */
    public void unbind() {
        GlDispatch.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    /**
     * Uploads integer array data to the buffer.
     *
     * @param data  the array of indices to upload
     * @param usage the expected usage pattern (e.g., GL_STATIC_DRAW)
     */
    public void uploadData(int[] data, int usage) {
        bind();
        GlDispatch.glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, usage);
    }

    /**
     * Uploads IntBuffer data to the buffer.
     * @param data  the buffer of indices to upload
     * @param usage the expected usage pattern
     */
    public void uploadData(IntBuffer data, int usage) {
        bind();
        GlDispatch.glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, usage);
    }

    /**
     * Deletes the buffer object from OpenGL memory.
     */
    public void delete() {
        GlDispatch.glDeleteBuffers(this.pointer);
    }

    /**
     * Frees the OpenGL resources associated with this object.
     */
    @Override
    public void free() {
        delete();
    }
}