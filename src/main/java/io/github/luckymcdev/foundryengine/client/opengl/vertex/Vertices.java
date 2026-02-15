package io.github.luckymcdev.foundryengine.client.opengl.vertex;

import org.lwjgl.util.par.ParShapes;
import org.lwjgl.util.par.ParShapesMesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;

public class Vertices {

    public static final float[] QUAD_VERTICES = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, -1.0f,
            1.0f, 1.0f,
            -1.0f, 1.0f
    };

    public static final float[] TRIANGLE_VERTICES = {
            0.0f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f
    };

    public static final VertexMesh FULLSCREEN_QUAD = VertexData.builder()
            .pos(-1.0f, -1.0f).uv(0.0f, 0.0f).end()
            .pos(1.0f, -1.0f).uv(1.0f, 0.0f).end()
            .pos(1.0f, 1.0f).uv(1.0f, 1.0f).end()
            .pos(-1.0f, 1.0f).uv(0.0f, 1.0f).end()
            .buildMesh(new int[]{
                    0, 1, 2,
                    2, 3, 0
            });

    private static VertexMesh wrapParCall(Supplier<ParShapesMesh> meshSupplier) {
        ParShapesMesh mesh = null;
        try {
            mesh = meshSupplier.get();
            if (mesh == null) throw new RuntimeException("Failed to generate mesh");

            return convertParMesh(mesh);
        } finally {
            if (mesh != null) {
                ParShapes.par_shapes_free_mesh(mesh);
            }
        }
    }

    public static VertexMesh createSphere(int slices, int stacks) {
        return wrapParCall(() -> ParShapes.par_shapes_create_parametric_sphere(slices, stacks));
    }

    private static VertexMesh convertParMesh(ParShapesMesh mesh) {
        int vCount = mesh.npoints();
        int tCount = mesh.ntriangles();

        FloatBuffer positions = mesh.points(vCount * 3);
        FloatBuffer uvs = mesh.tcoords(vCount * 2);
        IntBuffer triangleIndices = mesh.triangles(tCount * 3);

        VertexData builder = VertexData.builder(vCount * 5);
        for (int i = 0; i < vCount; i++) {
            builder.pos(
                    positions.get(i * 3),
                    positions.get(i * 3 + 1),
                    positions.get(i * 3 + 2)
            );

            if (uvs != null) {
                builder.uv(uvs.get(i * 2), uvs.get(i * 2 + 1));
            }

            builder.end();
        }

        int[] indices = new int[tCount * 3];
        triangleIndices.get(indices);

        return builder.buildMesh(indices);
    }


    /**
     * A simple struct holding vertex data
     */
    public record VertexMesh(float[] vertices, int[] indices, int vertexCount) {
    }

    /**
     * Builder for creating vertex data
     */
    public static class VertexData {
        private float[] data;
        private int index;
        private int verticesAdded = 0;
        private float x, y, z;
        private float u, v;
        private boolean hasPos, hasUv;

        private VertexData(int initialCapacity) {
            this.data = new float[initialCapacity];
            this.index = 0;
            this.verticesAdded = 0;
        }

        public static VertexData builder() {
            return new VertexData(64);
        }

        public static VertexData builder(int capacity) {
            return new VertexData(capacity);
        }

        public VertexData pos(float x, float y) {
            this.x = x;
            this.y = y;
            this.z = 0.0f;
            this.hasPos = true;
            return this;
        }

        public VertexData pos(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.hasPos = true;
            return this;
        }

        public VertexData uv(float u, float v) {
            this.u = u;
            this.v = v;
            this.hasUv = true;
            return this;
        }

        public VertexData end() {
            ensureCapacity(index + 4);

            if (hasPos) {
                data[index++] = x;
                data[index++] = y;
            }
            if (hasUv) {
                data[index++] = u;
                data[index++] = v;
            }

            verticesAdded++;

            // Reset state
            hasPos = false;
            hasUv = false;

            return this;
        }

        private void ensureCapacity(int required) {
            if (required > data.length) {
                float[] newData = new float[Math.max(required, data.length * 2)];
                System.arraycopy(data, 0, newData, 0, index);
                data = newData;
            }
        }

        public float[] build() {
            float[] result = new float[index];
            System.arraycopy(data, 0, result, 0, index);
            return result;
        }

        public int vertexCount() {
            return verticesAdded;
        }

        public VertexMesh buildMesh(int[] indices) {
            return new VertexMesh(build(), indices, vertexCount());
        }
    }
}