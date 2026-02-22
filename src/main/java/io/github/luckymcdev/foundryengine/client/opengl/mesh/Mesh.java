package io.github.luckymcdev.foundryengine.client.opengl.mesh;

import io.github.luckymcdev.foundryengine.client.opengl.GlDispatch;
import io.github.luckymcdev.foundryengine.client.opengl.objects.ElementBufferObject;
import io.github.luckymcdev.foundryengine.client.opengl.objects.VertexArrayObject;
import io.github.luckymcdev.foundryengine.client.opengl.objects.VertexBufferObject;
import org.jetbrains.annotations.ApiStatus;

import static org.lwjgl.opengl.GL33.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL33.GL_UNSIGNED_INT;

/**
 * A GPU mesh. Upload vertex data once, draw many times.
 */
@ApiStatus.Experimental
public class Mesh {

    private final VertexArrayObject vao;
    private final VertexBufferObject vbo;
    private final ElementBufferObject ebo;
    private final DrawMode drawMode;
    private final int indexCount;

    /**
     * Creates a mesh, optionally running MeshOptimizer over the data before uploading.
     *
     * @param vertexData the vertex data (must be indexed)
     * @param layout     the vertex attribute layout — must match how {@code vertexData} was built
     * @param drawMode   the GL primitive type to use when drawing
     */
    public Mesh(VertexData vertexData, VertexLayout layout, DrawMode drawMode) {
        if (!vertexData.isIndexed()) {
            throw new IllegalArgumentException("Mesh requires indexed VertexData. Supply indices via VertexData.Builder#indices(...).");
        }

        this.drawMode = drawMode;
        this.indexCount = vertexData.indices().length;

        vao = new VertexArrayObject();
        vao.bind();

        vbo = new VertexBufferObject();
        ebo = new ElementBufferObject();

        vbo.uploadData(vertexData.vertices(), GL_STATIC_DRAW);
        ebo.uploadData(vertexData.indices(), GL_STATIC_DRAW);


        layout.apply();
        vao.unbind();
    }

    /**
     * Draws the Mesh.
     */
    public void draw() {
        vao.bind();
        GlDispatch.glDrawElements(drawMode.glEnum(), indexCount, GL_UNSIGNED_INT, 0L);
        vao.unbind();
    }

    public void delete() {
        vao.delete();
        vbo.delete();
        ebo.delete();
    }
}