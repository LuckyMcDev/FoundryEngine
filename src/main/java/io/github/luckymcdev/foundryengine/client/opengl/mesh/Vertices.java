package io.github.luckymcdev.foundryengine.client.opengl.mesh;

import org.lwjgl.util.par.ParShapes;
import org.lwjgl.util.par.ParShapesMesh;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.function.Supplier;

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

    /**
     * Generates a UV sphere. Layout: {@link VertexLayout#POS_TEX}.
     *
     * @param slices longitudinal subdivisions
     * @param stacks latitudinal subdivisions
     */
    public static VertexData createSphere(int slices, int stacks) {
        return wrapParCall(() -> ParShapes.par_shapes_create_parametric_sphere(slices, stacks));
    }

    private static VertexData wrapParCall(Supplier<ParShapesMesh> supplier) {
        ParShapesMesh mesh = null;
        try {
            mesh = supplier.get();
            if (mesh == null) throw new RuntimeException("par_shapes returned a null mesh");
            return convertParMesh(mesh);
        } finally {
            if (mesh != null) ParShapes.par_shapes_free_mesh(mesh);
        }
    }

    private static VertexData convertParMesh(ParShapesMesh mesh) {
        int vCount = mesh.npoints();
        int tCount = mesh.ntriangles();

        FloatBuffer positions = mesh.points(vCount * 3);
        FloatBuffer uvs = mesh.tcoords(vCount * 2);
        IntBuffer triIdx = mesh.triangles(tCount * 3);

        VertexData.Builder builder = VertexData.builder(vCount * 5);

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
        triIdx.get(indices);

        return builder.indices(indices).build();
    }
}