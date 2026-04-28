package de.luckymcdev.foundryengine.common.blueprint.serial.model;

import org.jetbrains.annotations.Nullable;

public class SerializedPin {
    public int id;
    public int nodeId;
    public String label;
    public String typeName;
    public String connectionType;
    public @Nullable Object defaultValue;
    public boolean isConnected;
}