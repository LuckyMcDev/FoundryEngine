package io.github.luckymcdev.client.gl.vertex;

public class Vertices {

    public static final float[] QUAD_VERTICES = {
            -1.0f, -1.0f,
            1.0f, -1.0f,
            1.0f,  1.0f,
            -1.0f, -1.0f,
            1.0f,  1.0f,
            -1.0f,  1.0f
    };

    public static final float[] TRIANGLE_VERTICES = {
            0.0f, 0.5f,
            -0.5f, -0.5f,
            0.5f, -0.5f
    };

    public static final VertexMesh FULLSCREEN_QUAD = VertexData.builder()
            .pos(-1.0f, -1.0f).uv(0.0f, 0.0f).end()
            .pos( 1.0f, -1.0f).uv(1.0f, 0.0f).end()
            .pos( 1.0f,  1.0f).uv(1.0f, 1.0f).end()
            .pos(-1.0f,  1.0f).uv(0.0f, 1.0f).end()
            .buildMesh(new int[]{
                    0, 1, 2,
                    2, 3, 0
            });

    /**
     * A simple struct holding vertex data and metadata
     */
    public record VertexMesh(float[] vertices, int[] indices, int vertexCount) {
        public boolean isIndexed() { return indices != null; }
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