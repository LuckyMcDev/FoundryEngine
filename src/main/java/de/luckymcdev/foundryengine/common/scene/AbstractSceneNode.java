package de.luckymcdev.foundryengine.common.scene;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation for editor-visible nodes.
 */
public abstract class AbstractSceneNode implements EngineSceneNode {
    protected final String uuid;
    protected final List<EngineSceneNode> children = new ArrayList<>();
    protected final Map<String, Object> properties = new LinkedHashMap<>();

    protected String displayName;
    protected final Vector3f localPosition = new Vector3f();
    protected final Vector2f localRotation = new Vector2f();
    protected @Nullable EngineSceneNode parent;

    protected AbstractSceneNode(String uuid, String displayName) {
        this.uuid = uuid;
        this.displayName = displayName;
    }

    @Override
    public String getUUID() {
        return uuid;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public Vector3f getLocalPosition() {
        return localPosition;
    }

    @Override
    public void setLocalPosition(Vector3f pos) {
        this.localPosition.set(pos);
    }

    @Override
    public Vector2f getLocalRotation() {
        return localRotation;
    }

    @Override
    public void setLocalRotation(Vector2f rot) {
        this.localRotation.set(rot);
    }

    @Override
    public Vector3f getPosition() {
        // A pragmatic hierarchical transform (good enough for editor parenting and grouping):
        // - Rotations are composed by simple addition.
        // - Child positions are not rotated by parent rotation (keeps behavior predictable in Minecraft coords).
        Vector3f world = new Vector3f(getLocalPosition());
        EngineSceneNode p = this.parent;
        while (p != null) {
            world.add(p.getLocalPosition());
            p = p.getParent();
        }
        return world;
    }

    @Override
    public Vector2f getRotation() {
        Vector2f world = new Vector2f(getLocalRotation());
        EngineSceneNode p = this.parent;
        while (p != null) {
            world.add(p.getLocalRotation());
            p = p.getParent();
        }
        return world;
    }

    @Override
    public @Nullable EngineSceneNode getParent() {
        return parent;
    }

    @Override
    public void setParent(@Nullable EngineSceneNode parent) {
        this.parent = parent;
    }

    @Override
    public List<EngineSceneNode> getChildren() {
        return children;
    }

    @Override
    public void addChild(EngineSceneNode child) {
        children.add(child);
        child.setParent(this);
    }

    @Override
    public void removeChild(EngineSceneNode child) {
        children.remove(child);
        child.setParent(null);
    }

    @Override
    public void remove() {
        if (parent != null) {
            parent.removeChild(this);
        }
        for (EngineSceneNode child : new ArrayList<>(children)) {
            child.remove();
        }
        children.clear();
    }

    @Override
    public Map<String, Object> getProperties() {
        return properties;
    }

    @Override
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }
}