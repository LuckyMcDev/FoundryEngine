package de.luckymcdev.foundryengine.common.blueprint.nodes;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintContext;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePin;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes.EXEC;

public abstract class BuiltinNode {
    public final String identifier;
    public final String name;
    public final String category;
    public final List<NodePin> declaredInputs;
    public final List<NodePin> declaredOutputs;
    public final Map<String, Object> pinDefaults;

    public BuiltinNode(String name, String category) {
        this(name, name, category);
    }

    public BuiltinNode(String identifier, String name, String category) {
        this.identifier = identifier;
        this.name = name;
        this.category = category;
        this.declaredInputs = new ArrayList<>();
        this.declaredOutputs = new ArrayList<>();
        this.pinDefaults = new HashMap<>();
        initPins();
    }

    protected BuiltinNode(String identifier, String name, String category, boolean skipInit) {
        this.identifier = identifier;
        this.name = name;
        this.category = category;
        this.declaredInputs = new ArrayList<>();
        this.declaredOutputs = new ArrayList<>();
        this.pinDefaults = new HashMap<>();
        if (!skipInit) initPins();
    }

    protected void initPins() {
    }

    protected final ExecInputHandle execInput(String label) {
        declaredInputs.add(EXEC.required(label));
        return new ExecInputHandle(label);
    }

    protected final ExecOutputHandle execOutput(String label) {
        declaredOutputs.add(EXEC.output(label));
        return new ExecOutputHandle(label);
    }

    protected final <T> InputHandle<T> input(NodePinType<T> type, String label) {
        declaredInputs.add(type.required(label));
        return new InputHandle<>(label, type);
    }

    protected final <T> InputHandle<T> input(NodePinType<T> type, String label, T defaultValue) {
        declaredInputs.add(type.required(label));
        pinDefaults.put(label, defaultValue);
        return new InputHandle<>(label, type);
    }

    protected final <T> OutputHandle<T> output(NodePinType<T> type, String label) {
        declaredOutputs.add(type.output(label));
        return new OutputHandle<>(label, type);
    }

    public BlueprintNode createNode() {
        List<NodePin> allPins = new ArrayList<>(declaredInputs.size() + declaredOutputs.size());
        allPins.addAll(declaredInputs);
        allPins.addAll(declaredOutputs);
        BlueprintNode node = new BlueprintNode(name, category, allPins);
        node.identifier = identifier;
        for (var entry : pinDefaults.entrySet()) {
            var pin = node.inputPin(entry.getKey());
            if (pin != null) pin.defaultValue = entry.getValue();
        }
        return node;
    }

    public abstract void execute(BlueprintNode node, BlueprintEngine engine, BlueprintGraph graph, BlueprintContext ctx);
}
