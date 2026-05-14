package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.graph.NodePin;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.EXEC;

public final class NodeBuilder {
    private final String identifier;
    private final String name;
    private final String category;
    private final List<NodePin> declaredInputs = new ArrayList<>();
    private final List<NodePin> declaredOutputs = new ArrayList<>();
    private final Map<String, Object> pinDefaults = new HashMap<>();

    private NodeBuilder(String identifier, String name, String category) {
        this.identifier = identifier;
        this.name = name;
        this.category = category;
    }

    public static NodeBuilder create(String identifier, String name, String category) {
        return new NodeBuilder(identifier, name, category);
    }

    public NodeBuilder execInput(String label) {
        declaredInputs.add(EXEC.required(label));
        return this;
    }

    public NodeBuilder execOutput(String label) {
        declaredOutputs.add(EXEC.output(label));
        return this;
    }

    public <T> NodeBuilder input(NodePinType<T> type, String label) {
        declaredInputs.add(type.required(label));
        return this;
    }

    public <T> NodeBuilder input(NodePinType<T> type, String label, T defaultValue) {
        declaredInputs.add(type.required(label));
        pinDefaults.put(label, defaultValue);
        return this;
    }

    public <T> NodeBuilder output(NodePinType<T> type, String label) {
        declaredOutputs.add(type.output(label));
        return this;
    }

    public SimpleNode build(NodeExecutor executor) {
        return new SimpleNode(identifier, name, category,
                List.copyOf(declaredInputs), List.copyOf(declaredOutputs),
                Map.copyOf(pinDefaults), executor);
    }
}
