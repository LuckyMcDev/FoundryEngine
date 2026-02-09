package io.github.luckymcdev.client.gl.vertex.objects;

import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlObject;

import static org.lwjgl.opengl.GL33.*;

public class VertexBufferObject extends OpenGlObject {

    public VertexBufferObject() {
        this.pointer = GlDispatch.glGenBuffers();
    }

    public void bind() {
        GlDispatch.glBindBuffer(GL_ARRAY_BUFFER, this.pointer);
    }

    public void unbind() {
        GlDispatch.glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    public void uploadData(float[] data, int usage) {
        bind();
        GlDispatch.glBufferData(GL_ARRAY_BUFFER, data, usage);
    }

    public void delete() {
        GlDispatch.glDeleteBuffers(this.pointer);
    }

    @Override
    public void free() {
        delete();
    }
}