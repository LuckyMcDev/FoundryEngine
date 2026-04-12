package de.luckymcdev.foundryengine.client.imgui.imnodes;


import de.luckymcdev.foundryengine.client.imgui.imnodes.blueprint.BlueprintEngine;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePin;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeBuilder {
    private final BlueprintEngine engine;
    private final String category;
    private final String name;
    private final List<NodePin> pins = new ArrayList<>();
    private final Map<String, Object> defaults = new HashMap<>();
    private @Nullable BlueprintEngine.NodeBehavior behavior;

    public NodeBuilder(BlueprintEngine engine, String category, String name) {
        this.engine = engine;
        this.category = category;
        this.name = name;
    }

    public NodeBuilder in(NodePinType<?> type, String label) {
        pins.add(type.required(label));
        return this;
    }

    public NodeBuilder out(NodePinType<?> type, String label) {
        pins.add(type.output(label));
        return this;
    }

    public NodeBuilder defaultValue(String pinLabel, Object value) {
        defaults.put(pinLabel, value);
        return this;
    }

    public NodeBuilder behavior(BlueprintEngine.NodeBehavior behavior) {
        this.behavior = behavior;
        return this;
    }

    public BlueprintEngine register() {
        List<NodePin> capturedPins = List.copyOf(pins);
        Map<String, Object> capturedDefaults = Map.copyOf(defaults);
        engine.registerNode(category, name, () -> new ArrayList<>(capturedPins), capturedDefaults, behavior);
        return engine;
    }
}