package de.luckymcdev.foundryengine.client.opengl.mesh;

/**
 * Holds raw vertex data (float array + index array).
 */
public final class VertexData {

    private final float[] vertices;
    private final int[] indices;
    private final int vertexCount;

    private VertexData(Builder builder) {
        float[] src = builder.data;
        this.vertices = new float[builder.index];
        System.arraycopy(src, 0, this.vertices, 0, builder.index);

        this.indices = builder.indices;
        this.vertexCount = builder.verticesAdded;
    }

    public static Builder builder() {
        return new Builder(64);
    }

    public static Builder builder(int initialFloatCapacity) {
        return new Builder(initialFloatCapacity);
    }

    /**
     * Raw interleaved vertex floats.
     */
    public float[] vertices() {
        return vertices;
    }

    /**
     * Element indices. May be {@code null} if no indices were provided.
     */
    public int[] indices() {
        return indices;
    }

    /**
     * Number of complete vertices added via {@link Builder#end()}.
     */
    public int vertexCount() {
        return vertexCount;
    }

    /**
     * Whether this data set carries index data.
     */
    public boolean isIndexed() {
        return indices != null && indices.length > 0;
    }

    public static final class Builder {

        private float[] data;
        private int index;
        private int verticesAdded;

        // Per-vertex staging fields
        private float x, y, z;
        private float r, g, b, a;
        private float u, v;
        private float nx, ny, nz;
        private float tx, ty, tz;
        private float bx, by, bz;

        private boolean hasPos, hasPos2D, hasColor, hasColorRGBA, hasUv, hasNormal, hasTangent, hasBitangent;

        private int[] indices;

        private Builder(int initialCapacity) {
            this.data = new float[initialCapacity];
        }

        /**
         * 3-component position (x, y, z).
         */
        public Builder pos(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.hasPos = true;
            return this;
        }

        /**
         * 2-component position (x, y) — writes only two floats.
         */
        public Builder pos(float x, float y) {
            this.x = x;
            this.y = y;
            this.hasPos2D = true;
            return this;
        }

        /**
         * RGB colour (3 floats).
         */
        public Builder color(float r, float g, float b) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.hasColor = true;
            return this;
        }

        /**
         * RGBA colour (4 floats).
         */
        public Builder color(float r, float g, float b, float a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            this.hasColorRGBA = true;
            return this;
        }

        /**
         * Texture coordinate (u, v).
         */
        public Builder uv(float u, float v) {
            this.u = u;
            this.v = v;
            this.hasUv = true;
            return this;
        }

        /**
         * Normal vector (nx, ny, nz).
         */
        public Builder normal(float nx, float ny, float nz) {
            this.nx = nx;
            this.ny = ny;
            this.nz = nz;
            this.hasNormal = true;
            return this;
        }

        /**
         * Tangent vector (tx, ty, tz).
         */
        public Builder tangent(float tx, float ty, float tz) {
            this.tx = tx;
            this.ty = ty;
            this.tz = tz;
            this.hasTangent = true;
            return this;
        }

        /**
         * Bitangent vector (bx, by, bz).
         */
        public Builder bitangent(float bx, float by, float bz) {
            this.bx = bx;
            this.by = by;
            this.bz = bz;
            this.hasBitangent = true;
            return this;
        }

        /**
         * Finalizes the current vertex and appends its floats to the backing buffer.
         * The order in which attributes are written mirrors the order in which the
         * setters were called — keep this consistent with your {@link VertexLayout}.
         */
        public Builder end() {
            int floatsNeeded = 0;
            if (hasPos) floatsNeeded += 3;
            if (hasPos2D) floatsNeeded += 2;
            if (hasColor) floatsNeeded += 3;
            if (hasColorRGBA) floatsNeeded += 4;
            if (hasUv) floatsNeeded += 2;
            if (hasNormal) floatsNeeded += 3;
            if (hasTangent) floatsNeeded += 3;
            if (hasBitangent) floatsNeeded += 3;

            ensureCapacity(index + floatsNeeded);

            if (hasPos) {
                data[index++] = x;
                data[index++] = y;
                data[index++] = z;
            }
            if (hasPos2D) {
                data[index++] = x;
                data[index++] = y;
            }
            if (hasColor) {
                data[index++] = r;
                data[index++] = g;
                data[index++] = b;
            }
            if (hasColorRGBA) {
                data[index++] = r;
                data[index++] = g;
                data[index++] = b;
                data[index++] = a;
            }
            if (hasUv) {
                data[index++] = u;
                data[index++] = v;
            }
            if (hasNormal) {
                data[index++] = nx;
                data[index++] = ny;
                data[index++] = nz;
            }
            if (hasTangent) {
                data[index++] = tx;
                data[index++] = ty;
                data[index++] = tz;
            }
            if (hasBitangent) {
                data[index++] = bx;
                data[index++] = by;
                data[index++] = bz;
            }

            verticesAdded++;

            // Reset staging flags
            hasPos = false;
            hasPos2D = false;
            hasColor = false;
            hasColorRGBA = false;
            hasUv = false;
            hasNormal = false;
            hasTangent = false;
            hasBitangent = false;

            return this;
        }

        /**
         * Supplies the element index array. Call once before {@link #build()}.
         */
        public Builder indices(int... indices) {
            this.indices = indices;
            return this;
        }

        public VertexData build() {
            return new VertexData(this);
        }

        private void ensureCapacity(int required) {
            if (required > data.length) {
                float[] grown = new float[Math.max(required, data.length * 2)];
                System.arraycopy(data, 0, grown, 0, index);
                data = grown;
            }
        }
    }
}