package de.luckymcdev.foundryengine.common.scene;

import net.minecraft.world.entity.Entity;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * A node in the editor-visible scene tree.
 *
 * <p>This is intentionally editor-oriented: it is used for selection, gizmos and inspection.
 * Persisted scene graph nodes and live world-backed nodes (entities, etc.) can both implement this.</p>
 */
public interface EngineSceneNode {

    /**
     * If this node is backed by a world entity, returns it. Otherwise null.
     */
    default @Nullable Entity asEntity() {
        return null;
    }

    /**
     * Whether the editor may modify this node (transform, name, properties, removal).
     */
    boolean editable();

    String getUUID();

    String getTypeName();

    String getDisplayName();

    default void setDisplayName(String displayName) {
    }

    /**
     * Local-space transform (relative to parent). For world-backed nodes, this is the world transform.
     */
    Vector3f getLocalPosition();

    default void setLocalPosition(Vector3f pos) {
    }

    Vector2f getLocalRotation();

    default void setLocalRotation(Vector2f rot) {
    }

    /**
     * World-space transform (after parenting is applied). Defaults to local transform.
     */
    default Vector3f getPosition() {
        return getLocalPosition();
    }

    default Vector2f getRotation() {
        return getLocalRotation();
    }

    void remove();

    @Nullable EngineSceneNode getParent();

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

