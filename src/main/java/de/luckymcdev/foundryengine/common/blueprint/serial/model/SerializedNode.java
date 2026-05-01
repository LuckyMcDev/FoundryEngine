package de.luckymcdev.foundryengine.common.blueprint.serial.model;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SerializedNode {
    public int id;
    /**
     * Stable runtime identifier (behavior lookup, event dispatch).
     * Older files may omit this and rely on {@link #name}.
     */
    public @Nullable String identifier;
    public String name;
    public @Nullable String category;
    public float posX;
    public float posY;
    public Map<String, Object> outputValues;
    /**
     * Optional arbitrary metadata for the node (editor state).
     * Older blueprint files may omit this field.
     */
    public @Nullable Map<String, Object> data;
}
