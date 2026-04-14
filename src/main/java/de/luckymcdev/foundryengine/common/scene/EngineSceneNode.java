package de.luckymcdev.foundryengine.common.scene;

import net.minecraft.world.entity.Entity;
import org.joml.Vector2f;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public interface EngineSceneNode {
    default Entity asEntity() {
        return null;
    }

    boolean editable();

    String getUUID();

    String getTypeName();

    String getDisplayName();

    Vector3f getPosition();

    Vector2f getRotation();

    void remove();

    @Nullable
    EngineSceneNode getParent();

    void setParent(@Nullable EngineSceneNode parent);

    List<EngineSceneNode> getChildren();

    void addChild(EngineSceneNode child);

    void removeChild(EngineSceneNode child);

    default Map<String, Object> getProperties() {
        return Map.of();
    }

    default void setProperty(String key, Object value) {
    }

    default void drawGizmos() {
    }
}