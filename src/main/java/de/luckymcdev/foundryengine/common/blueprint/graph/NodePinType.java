package de.luckymcdev.foundryengine.common.blueprint.graph;

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class NodePinType<T> {
    public final String displayName;
    public final NodePinShape defaultShape;
    /**
     * ARGB packed color, e.g. {@code 0xFF_FFFFFF}. Used by client rendering.
     */
    public final int color;
    public final List<NodePin> singleOutput;
    public final List<NodePin> singleRequiredInput;
    /**
     * Optional client-side context-menu builder. {@code null} on the server/common
     * side; client code may supply a non-null value when constructing instances.
     */
    public final @Nullable Consumer<Consumer<BlueprintNode>> menuBuilder;

    public NodePinType(String displayName, NodePinShape defaultShape, int color,
                       @Nullable Consumer<Consumer<BlueprintNode>> menuBuilder) {
        this.displayName = displayName;
        this.defaultShape = defaultShape;
        this.color = color;
        this.menuBuilder = menuBuilder;
        this.singleOutput = List.of(output("Out"));
        this.singleRequiredInput = List.of(required("In"));
    }

    public NodePinType(String displayName, NodePinShape defaultShape,
                       @Nullable Consumer<Consumer<BlueprintNode>> menuBuilder) {
        this(displayName, defaultShape, 0xFF_FFFFFF, menuBuilder);
    }

    public NodePinType(String displayName) {
        this(displayName, NodePinShape.FILLED_TRIANGLE, 0xFF_FFFFFF, null);
    }

    public NodePin output(String label) {
        return new NodePin(this, label, NodePinConnectionType.OUTPUT, defaultShape);
    }

    public NodePin required(String label) {
        return new NodePin(this, label, NodePinConnectionType.REQUIRED_INPUT, defaultShape);
    }

    public NodePin optional(String label) {
        return new NodePin(this, label, NodePinConnectionType.OPTIONAL_INPUT, defaultShape);
    }

    public boolean isCompatibleWith(NodePinType<?> other) {
        if (this == other) return true;
        return "Any".equals(this.displayName) || "Any".equals(other.displayName);
    }

    public float r() {
        return ((color >> 16) & 0xFF) / 255f;
    }

    public float g() {
        return ((color >> 8) & 0xFF) / 255f;
    }

    public float b() {
        return (color & 0xFF) / 255f;
    }

    public float a() {
        return ((color >> 24) & 0xFF) / 255f;
    }
}