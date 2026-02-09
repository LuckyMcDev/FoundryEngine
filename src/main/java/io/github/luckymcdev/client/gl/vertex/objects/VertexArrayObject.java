package io.github.luckymcdev.client.gl.vertex.objects;


import io.github.luckymcdev.client.gl.GlDispatch;
import io.github.luckymcdev.client.gl.OpenGlObject;

public class VertexArrayObject extends OpenGlObject {

    public VertexArrayObject() {
        this.pointer = GlDispatch.glGenVertexArrays();
    }

    public void bind() {
        GlDispatch.glBindVertexArray(this.pointer);
    }

    public void unbind() {
        GlDispatch.glBindVertexArray(0);
    }

    public void delete() {
        GlDispatch.glDeleteVertexArrays(this.pointer);
    }

    @Override
    public void free() {
        delete();
    }
}