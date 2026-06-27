package de.luckymcdev.foundryengine.client.render.obj;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.render.MeshRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;

public record Face(List<Vertex> vertices, Material material) {
    public Face(List<Vertex> vertices, @Nullable Material material) {
        this.vertices = vertices;
        this.material = material == null ? Material.MISSING : material;
    }

    /**
     * Convenience constructor for callers not using materials.
     */
    public Face(List<Vertex> vertices) {
        this(vertices, Material.MISSING);
    }

    private static void pushVertex(BufferBuilder buffer, Vertex vertex, PoseStack poseStack,
                                   float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();
        Vector3f pos = vertex.position();
        Vector3f normal = vertex.normal();

        buffer.addVertex(pose, pos.x(), pos.y(), pos.z())
                .setColor(r, g, b, a)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    private static void addVertex(VertexConsumer buffer, Vertex vertex, PoseStack poseStack, int packedLight,
                                  float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();
        Vector3f position = vertex.position();
        Vector3f normal = vertex.normal();
        Vector2f uv = vertex.uv();

        buffer.addVertex(pose, position.x(), position.y(), position.z());
        buffer.setColor(r, g, b, a);
        buffer.setUv(uv.x, -uv.y);
        buffer.setOverlay(OverlayTexture.NO_OVERLAY);
        buffer.setLight(packedLight);
        buffer.setNormal(pose, normal.x(), normal.y(), normal.z());
    }

    private static void pushVertexTextured(BufferBuilder buffer, Vertex vertex, PoseStack poseStack,
                                           float r, float g, float b, float a, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Vector3f pos = vertex.position();
        Vector3f normal = vertex.normal();
        Vector2f uv = vertex.uv();

        buffer.addVertex(pose, pos.x(), pos.y(), pos.z())
                .setUv(uv.x, -uv.y)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setColor(r, g, b, a)
                .setNormal(pose, normal.x(), normal.y(), normal.z());
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
     * Emit this face's geometry directly into {@code buffer}.
     * Use this with {@link MeshRenderer} — the caller owns pipeline selection.
     * Note: this path does not currently sample {@link #material}'s texture;
     * it only writes position/normal/color. Use {@link #renderFace(PoseStack, int)}
     * for textured rendering via the vanilla MultiBufferSource path.
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
            Client.LOGGER.warn("Skipping face with invalid vertex count: {}", count);
        }
    }

    /**
     * Render this face via Minecraft's {@link MultiBufferSource} pipeline, using an
     * explicitly supplied {@link RenderType}. Use this when the caller wants to force
     * a specific render type (e.g. a depth-only or outline pass) regardless of material.
     */
    public void renderFace(PoseStack poseStack, RenderType renderType, int packedLight) {
        Vector3f diffuse = material.getDiffuseColor();
        renderFace(poseStack, renderType, packedLight, diffuse.x(), diffuse.y(), diffuse.z(), material.getOpacity());
    }

    /**
     * Render this face via Minecraft's {@link MultiBufferSource} pipeline, resolving
     * the {@link RenderType} from this face's {@link #material} (see {@link ObjRenderTypes}).
     * This is the normal entry point for textured OBJ/MTL rendering.
     */
    public void renderFace(PoseStack poseStack, int packedLight) {
        RenderType renderType = ObjRenderTypes.forMaterial(material);
        renderFace(poseStack, renderType, packedLight);
    }

    private void renderFace(PoseStack poseStack, RenderType renderType, int packedLight,
                            float r, float g, float b, float a) {
        MultiBufferSource.BufferSource mcBufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = mcBufferSource.getBuffer(renderType);
        int count = vertices.size();

        if (count == 4) {
            renderQuad(poseStack, consumer, packedLight, r, g, b, a);
        } else if (count == 3) {
            renderTriangle(poseStack, consumer, packedLight, r, g, b, a);
        } else if (count > 4) {
            renderNgon(poseStack, consumer, packedLight, r, g, b, a);
        } else {
            Client.LOGGER.warn("Skipping face with invalid vertex count: {}", count);
        }
    }

    private void renderTriangle(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                                float r, float g, float b, float a) {
        for (Vertex v : vertices) addVertex(buffer, v, poseStack, packedLight, r, g, b, a);
        addVertex(buffer, vertices.get(0), poseStack, packedLight, r, g, b, a);
    }

    private void renderQuad(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                            float r, float g, float b, float a) {
        for (Vertex v : vertices) addVertex(buffer, v, poseStack, packedLight, r, g, b, a);
    }

    private void renderNgon(PoseStack poseStack, VertexConsumer buffer, int packedLight,
                            float r, float g, float b, float a) {
        Vertex anchor = vertices.getFirst();
        for (int i = 1; i < vertices.size() - 1; i++) {
            Vertex v1 = vertices.get(i);
            Vertex v2 = vertices.get(i + 1);
            addVertex(buffer, anchor, poseStack, packedLight, r, g, b, a);
            addVertex(buffer, v1, poseStack, packedLight, r, g, b, a);
            addVertex(buffer, v2, poseStack, packedLight, r, g, b, a);
        }
    }

    public void buildVerticesTextured(BufferBuilder buffer, PoseStack poseStack, float r, float g, float b, float a, int packedLight) {
        int count = vertices.size();
        if (count == 3) {
            buildTriangleTextured(buffer, poseStack, r, g, b, a, packedLight);
        } else if (count == 4) {
            buildQuadTextured(buffer, poseStack, r, g, b, a, packedLight);
        } else if (count > 4) {
            buildNgonTextured(buffer, poseStack, r, g, b, a, packedLight);
        } else {
            Client.LOGGER.warn("Skipping face with invalid vertex count: {}", count);
        }
    }

    public void buildVerticesTextured(BufferBuilder buffer, PoseStack poseStack, int packedLight) {
        Vector3f d = material.getDiffuseColor();
        buildVerticesTextured(buffer, poseStack, d.x(), d.y(), d.z(), material.getOpacity(), packedLight);
    }

    private void buildTriangleTextured(BufferBuilder buffer, PoseStack poseStack, float r, float g, float b, float a, int packedLight) {
        for (Vertex v : vertices) pushVertexTextured(buffer, v, poseStack, r, g, b, a, packedLight);
        pushVertexTextured(buffer, vertices.get(0), poseStack, r, g, b, a, packedLight);
    }

    private void buildQuadTextured(BufferBuilder buffer, PoseStack poseStack, float r, float g, float b, float a, int packedLight) {
        for (Vertex v : vertices) pushVertexTextured(buffer, v, poseStack, r, g, b, a, packedLight);
    }

    private void buildNgonTextured(BufferBuilder buffer, PoseStack poseStack, float r, float g, float b, float a, int packedLight) {
        Vertex anchor = vertices.getFirst();
        for (int i = 1; i < vertices.size() - 1; i++) {
            Vertex v1 = vertices.get(i);
            Vertex v2 = vertices.get(i + 1);
            pushVertexTextured(buffer, anchor, poseStack, r, g, b, a, packedLight);
            pushVertexTextured(buffer, v1, poseStack, r, g, b, a, packedLight);
            pushVertexTextured(buffer, v2, poseStack, r, g, b, a, packedLight);
            pushVertexTextured(buffer, v2, poseStack, r, g, b, a, packedLight);
        }
    }

    public Vector3f getCentroid() {
        Vector3f centroid = new Vector3f();
        for (Vertex vertex : vertices) centroid.add(vertex.position());
        centroid.mul(1f / vertices.size());
        return centroid;
    }
}