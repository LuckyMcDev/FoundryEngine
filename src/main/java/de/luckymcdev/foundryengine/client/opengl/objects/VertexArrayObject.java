package de.luckymcdev.foundryengine.client.opengl.objects;

import de.luckymcdev.foundryengine.client.opengl.GlDispatch;
import de.luckymcdev.foundryengine.client.opengl.OpenGlObject;

/**
 * A wrapper around a VAO (Vertex Array Object) OpenGL Object.
 */
public class VertexArrayObject extends OpenGlObject {

    /**
     * Creates a new VAO and generates its OpenGL pointer.
     */
    public VertexArrayObject() {
        this.pointer = GlDispatch.glGenVertexArrays();
    }

    /**
     * Binds this Vertex Array Object.
     */
    public void bind() {
        GlDispatch.glBindVertexArray(this.pointer);
    }

    /**
     * Unbinds the current VAO.
     */
    public void unbind() {
        GlDispatch.glBindVertexArray(0);
    }

    /**
     * Deletes the VAO from OpenGL memory.
     */
    public void delete() {
        GlDispatch.glDeleteVertexArrays(this.pointer);
    }

    /**
     * Frees the OpenGL resources associated with this VAO.
     */
    @Override
    public void free() {
        delete();
    }
}