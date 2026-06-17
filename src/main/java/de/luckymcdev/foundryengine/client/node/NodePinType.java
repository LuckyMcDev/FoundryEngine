package de.luckymcdev.foundryengine.client.node;

import de.luckymcdev.foundryengine.common.graph.type.NodePinShapeRef;
import de.luckymcdev.foundryengine.common.graph.type.PinType;

import java.util.List;
import java.util.function.Supplier;

/**
 * Convenience wrapper around {@link PinType} for the editor layer.
 * Provides factory methods for creating {@link NodePin}s and
 * {@link NodeOption}s for context menus.
 */
public class NodePinType {
    public final PinType type;
    public final NodePinShape defaultShape;
    public final List<NodePin> singleOutput;
    public final List<NodePin> singleRequiredInput;
    public final List<NodeOption> nodeOptions;
    public final Supplier<NodeBuilder> builderFactory;

    public NodePinType(PinType type, NodePinShape defaultShape,
                       List<NodeOption> nodeOptions,
                       Supplier<NodeBuilder> builderFactory) {
        this.type = type;
        this.defaultShape = defaultShape;
        this.singleOutput = List.of(output("Out"));
        this.singleRequiredInput = List.of(required("In"));
        this.nodeOptions = nodeOptions;
        this.builderFactory = builderFactory;
    }

    public NodePinType(PinType type, List<NodeOption> nodeOptions,
                       Supplier<NodeBuilder> builderFactory) {
        this(type, NodePin.toShape(type.defaultShape()), nodeOptions, builderFactory);
    }

    public NodePin output(String label) {
        return new NodePin(type, label, NodePinConnectionType.OUTPUT, defaultShape);
    }

    public NodePin required(String label) {
        return new NodePin(type, label, NodePinConnectionType.REQUIRED_INPUT, defaultShape);
    }

    public NodePin optional(String label) {
        return new NodePin(type, label, NodePinConnectionType.OPTIONAL_INPUT, defaultShape);
    }
}
