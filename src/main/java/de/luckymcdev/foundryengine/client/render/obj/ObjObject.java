package de.luckymcdev.foundryengine.client.render.obj;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.client.Client;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ObjObject {
    private final String name;
    private final List<Face> faces;
    private Vector3f position;
    private Vector3f rotation;
    private Vector3f scale;

    public ObjObject(String name) {
        this.name = name;
        this.faces = new ArrayList<>();
        this.position = new Vector3f(0, 0, 0);
        this.rotation = new Vector3f(0, 0, 0);
        this.scale = new Vector3f(1, 1, 1);
    }

    public void addFace(Face face) {
        faces.add(face);
    }

    public void render(PoseStack poseStack, RenderType renderType, int packedLight) {
        poseStack.pushPose();
        applyTransformToPoseStack(poseStack);
        faces.forEach(face -> face.renderFace(poseStack, renderType, packedLight));
        poseStack.popPose();
    }

    public void render(RenderPipeline pipeline, Matrix4f viewMatrix,
                       float r, float g, float b, float a) {
        Matrix4f mvp = new Matrix4f(viewMatrix).mul(buildModelMatrix());

        Client.getMeshRenderer().draw(pipeline, mvp, buffer -> {
            PoseStack poseStack = new PoseStack();
            for (Face face : faces) {
                face.buildVertices(buffer, poseStack, r, g, b, a);
            }
        });
    }

    public void render(RenderPipeline pipeline, Matrix4f viewMatrix) {
        render(pipeline, viewMatrix, 1f, 1f, 1f, 1f);
    }

    public Vector3f getCentroid() {
        if (faces.isEmpty()) return new Vector3f(0, 0, 0);
        Vector3f centroid = new Vector3f();
        for (Face face : faces) centroid.add(face.getCentroid());
        centroid.mul(1f / faces.size());
        return centroid;
    }

    /**
     * Builds a local model matrix from this object's position, rotation, and scale.
     */
    private Matrix4f buildModelMatrix() {
        return new Matrix4f()
                .translate(position)
                .rotate(new Quaternionf().rotateXYZ(rotation.x, rotation.y, rotation.z))
                .scale(scale);
    }

    /**
     * Applies this object's transform to a PoseStack (used by the RenderType path).
     */
    private void applyTransformToPoseStack(PoseStack poseStack) {
        poseStack.translate(position.x, position.y, position.z);
        poseStack.mulPose(new Quaternionf().rotateXYZ(rotation.x, rotation.y, rotation.z));
        poseStack.scale(scale.x, scale.y, scale.z);
    }

    // Getters / setters
    public String getName() {
        return name;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f p) {
        this.position = p;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f r) {
        this.rotation = r;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.set(x, y, z);
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f s) {
        this.scale = s;
    }

    public void setScale(float u) {
        this.scale.set(u, u, u);
    }

    public void setScale(float x, float y, float z) {
        this.scale.set(x, y, z);
    }
}