package de.luckymcdev.foundryengine.common.graph;

import de.luckymcdev.foundryengine.common.graph.domain.GraphDomain;
import de.luckymcdev.foundryengine.common.graph.domain.ScriptDataHandler;
import de.luckymcdev.foundryengine.common.graph.domain.ScriptExecHandler;
import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.type.PinType;
import net.minecraft.resources.Identifier;

import java.util.*;

public class ScriptDomain implements GraphDomain {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("foundryengine", "script");

    private final Map<Identifier, ScriptExecHandler> execHandlers = new LinkedHashMap<>();
    private final Map<Identifier, ScriptDataHandler> dataHandlers = new LinkedHashMap<>();

    public void registerExecHandler(Identifier nodeId, ScriptExecHandler handler) {
        execHandlers.put(nodeId, handler);
    }

    public void registerDataHandler(Identifier nodeId, ScriptDataHandler handler) {
        dataHandlers.put(nodeId, handler);
    }

    public ScriptExecHandler getExecHandler(Identifier nodeId) {
        return execHandlers.get(nodeId);
    }

    public ScriptDataHandler getDataHandler(Identifier nodeId) {
        return dataHandlers.get(nodeId);
    }

    public boolean isExecNode(Identifier nodeId) {
        return execHandlers.containsKey(nodeId);
    }

    public boolean isDataNode(Identifier nodeId) {
        return dataHandlers.containsKey(nodeId);
    }

    public Collection<Identifier> supportedExecNodes() {
        return Collections.unmodifiableSet(execHandlers.keySet());
    }

    public Collection<Identifier> supportedDataNodes() {
        return Collections.unmodifiableSet(dataHandlers.keySet());
    }

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Event Script";
    }

    @Override
    public PinType flowType() {
        return PinType.EXEC;
    }

    @Override
    public List<String> validate(GraphModel graph) {
        var errors = new ArrayList<String>();
        for (var node : graph.nodes().values()) {
            boolean hasExec = execHandlers.containsKey(node.typeRef());
            boolean hasData = dataHandlers.containsKey(node.typeRef());
            if (!hasExec && !hasData) {
                errors.add("Node %s (%s) has no handler in script domain".formatted(node.id(), node.typeRef()));
            }
        }
        return errors;
    }

    @Override
    public String generate(GraphModel graph) {
        return "";
    }
}
