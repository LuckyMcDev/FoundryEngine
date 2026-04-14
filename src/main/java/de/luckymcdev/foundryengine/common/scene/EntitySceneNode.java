package de.luckymcdev.foundryengine.common.scene;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class EntitySceneNode extends AbstractSceneNode {
    private final Entity entity;

    public EntitySceneNode(Entity entity) {
        super(entity.getStringUUID(), entity.getName().getString());
        this.entity = entity;
    }

    @Override
    public Entity asEntity() {
        return entity;
    }

    @Override
    public boolean editable() {
        return true;
    }

    @Override
    public String getTypeName() {
        return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
    }

    @Override
    public String getDisplayName() {
        if (entity.hasCustomName() && entity.getCustomName() != null) {
            return entity.getCustomName().getString();
        }
        return entity.getType().getDescription().getString();
    }

    @Override
    public Vector3f getPosition() {
        return entity.position().toVector3f();
    }

    @Override
    public Vector2f getRotation() {
        var rot = entity.getRotationVector();
        return new Vector2f(rot.x, rot.y);
    }

    @Override
    public void remove() {
        entity.remove(Entity.RemovalReason.KILLED);
        super.remove();
    }

    @Override
    public void drawGizmos() {
        AABB bb = entity.getBoundingBox();
        Gizmos.cuboid(bb, GizmoStyle.stroke(0xFFFFFF00));
    }
}