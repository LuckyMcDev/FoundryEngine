package de.luckymcdev.foundryengine.client.render.obj;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.rendertype.RenderType;
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

        poseStack.translate(position.x, position.y, position.z);

        Quaternionf quaternion = new Quaternionf().rotateXYZ(rotation.x, rotation.y, rotation.z);
        poseStack.mulPose(quaternion);

        poseStack.scale(scale.x, scale.y, scale.z);

        faces.forEach(face -> face.renderFace(poseStack, renderType, packedLight));

        poseStack.popPose();
    }

    public Vector3f getCentroid() {
        if (faces.isEmpty()) return new Vector3f(0, 0, 0);

        Vector3f centroid = new Vector3f();
        for (Face face : faces) {
            centroid.add(face.getCentroid());
        }
        centroid.mul(1f / faces.size());
        return centroid;
    }

    // Getters and setters for animation
    public String getName() {
        return name;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public Vector3f getRotation() {
        return rotation;
    }

    public void setRotation(Vector3f rotation) {
        this.rotation = rotation;
    }

    public void setRotation(float x, float y, float z) {
        this.rotation.set(x, y, z);
    }

    public Vector3f getScale() {
        return scale;
    }

    public void setScale(Vector3f scale) {
        this.scale = scale;
    }

    public void setScale(float uniform) {
        this.scale.set(uniform, uniform, uniform);
    }

    public void setScale(float x, float y, float z) {
        this.scale.set(x, y, z);
    }
}