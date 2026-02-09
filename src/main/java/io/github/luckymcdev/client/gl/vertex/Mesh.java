package io.github.luckymcdev.client.gl.vertex;

import io.github.luckymcdev.client.gl.vertex.objects.ElementBufferObject;
import io.github.luckymcdev.client.gl.vertex.objects.VertexArrayObject;
import io.github.luckymcdev.client.gl.vertex.objects.VertexBufferObject;

import static org.lwjgl.opengl.GL33.*;

public class Mesh {
    private final VertexArrayObject vao;
    private final VertexBufferObject vbo;
    private ElementBufferObject ebo;
    private int vertexCount;
    private int indexCount;
    private final boolean indexed;
    private final int drawMode;

    public Mesh(float[] vertices, int vertexCount, VertexLayout layout) {
        this(vertices, vertexCount, layout, GL_TRIANGLES);
    }

    // Constructor with custom draw mode
    public Mesh(float[] vertices, int vertexCount, VertexLayout layout, int drawMode) {
        this.indexed = false;
        this.vertexCount = vertexCount;
        this.drawMode = drawMode;

        vao = new VertexArrayObject();
        vao.bind();

        vbo = new VertexBufferObject();
        vbo.uploadData(vertices, GL_STATIC_DRAW);

        layout.apply();

        vao.unbind();
        vbo.unbind();
    }

    // Indexed constructor with layout
    public Mesh(float[] vertices, int[] indices, VertexLayout layout) {
        this(vertices, indices, layout, GL_TRIANGLES);
    }

    // Indexed constructor with custom draw mode
    public Mesh(float[] vertices, int[] indices, VertexLayout layout, int drawMode) {
        this.indexed = true;
        this.indexCount = indices.length;
        this.drawMode = drawMode;

        vao = new VertexArrayObject();
        vao.bind();

        vbo = new VertexBufferObject();
        vbo.uploadData(vertices, GL_STATIC_DRAW);

        ebo = new ElementBufferObject();
        ebo.uploadData(indices, GL_STATIC_DRAW);

        layout.apply();

        vao.unbind();
        vbo.unbind();
    }

    public void draw() {
        vao.bind();

        if (indexed) {
            glDrawElements(drawMode, indexCount, GL_UNSIGNED_INT, 0L);
        } else {
            glDrawArrays(drawMode, 0, vertexCount);
        }

        vao.unbind();
    }

    public void delete() {
        vao.delete();
        vbo.delete();
        if (ebo != null) {
            ebo.delete();
        }
    }
}