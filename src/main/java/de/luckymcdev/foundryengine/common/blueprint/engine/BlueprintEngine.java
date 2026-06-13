package de.luckymcdev.foundryengine.common.blueprint.engine;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintNode;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinInfo;
import de.luckymcdev.foundryengine.common.blueprint.nodes.BuiltinNode;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.common.registry.GenericRegistryList;
import de.luckymcdev.foundryengine.common.util.color.Color;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Collections;
import java.util.Map;

public class BlueprintEngine {
    public static final String CTX_REGISTRY_EVENT = "_registry_event";
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GenericRegistryList<BuiltinNode> builtinNodes = new GenericRegistryList<>();
    private final GenericRegistry<String, BuiltinNode> builtinById = new GenericRegistry<>();

    private static boolean hasExecInput(BlueprintNode node) {
        for (var p : node.inputPins) {
            if (p.pin.type() == BlueprintTypes.EXEC) return true;
        }
        return false;
    }

    public void register(BuiltinNode node) {
        builtinNodes.add(node);
        builtinById.register(node.identifier, node);
    }

    public Color getCategoryColor(@Nullable String category) {
        return BlueprintCategories.color(category);
    }

    public GenericRegistryList<BuiltinNode> getBuiltinNodes() {
        return builtinNodes;
    }

    public @Nullable BuiltinNode getById(String identifier) {
        return builtinById.get(identifier);
    }

    public BlueprintNode createNode(BuiltinNode builtin) {
        return builtin.createNode();
    }

    public boolean canConnect(NodePinInfo src, NodePinInfo dst) {
        return src.pin.type().isCompatibleWith(dst.pin.type());
    }

    public void executeGraph(BlueprintGraph graph) {
        executeEvent("event.begin_play", graph);
    }

    public void executeEvent(String eventName, BlueprintGraph graph) {
        executeEvent(eventName, graph, Collections.emptyMap());
    }

    public void executeEvent(String eventName, BlueprintGraph graph, Map<String, Object> payload) {
        for (BlueprintNode node : graph.nodes.values()) {
            if (node.identifier.equals(eventName)) {
                BlueprintContext ctx = new BlueprintContext(graph, this);
                payload.forEach(ctx::setVar);
                if (!payload.isEmpty()) {
                    for (var out : node.outputPins) {
                        if (out.pin.type() == BlueprintTypes.EXEC) continue;
                        Object v = payload.get(out.pin.label());
                        if (v != null) node.setOutput(out.pin.label(), v);
                    }
                }
                executeNext(node, graph, ctx);
            }
        }
    }

    public void executeNext(BlueprintNode node, BlueprintGraph graph, BlueprintContext ctx) {
        BuiltinNode builtin = builtinById.get(node.identifier);
        if (builtin != null) {
            builtin.execute(node, this, graph, ctx);
            if (hasExecInput(node)) return;
        }
        for (var pin : node.outputPins) {
            if (pin.pin.type() == BlueprintTypes.EXEC) {
                executePin(node, pin.pin.label(), graph, ctx);
                return;
            }
        }
    }

    public void executePin(BlueprintNode node, String pinLabel, BlueprintGraph graph, BlueprintContext ctx) {
        var pin = node.outputPin(pinLabel);
        if (pin != null) {
            var connectedInput = graph.getConnectedInputPin(pin);
            if (connectedInput != null) {
                executeNext(connectedInput.node, graph, ctx);
            }
        }
    }

    public void registerBuiltins() {
    }
}
