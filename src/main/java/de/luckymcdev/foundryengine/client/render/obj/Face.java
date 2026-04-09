package de.luckymcdev.foundryengine.client.render.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.List;

public record Face(List<Vertex> vertices) {

    public void renderFace(PoseStack poseStack, RenderType renderType, int packedLight) {
        MultiBufferSource.BufferSource mcBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer buffer = mcBufferSource.getBuffer(renderType);
        int vertexCount = vertices.size();

        if (vertexCount == 4) {
            renderQuad(poseStack, buffer, packedLight);
        } else if (vertexCount == 3) {
            renderTriangle(poseStack, buffer, packedLight);
        } else if (vertexCount > 4) {
            renderNgon(poseStack, buffer, packedLight);
        } else {
            Common.LOGGER.warn("Skipping face with invalid vertex count: {}", vertexCount);
        }
    }

    public void renderTriangle(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        this.vertices.forEach(vertex -> addVertex(buffer, vertex, poseStack, packedLight));
        addVertex(buffer, this.vertices.get(0), poseStack, packedLight);
    }

    public void renderQuad(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        this.vertices.forEach(vertex -> addVertex(buffer, vertex, poseStack, packedLight));
    }

    public void renderNgon(PoseStack poseStack, VertexConsumer buffer, int packedLight) {
        Vertex anchor = vertices.getFirst();

        for (int i = 1; i < vertices.size() - 1; i++) {
            Vertex v1 = vertices.get(i);
            Vertex v2 = vertices.get(i + 1);

            addVertex(buffer, anchor, poseStack, packedLight);
            addVertex(buffer, v1, poseStack, packedLight);
            addVertex(buffer, v2, poseStack, packedLight);
        }
    }

    private void addVertex(VertexConsumer buffer, Vertex vertex, PoseStack poseStack, int packedLight) {
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
     * Returns the centroid of all vertices in this face.
     * <p>
     * This method will calculate the centroid by summing all vertex positions and dividing by the number of vertices.
     *
     * @return The centroid of all vertices in this face
     */
    public Vector3f getCentroid() {
        Vector3f centroid = new Vector3f();
        for (Vertex vertex : vertices) centroid.add(vertex.position());
        centroid.mul(1f / vertices.size());
        return centroid;
    }
}