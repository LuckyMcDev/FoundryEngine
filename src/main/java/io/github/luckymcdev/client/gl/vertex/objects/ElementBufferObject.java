package io.github.luckymcdev.client.gl.vertex.objects;

import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlObject;

import static org.lwjgl.opengl.GL33.*;

public class ElementBufferObject extends OpenGlObject {

    public ElementBufferObject() {
        this.pointer =GlDispatch.glGenBuffers();
    }

    public void bind() {
        GlDispatch.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.pointer);
    }

    public void unbind() {
        GlDispatch.glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void uploadData(int[] data, int usage) {
        bind();
        GlDispatch.glBufferData(GL_ELEMENT_ARRAY_BUFFER, data, usage);
    }

    public void delete() {
        GlDispatch.glDeleteBuffers(this.pointer);
    }

    @Override
    public void free() {
        delete();
    }
}