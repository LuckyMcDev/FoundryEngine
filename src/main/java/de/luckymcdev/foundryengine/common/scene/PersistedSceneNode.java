package de.luckymcdev.foundryengine.common.scene;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;
import org.joml.Vector3f;

/**
 * A node that is part of the persisted scene graph (stored in {@code SavedData}).
 */
public final class PersistedSceneNode extends AbstractSceneNode {
    private final SceneGraph graph;
    private final String typeName;

    PersistedSceneNode(SceneGraph graph, String uuid, String displayName, String typeName) {
        super(uuid, displayName);
        this.graph = graph;
        this.typeName = typeName;
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public void setDisplayName(String displayName) {
        super.setDisplayName(displayName);
        graph.markDirty();
    }

    @Override
    public void setLocalPosition(Vector3f pos) {
        super.setLocalPosition(pos);
        graph.markDirty();
    }

    @Override
    public void setLocalRotation(Vector2f rot) {
        super.setLocalRotation(rot);
        graph.markDirty();
    }

    @Override
    public void addChild(EngineSceneNode child) {
        super.addChild(child);
        graph.markDirty();
    }

    @Override
    public void removeChild(EngineSceneNode child) {
        super.removeChild(child);
        graph.markDirty();
    }

    @Override
    public void setProperty(String key, Object value) {
        super.setProperty(key, value);
        graph.markDirty();
    }

    @Override
    public void remove() {
        graph.removeSubtree(this.uuid);
    }

    @Override
    public void drawGizmos() {
        Vector3f p = getPosition();
        Vec3 pos = new Vec3(p.x, p.y, p.z);
        Gizmos.point(pos, Color.CYAN.argb(), 0.35f);
        Gizmos.circle(pos, 0.55f, GizmoStyle.stroke(Color.CYAN.argb()));
    }
}

