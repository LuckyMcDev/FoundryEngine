package io.github.luckymcdev.foundryengine.client.opengl.vertex;

import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.objects.ElementBufferObject;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.objects.VertexArrayObject;
import io.github.luckymcdev.foundryengine.client.opengl.vertex.objects.VertexBufferObject;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.meshoptimizer.MeshOptimizer;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL33.GL_UNSIGNED_INT;

public class Mesh {
    private final VertexArrayObject vao;
    private final VertexBufferObject vbo;
    private final int drawMode;
    private final ElementBufferObject ebo;
    private final int indexCount;

    public Mesh(float[] vertices, int[] indices, VertexLayout layout, int drawMode, boolean optimize) {
        this.drawMode = drawMode;

        if (optimize) {
            IntBuffer indexBuffer = BufferUtils.createIntBuffer(indices.length);
            indexBuffer.put(indices).flip();

            int stride = layout.getStride();
            int vCount = vertices.length / (stride / 4);

            MeshOptimizer.meshopt_optimizeVertexCache(indexBuffer, indexBuffer, vCount);

            ByteBuffer vertexByteBuffer = BufferUtils.createByteBuffer(vertices.length * 4);
            FloatBuffer vertexFloatView = vertexByteBuffer.asFloatBuffer();
            vertexFloatView.put(vertices).flip();

            MeshOptimizer.meshopt_optimizeVertexFetch(
                    vertexByteBuffer,
                    indexBuffer,
                    vertexByteBuffer,
                    vCount,
                    stride
            );

            this.indexCount = indices.length;

            vao = new VertexArrayObject();
            vao.bind();

            vbo = new VertexBufferObject();
            vbo.uploadData(vertexFloatView, GL_STATIC_DRAW);

            ebo = new ElementBufferObject();
            ebo.uploadData(indexBuffer, GL_STATIC_DRAW);
        } else {
            this.indexCount = indices.length;

            vao = new VertexArrayObject();
            vao.bind();

            vbo = new VertexBufferObject();
            vbo.uploadData(vertices, GL_STATIC_DRAW);

            ebo = new ElementBufferObject();
            ebo.uploadData(indices, GL_STATIC_DRAW);
        }

        layout.apply();
        vao.unbind();
    }

    public void draw() {
        vao.bind();

        GlDispatch.glDrawElements(drawMode, indexCount, GL_UNSIGNED_INT, 0L);

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