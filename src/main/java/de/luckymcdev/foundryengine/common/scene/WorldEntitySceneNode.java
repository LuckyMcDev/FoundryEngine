package de.luckymcdev.foundryengine.common.scene;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Set;

/**
 * A live node backed by a Minecraft entity.
 *
 * <p>Not persisted as part of the engine scene graph; it is just exposed to the editor.</p>
 */
public final class WorldEntitySceneNode extends AbstractSceneNode {
    private final Entity entity;

    public WorldEntitySceneNode(Entity entity) {
        super(entity.getStringUUID(), entity.getName().getString());
        this.entity = entity;
    }

    @Override
    public Entity asEntity() {
        return entity;
    }

    @Override
    public boolean editable() {
        // Read-only for now: we don't have a server-authoritative edit packet yet.
        return false;
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
    public Vector3f getLocalPosition() {
        return entity.position().toVector3f();
    }

    @Override
    public void setLocalPosition(Vector3f pos) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            Vector2f rot = getLocalRotation();
            entity.teleportTo(serverLevel, pos.x, pos.y, pos.z, Set.of(), rot.x, rot.y, false);
        } else {
            entity.setPos(pos.x, pos.y, pos.z);
        }
    }

    @Override
    public Vector2f getLocalRotation() {
        var rot = entity.getRotationVector();
        return new Vector2f(rot.x, rot.y);
    }

    @Override
    public void setLocalRotation(Vector2f rot) {
        Vector3f pos = getLocalPosition();
        if (entity.level() instanceof ServerLevel serverLevel) {
            entity.teleportTo(serverLevel, pos.x, pos.y, pos.z, Set.of(), rot.y, rot.x, false);
        }
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
