package io.github.luckymcdev.client.gl.vertex;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33.*;

public class VertexLayout {
    // Common predefined layouts
    public static final VertexLayout POS = new Builder()
            .position()
            .build();

    public static final VertexLayout POS_2D = new Builder()
            .position2D()
            .build();

    public static final VertexLayout POS_COLOR = new Builder()
            .position()
            .color()
            .build();

    public static final VertexLayout POS_COLOR_2D = new Builder()
            .position2D()
            .color()
            .build();

    public static final VertexLayout POS_TEX = new Builder()
            .position()
            .texCoord()
            .build();

    public static final VertexLayout POS_TEX_2D = new Builder()
            .position2D()
            .texCoord()
            .build();

    public static final VertexLayout POS_COLOR_TEX = new Builder()
            .position()
            .color()
            .texCoord()
            .build();

    public static final VertexLayout POS_COLOR_TEX_2D = new Builder()
            .position2D()
            .color()
            .texCoord()
            .build();

    public static final VertexLayout POS_NORMAL = new Builder()
            .position()
            .normal()
            .build();

    public static final VertexLayout POS_NORMAL_TEX = new Builder()
            .position()
            .normal()
            .texCoord()
            .build();

    public static final VertexLayout POS_NORMAL_COLOR = new Builder()
            .position()
            .normal()
            .color()
            .build();

    public static final VertexLayout POS_NORMAL_COLOR_TEX = new Builder()
            .position()
            .normal()
            .color()
            .texCoord()
            .build();

    public static final VertexLayout POS_NORMAL_TANGENT_TEX = new Builder()
            .position()
            .normal()
            .tangent()
            .texCoord()
            .build();

    public static final VertexLayout POS_COLOR_RGBA = new Builder()
            .position()
            .colorRGBA()
            .build();

    public static final VertexLayout POS_COLOR_RGBA_TEX = new Builder()
            .position()
            .colorRGBA()
            .texCoord()
            .build();

    private final List<VertexAttribute> attributes;
    private final int stride;

    private VertexLayout(Builder builder) {
        this.attributes = new ArrayList<>(builder.attributes);
        this.stride = builder.currentOffset;
    }

    public void apply() {
        for (VertexAttribute attr : attributes) {
            glVertexAttribPointer(
                    attr.location,
                    attr.componentCount,
                    GL_FLOAT,
                    false,
                    stride,
                    attr.offset
            );
            glEnableVertexAttribArray(attr.location);
        }
    }

    public int getStride() {
        return stride;
    }

    public int getStrideInFloats() {
        return stride / Float.BYTES;
    }

    public static class Builder {
        private final List<VertexAttribute> attributes = new ArrayList<>();
        private int currentOffset = 0;
        private int nextLocation = 0;

        public Builder position() {
            return addAttribute(3); // x, y, z
        }

        public Builder position2D() {
            return addAttribute(2); // x, y
        }

        public Builder color() {
            return addAttribute(3); // r, g, b
        }

        public Builder colorRGBA() {
            return addAttribute(4); // r, g, b, a
        }

        public Builder texCoord() {
            return addAttribute(2); // u, v
        }

        public Builder normal() {
            return addAttribute(3); // nx, ny, nz
        }

        public Builder tangent() {
            return addAttribute(3); // tx, ty, tz
        }

        public Builder bitangent() {
            return addAttribute(3); // bx, by, bz
        }

        public Builder custom(int componentCount) {
            return addAttribute(componentCount);
        }

        private Builder addAttribute(int componentCount) {
            attributes.add(new VertexAttribute(
                    nextLocation++,
                    componentCount,
                    currentOffset
            ));
            currentOffset += componentCount * Float.BYTES;
            return this;
        }

        public VertexLayout build() {
            return new VertexLayout(this);
        }
    }

    private record VertexAttribute(int location, int componentCount, int offset) {
    }
}