package io.github.luckymcdev.foundryengine.client.opengl.mesh;

/**
 * Common pre-built {@link VertexData} constants and procedural mesh generators.
 */
public final class Vertices {

    /**
     * A full-screen quad covering NDC [-1, 1], with UV [0, 1]. Layout: {@link VertexLayout#POS_TEX_2D}.
     */
    public static final VertexData FULLSCREEN_QUAD = VertexData.builder()
            .pos(-1.0f, -1.0f).uv(0.0f, 0.0f).end()
            .pos(1.0f, -1.0f).uv(1.0f, 0.0f).end()
            .pos(1.0f, 1.0f).uv(1.0f, 1.0f).end()
            .pos(-1.0f, 1.0f).uv(0.0f, 1.0f).end()
            .indices(0, 1, 2, 2, 3, 0)
            .build();

    private Vertices() {
    }
}