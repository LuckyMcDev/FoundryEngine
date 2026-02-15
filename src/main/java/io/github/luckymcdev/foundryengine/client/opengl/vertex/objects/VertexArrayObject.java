package io.github.luckymcdev.foundryengine.client.opengl.vertex.objects;


import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.OpenGlObject;

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