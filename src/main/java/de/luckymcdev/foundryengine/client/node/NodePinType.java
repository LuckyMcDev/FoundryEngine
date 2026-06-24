package de.luckymcdev.foundryengine.client.node;

import java.util.List;
import java.util.function.Supplier;

public class NodePinType<T> {
    public final String displayName;
    public final NodePinShape defaultShape;
    public final List<NodePin<T>> singleOutput;
    public final List<NodePin<T>> singleRequiredInput;
    public final List<NodeOption<T>> nodeOptions;
    public final Supplier<NodeBuilder<T>> builderFactory;

    public NodePinType(String displayName, NodePinShape defaultShape,
                       List<NodeOption<T>> nodeOptions,
                       Supplier<NodeBuilder<T>> builderFactory) {
        this.displayName = displayName;
        this.defaultShape = defaultShape;
        this.singleOutput = List.of(output("Out"));
        this.singleRequiredInput = List.of(required("In"));
        this.nodeOptions = nodeOptions;
        this.builderFactory = builderFactory;
    }

    public static <T> NodePinType<T> fromFactories(String displayName, NodePinShape defaultShape,
                                                   List<NamedBuilderFactory<T>> factories,
                                                   Supplier<NodeBuilder<T>> builderFactory) {
        return new NodePinType<>(
                displayName,
                defaultShape,
                factories.stream()
                        .map(f -> new NodeOption<>(f.name, f.factory))
                        .toList(),
                builderFactory
        );
    }

    public NodePin<T> output(String label) {
        return new NodePin<>(this, label, NodePinConnectionType.OUTPUT, defaultShape);
    }

    public NodePin<T> required(String label) {
        return new NodePin<>(this, label, NodePinConnectionType.REQUIRED_INPUT, defaultShape);
    }

    public NodePin<T> optional(String label) {
        return new NodePin<>(this, label, NodePinConnectionType.OPTIONAL_INPUT, defaultShape);
    }

    public record NamedBuilderFactory<T>(String name, Supplier<NodeBuilder<T>> factory) {
    }
}