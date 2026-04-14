package de.luckymcdev.foundryengine.common.scene;

import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PointNode extends AbstractSceneNode {
    private final Vector3f position = new Vector3f();
    private final Vector2f rotation = new Vector2f();

    public PointNode(String uuid, Vector3f position) {
        super(uuid, "Point");
        this.position.set(position);
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public String getTypeName() {
        return "custom:point";
    }

    @Override
    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position.set(position);
    }

    @Override
    public Vector2f getRotation() {
        return rotation;
    }

    public void setRotation(Vector2f rotation) {
        this.rotation.set(rotation);
    }

    @Override
    public void drawGizmos() {
        Vec3 pos = new Vec3(position.x, position.y, position.z);
        Gizmos.point(pos, 0xFF00FFFF, 0.3f);
        Gizmos.circle(pos, 0.5f, GizmoStyle.stroke(0xFF00FFFF));
    }
}