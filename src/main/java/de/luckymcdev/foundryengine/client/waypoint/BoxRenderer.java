package de.luckymcdev.foundryengine.client.waypoint;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.DepthStencilState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.CompareOp;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.mixin.invoker.RenderPipelinesInvoker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.OptionalDouble;
import java.util.OptionalInt;

public class BoxRenderer {
    private static final RenderPipeline FILLED_THROUGH_WALLS = RenderPipelinesInvoker.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(Common.MODID, "pipeline/debug_filled_box_through_walls"))
                    .withDepthStencilState(new DepthStencilState(CompareOp.ALWAYS_PASS, false))
                    .build()
    );

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1, 1, 1, 1);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();
    // Geometry data (static, shared across all instances)
    private static final float EDGE_WIDTH = 1f / 16;
    private static final float EDGE_ALPHA = 1f;
    // Face definitions: each is a quad (4 vertices) with local coordinates in [0,1]³.
    // The actual orientation is applied in drawRect.
    private static final float[][] BASE_FACE = {
            {0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0}
    };
    private static final float[][] BOTTOM_EDGE = {
            {0, 0, EDGE_WIDTH}, {0, 0, 1 - EDGE_WIDTH}, {0, EDGE_WIDTH, 1 - EDGE_WIDTH}, {0, EDGE_WIDTH, EDGE_WIDTH}
    };
    private static final float[][] TOP_EDGE = {
            {0, 1, 1 - EDGE_WIDTH}, {0, 1, EDGE_WIDTH}, {0, 1 - EDGE_WIDTH, EDGE_WIDTH}, {0, 1 - EDGE_WIDTH, 1 - EDGE_WIDTH}
    };
    private static final float[][] RIGHT_EDGE = {
            {0, 1 - EDGE_WIDTH, 1 - EDGE_WIDTH}, {0, EDGE_WIDTH, 1 - EDGE_WIDTH}, {0, EDGE_WIDTH, 1}, {0, 1 - EDGE_WIDTH, 1}
    };
    private static final float[][] LEFT_EDGE = {
            {0, 1 - EDGE_WIDTH, 0}, {0, EDGE_WIDTH, 0}, {0, EDGE_WIDTH, EDGE_WIDTH}, {0, 1 - EDGE_WIDTH, EDGE_WIDTH}
    };
    private static final float[][] BL_CORNER = {
            {0, 0, EDGE_WIDTH}, {0, EDGE_WIDTH, EDGE_WIDTH}, {0, EDGE_WIDTH, 0}, {0, 0, 0}
    };
    private static final float[][] BR_CORNER = {
            {0, 0, 1}, {0, EDGE_WIDTH, 1}, {0, EDGE_WIDTH, 1 - EDGE_WIDTH}, {0, 0, 1 - EDGE_WIDTH}
    };
    private static final float[][] TL_CORNER = {
            {0, 1, 0}, {0, 1 - EDGE_WIDTH, 0}, {0, 1 - EDGE_WIDTH, EDGE_WIDTH}, {0, 1, EDGE_WIDTH}
    };
    private static final float[][] TR_CORNER = {
            {0, 1, 1 - EDGE_WIDTH}, {0, 1 - EDGE_WIDTH, 1 - EDGE_WIDTH}, {0, 1 - EDGE_WIDTH, 1}, {0, 1, 1}
    };
    // Mapping of axis to which edges/corners are needed. Stored as arrays of {edgeQuad, sideIndices, cornerQuad, diagIndices}
    // This data comes from the original hardcoded if‑chains. We'll keep it as simple static data.
    private static final EdgeCornerData[] EDGE_CORNER_DATA = new EdgeCornerData[3];

    static {
        // Axis 0 (Z)
        EDGE_CORNER_DATA[0] = new EdgeCornerData(
                new float[][][]{LEFT_EDGE, BOTTOM_EDGE, RIGHT_EDGE, TOP_EDGE},
                new int[]{1, 2, 4, 5},
                new float[][][]{BL_CORNER, TL_CORNER, BR_CORNER, TR_CORNER},
                new int[]{8, 9, 10, 11}
        );
        // Axis 1 (X)
        EDGE_CORNER_DATA[1] = new EdgeCornerData(
                new float[][][]{LEFT_EDGE, BOTTOM_EDGE, RIGHT_EDGE, TOP_EDGE},
                new int[]{2, 0, 5, 3},
                new float[][][]{BL_CORNER, BR_CORNER, TL_CORNER, TR_CORNER},
                new int[]{1, 3, 5, 7}
        );
        // Axis 2 (Y)
        EDGE_CORNER_DATA[2] = new EdgeCornerData(
                new float[][][]{LEFT_EDGE, BOTTOM_EDGE, RIGHT_EDGE, TOP_EDGE},
                new int[]{0, 1, 3, 4},
                new float[][][]{BL_CORNER, BR_CORNER, TL_CORNER, TR_CORNER},
                new int[]{0, 4, 2, 6}
        );
    }

    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;

    private static void draw(Minecraft client, RenderPipeline pipeline, MeshData builtBuffer,
                             MeshData.DrawState drawParams, GpuBuffer vertices, VertexFormat format, Matrix4fc modelView) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shapeBuffer = RenderSystem.getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeBuffer.getBuffer(drawParams.indexCount());
            indexType = shapeBuffer.type();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(modelView, COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> Common.MODID + " waypoint rendering",
                        client.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        client.getMainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty())) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawParams.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void renderWaypoints(RenderLevelStageEvent.AfterLevel context) {
        for (CWaypoint waypoint : WaypointManager.getWaypoints()) {
            extractAndDrawWaypoint(context, waypoint);
        }
    }

    private void extractAndDrawWaypoint(RenderLevelStageEvent.AfterLevel context, CWaypoint waypoint) {
        if (renderWaypoint(context, waypoint)) {
            Vec3 camera = context.getLevelRenderState().cameraRenderState.pos;
            Matrix4f fullViewMatrix = new Matrix4f(context.getLevelRenderState().cameraRenderState.viewRotationMatrix);
            fullViewMatrix.translate((float) -camera.x, (float) -camera.y, (float) -camera.z);
            drawFilledThroughWalls(Minecraft.getInstance(), FILLED_THROUGH_WALLS, fullViewMatrix);
        }
    }

    private boolean renderWaypoint(RenderLevelStageEvent.AfterLevel context, CWaypoint waypoint) {
        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, FILLED_THROUGH_WALLS.getVertexFormatMode(), FILLED_THROUGH_WALLS.getVertexFormat());
        }
        return renderCube(new Matrix4f(), buffer,
                waypoint.getX(), waypoint.getY(), waypoint.getZ(),
                waypoint.getColour().r(), waypoint.getColour().g(), waypoint.getColour().b(), waypoint.getColour().a());
    }

    // Draws a single face (axis, direction) with its edges and corners.
    private void drawFace(Matrix4fc matrix, BufferBuilder buffer, boolean[] sides, boolean[] diags,
                          int axis, int dir, float x, float y, float z, float r, float g, float b, float a) {
        // Main body
        drawRect(matrix, buffer, BASE_FACE, axis, dir, x, y, z, r, g, b, a);

        EdgeCornerData data = EDGE_CORNER_DATA[axis];
        // Edges
        for (int i = 0; i < 4; i++) {
            if (sides[data.sideIndices[i]]) {
                drawRect(matrix, buffer, data.edgeQuads[i], axis, dir, x, y, z, r, g, b, EDGE_ALPHA);
            }
        }
        // Corners
        for (int i = 0; i < 4; i++) {
            boolean side1 = sides[data.sideIndices[i]];
            boolean side2 = sides[data.sideIndices[(i + 1) % 4]];
            boolean missingDiagonal = !diags[data.diagIndices[i]];
            if (side1 || side2 || missingDiagonal) {
                drawRect(matrix, buffer, data.cornerQuads[i], axis, dir, x, y, z, r, g, b, EDGE_ALPHA);
            }
        }
    }

    // Draws a single rectangular (quad) piece, transforming local coordinates into world space.
    // The quad is given in a coordinate system where the face is on the plane of the given axis,
    // and the other two axes run from 0 to 1. The direction moves the whole face outward.
    private void drawRect(Matrix4fc matrix, BufferBuilder buffer, float[][] quad, int axis, int dir,
                          float x, float y, float z, float r, float g, float b, float a) {
        int i = (axis + 2) % 3;
        int j = (axis + 1) % 3;
        int k = axis;

        for (int vert = 0; vert < 4; vert++) {
            int v = (dir == 1) ? vert : 3 - vert; // reverse order for opposite direction
            float vertX = quad[v][i] + x;
            float vertY = quad[v][j] + y;
            float vertZ = quad[v][k] + z;

            if (axis == 0) vertZ += dir;
            else if (axis == 1) vertX += dir;
            else vertY += dir;

            buffer.addVertex(matrix, vertX, vertY, vertZ).setColor(r, g, b, a);
        }
    }

    private void drawCube(Matrix4fc matrix, BufferBuilder buffer, boolean[] sides, boolean[] diags,
                          float x, float y, float z, float r, float g, float b, float a) {
        // Order: [0] Z-, [1] X-, [2] Y-, [3] Z+, [4] X+, [5] Y+
        if (sides[0]) drawFace(matrix, buffer, sides, diags, 0, 0, x, y, z, r, g, b, a);
        if (sides[1]) drawFace(matrix, buffer, sides, diags, 1, 0, x, y, z, r, g, b, a);
        if (sides[2]) drawFace(matrix, buffer, sides, diags, 2, 0, x, y, z, r, g, b, a);
        if (sides[3]) drawFace(matrix, buffer, sides, diags, 0, 1, x, y, z, r, g, b, a);
        if (sides[4]) drawFace(matrix, buffer, sides, diags, 1, 1, x, y, z, r, g, b, a);
        if (sides[5]) drawFace(matrix, buffer, sides, diags, 2, 1, x, y, z, r, g, b, a);
    }

    private boolean renderCube(Matrix4fc positionMatrix, BufferBuilder buffer,
                               float x, float y, float z, float r, float g, float b, float a) {
        boolean[] sides = {
                !WaypointManager.waypointExists(x, y, z - 1),
                !WaypointManager.waypointExists(x - 1, y, z),
                !WaypointManager.waypointExists(x, y - 1, z),
                !WaypointManager.waypointExists(x, y, z + 1),
                !WaypointManager.waypointExists(x + 1, y, z),
                !WaypointManager.waypointExists(x, y + 1, z)
        };
        boolean[] diagonals = {
                WaypointManager.waypointExists(x - 1, y, z - 1),
                WaypointManager.waypointExists(x, y - 1, z - 1),
                WaypointManager.waypointExists(x + 1, y, z - 1),
                WaypointManager.waypointExists(x, y + 1, z - 1),
                WaypointManager.waypointExists(x - 1, y, z + 1),
                WaypointManager.waypointExists(x, y - 1, z + 1),
                WaypointManager.waypointExists(x + 1, y, z + 1),
                WaypointManager.waypointExists(x, y + 1, z + 1),
                WaypointManager.waypointExists(x - 1, y - 1, z),
                WaypointManager.waypointExists(x - 1, y + 1, z),
                WaypointManager.waypointExists(x + 1, y - 1, z),
                WaypointManager.waypointExists(x + 1, y + 1, z)
        };

        boolean anySide = false;
        for (boolean side : sides)
            if (side) {
                anySide = true;
                break;
            }
        if (!anySide) return false;

        drawCube(positionMatrix, buffer, sides, diagonals, x, y, z, r, g, b, a);
        return true;
    }

    private void drawFilledThroughWalls(Minecraft client, RenderPipeline pipeline, Matrix4fc fullViewMatrix) {
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParams = builtBuffer.drawState();
        VertexFormat format = drawParams.format();

        GpuBuffer vertices = upload(drawParams, format, builtBuffer);
        draw(client, pipeline, builtBuffer, drawParams, vertices, format, fullViewMatrix);

        vertexBuffer.rotate();
        buffer = null;
    }

    private GpuBuffer upload(MeshData.DrawState drawParams, VertexFormat format, MeshData builtBuffer) {
        int vertexBufferSize = drawParams.vertexCount() * format.getVertexSize();
        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            vertexBuffer = new MappableRingBuffer(() -> Common.MODID + " waypoint buffer",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE, vertexBufferSize);
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView view = encoder.mapBuffer(vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()), false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), view.data());
        }
        return vertexBuffer.currentBuffer();
    }

    public void close() {
        ALLOCATOR.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }

    private record EdgeCornerData(float[][][] edgeQuads, int[] sideIndices, float[][][] cornerQuads,
                                  int[] diagIndices) {
    }
}