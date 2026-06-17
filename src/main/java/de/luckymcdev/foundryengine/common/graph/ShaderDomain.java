package de.luckymcdev.foundryengine.common.graph;

import de.luckymcdev.foundryengine.common.graph.domain.GraphDomain;
import de.luckymcdev.foundryengine.common.graph.domain.ShaderCodegenContext;
import de.luckymcdev.foundryengine.common.graph.domain.ShaderNodeHandler;
import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.type.PinType;
import net.minecraft.resources.Identifier;

import java.util.*;

public class ShaderDomain implements GraphDomain {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("foundryengine", "shader");

    private final Map<Identifier, ShaderNodeHandler> handlers = new LinkedHashMap<>();

    public void registerHandler(Identifier nodeId, ShaderNodeHandler handler) {
        handlers.put(nodeId, handler);
    }

    public ShaderNodeHandler getHandler(Identifier nodeId) {
        return handlers.get(nodeId);
    }

    public Collection<Identifier> supportedNodes() {
        return Collections.unmodifiableSet(handlers.keySet());
    }

    @Override
    public Identifier id() {
        return ID;
    }

    @Override
    public String displayName() {
        return "Shader Graph";
    }

    @Override
    public PinType flowType() {
        return null;
    }

    @Override
    public List<String> validate(GraphModel graph) {
        var errors = new ArrayList<String>();
        for (var node : graph.nodes().values()) {
            if (!handlers.containsKey(node.typeRef())) {
                errors.add("Node %s (%s) has no handler in shader domain".formatted(node.id(), node.typeRef()));
            }
        }
        return errors;
    }

    @Override
    public String generate(GraphModel graph) {
        return "// Shader generation not yet implemented\n";
    }
}
