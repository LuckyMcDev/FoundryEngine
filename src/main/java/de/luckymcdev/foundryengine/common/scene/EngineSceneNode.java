package de.luckymcdev.foundryengine.common.scene;

import net.minecraft.world.entity.Entity;
import org.joml.Vector2f;
import org.joml.Vector3f;

public interface EngineSceneNode {
    default Entity self() {
        return (Entity) this;
    }

    String getUUID();

    /**
     * Human-readable type name, e.g. "minecraft:zombie"
     */
    String getTypeName();

    /**
     * Short display name shown in the scene tree, e.g. custom name or type fallback
     */
    String getDisplayName();

    /**
     * The Position of the Entity.
     */
    Vector3f getPosition();

    /**
     * Pitch (x) and Yaw (y) in degrees
     */
    Vector2f getRotation();


    void remove();
}