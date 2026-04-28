package de.luckymcdev.foundryengine.common.blueprint.serial.model;

import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class SerializedNode {
    public int id;
    public String name;
    public @Nullable String category;
    public float posX;
    public float posY;
    public Map<String, Object> outputValues;
}