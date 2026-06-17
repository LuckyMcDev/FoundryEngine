package de.luckymcdev.foundryengine.common.graph.domain;

import de.luckymcdev.foundryengine.common.graph.ScriptDomain;
import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.model.NodeModel;

import de.luckymcdev.foundryengine.common.graph.model.NodeModel;
import de.luckymcdev.foundryengine.common.graph.type.PinType;

import java.util.*;

public class ScriptRuntimeContext {

    private final GraphModel graph;
    private final ScriptDomain domain;
    private final Object event;
    private final Map<UUID, Object> evaluationCache = new HashMap<>();
    private final Map<String, Object> variables = new HashMap<>();
    private final Set<UUID> visited = new HashSet<>();

    public ScriptRuntimeContext(GraphModel graph, ScriptDomain domain, Object event) {
        this.graph = graph;
        this.domain = domain;
        this.event = event;
    }

    public GraphModel graph() { return graph; }
    public ScriptDomain domain() { return domain; }

    public <T> T getEvent() {
        return (T) event;
    }

    public void setVariable(String name, Object value) {
        variables.put(name, value);
    }

    public Object getVariable(String name) {
        return variables.get(name);
    }

    public Object resolve(NodeModel node) {
        return evaluationCache.computeIfAbsent(node.id(),
                id -> domain.getDataHandler(node.typeRef()).evaluate(node, this));
    }

    public Object resolve(UUID pinId) {
        var link = graph.linkTo(pinId);
        if (link == null) return null;
        var fromPin = graph.pin(link.fromPin());
        if (fromPin == null) return null;
        var fromNode = graph.nodeForPin(fromPin.id());
        if (fromNode == null) return null;
        var handler = domain.getDataHandler(fromNode.typeRef());
        if (handler == null) return null;
        return resolve(fromNode);
    }

    public void walkAllExecOutputs(NodeModel node) {
        if (!visited.add(node.id())) return;
        for (var pin : node.outputPins()) {
            if (pin.type() != PinType.EXEC) continue;
            walkExecFrom(pin.id());
        }
    }

    public void walkExecFrom(UUID outputPinId) {
        var links = graph.linksFrom(outputPinId);
        for (var link : links) {
            var toPin = graph.pin(link.toPin());
            if (toPin == null) continue;
            var nextNode = graph.nodeForPin(toPin.id());
            if (nextNode == null) continue;
            var nextHandler = domain.getExecHandler(nextNode.typeRef());
            if (nextHandler == null) continue;
            nextHandler.execute(nextNode, this);
            walkAllExecOutputs(nextNode);
        }
    }
}
