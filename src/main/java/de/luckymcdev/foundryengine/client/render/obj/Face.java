package de.luckymcdev.foundryengine.client.render.obj;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.luckymcdev.foundryengine.client.render.MeshRenderer;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public record Face(List<Vertex> vertices) {
    private static void pushVertex(BufferBuilder buffer, Vertex vertex, PoseStack poseStack,
                                   float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();
        Vector3f pos = vertex.position();
        Vector3f normal = vertex.normal();

        buffer.addVertex(pose, pos.x(), pos.y(), pos.z())
                .setColor(r, g, b, a)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    private static void addVertex(VertexConsumer buffer, Vertex vertex, PoseStack poseStack, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Vector3f position = vertex.position();
        Vector3f normal = vertex.normal();
        Vector2f uv = vertex.uv();

        buffer.addVertex(pose, position.x(), position.y(), position.z());
        buffer.setColor(255, 255, 255, 255);
        buffer.setUv(uv.x, -uv.y);
        buffer.setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.setLight(packedLight);
        buffer.setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    /**
     * Emit this face's geometry directly into {@code buffer}.
     * Use this with {@link MeshRenderer} — the caller owns pipeline selection.
     *
     * @param buffer    the active {@link BufferBuilder} from a {@link MeshRenderer.DrawSession}
     * @param poseStack current pose (for position / normal transformation)
     * @param r         red   [0–1]
     * @param g         green [0–1]
     * @param b         blue  [0–1]
     * @param a         alpha [0–1]
     */
    public void buildVertices(BufferBuilder buffer, PoseStack poseStack, float r, float g, float b, float a) {
        int count = vertices.size();
        if (count == 3) {
            buildTriangle(buffer, poseStack, r, g, b, a);
        } else if (count == 4) {
            buildQuad(buffer, poseStack, r, g, b, a);
        } else if (count > 4) {
            buildNgon(buffer, poseStack, r, g, b, a);
        } else {
            Common.LOGGER.warn("Skipping face with invalid vertex count: {}", count);
        }
    }

    /**
     * Overload that uses full white + opaque alpha.
     */
    public void buildVertices(BufferBuilder buffer, PoseStack poseStack) {
        buildVertices(buffer, poseStack, 1f, 1f, 1f, 1f);
    }

    private void buildTriangle(BufferBuilder buffer, PoseStack poseStack, float r, float g, float b, float a) {
        for (Vertex v : vertices) pushVertex(buffer, v, poseStack, r, g, b, a);
        // Minecraft's QUADS mode needs 4 verts; duplicate the last to close the quad.
        pushVertex(buffer, vertices.get(0), poseStack, r, g, b, a);
    }

    private void buildQuad(BufferBuilder buffer, PoseStack poseStack, float r, float g, float b, float a) {
        for (Vertex v : vertices) pushVertex(buffer, v, poseStack, r, g, b, a);
    }

    private void buildNgon(BufferBuilder buffer, PoseStack poseStack, float r, float g, float b, float a) {
        Vertex anchor = vertices.getFirst();
        for (int i = 1; i < vertices.size() - 1; i++) {
            // Fan triangulation — each triangle still needs a 4th dup vert for QUADS mode.
            Vertex v1 = vertices.get(i);
            Vertex v2 = vertices.get(i + 1);
            pushVertex(buffer, anchor, poseStack, r, g, b, a);
            pushVertex(buffer, v1, poseStack, r, g, b, a);
            pushVertex(buffer, v2, poseStack, r, g, b, a);
            pushVertex(buffer, v2, poseStack, r, g, b, a); // dup to close quad
        }
    }

    /**
     * Render this face via Minecraft's {@link MultiBufferSource} pipeline.
     * Use this when rendering entities, block entities, or items where a
     * {@link RenderType} and packed light are provided by the game.
     */
    public void renderFace(PoseStack poseStack, RenderType renderType, int packedLight) {
        MultiBufferSource.BufferSource mcBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = mcBufferSource.getBuffer(renderType);
        int count = vertices.size();

        if (count == 4) {
            renderQuad(poseStack, consumer, packedLight);
        } else if (count == 3) {
            renderTriangle(poseStack, consumer, packedLight);
        } else if (count > 4) {
            renderNgon(poseStack, consumer, packedLight);
        } else {
            Common.LOGGER.warn("Skipping face with invalid vertex count: {}", count);
        }
    }

    private void renderTriangle(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        for (Vertex v : vertices) addVertex(buffer, v, poseStack, packedLight);
        addVertex(buffer, vertices.get(0), poseStack, packedLight);
    }

    private void renderQuad(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        for (Vertex v : vertices) addVertex(buffer, v, poseStack, packedLight);
    }

    private void renderNgon(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        Vertex anchor = vertices.getFirst();
        for (int i = 1; i < vertices.size() - 1; i++) {
            Vertex v1 = vertices.get(i);
            Vertex v2 = vertices.get(i + 1);
            addVertex(buffer, anchor, poseStack, packedLight);
            addVertex(buffer, v1, poseStack, packedLight);
            addVertex(buffer, v2, poseStack, packedLight);
        }
    }

    public Vector3f getCentroid() {
        Vector3f centroid = new Vector3f();
        for (Vertex vertex : vertices) centroid.add(vertex.position());
        centroid.mul(1f / vertices.size());
        return centroid;
    }
}