package de.luckymcdev.foundryengine.common.scene;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractSceneNode implements EngineSceneNode {
    protected final String uuid;
    protected final List<EngineSceneNode> children = new ArrayList<>();
    protected final Map<String, Object> properties = new HashMap<>();
    protected String displayName;
    @Nullable
    protected EngineSceneNode parent;

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

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    @Nullable
    public EngineSceneNode getParent() {
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