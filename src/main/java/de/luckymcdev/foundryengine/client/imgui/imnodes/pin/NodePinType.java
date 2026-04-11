package de.luckymcdev.foundryengine.client.imgui.imnodes.pin;

import de.luckymcdev.foundryengine.client.imgui.imnodes.Node;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class NodePinType<T> {
    public final String displayName;
    public final NodePinShape defaultShape;
    public final int color;
    public final List<NodePin> singleOutput;
    public final List<NodePin> singleRequiredInput;
    public final @Nullable Consumer<Consumer<Node>> menuBuilder;

    public NodePinType(String displayName, NodePinShape defaultShape, int color,
                       @Nullable Consumer<Consumer<Node>> menuBuilder) {
        this.displayName = displayName;
        this.defaultShape = defaultShape;
        this.color = color;
        this.menuBuilder = menuBuilder;
        this.singleOutput = List.of(output("Out"));
        this.singleRequiredInput = List.of(required("In"));
    }

    public NodePinType(String displayName, NodePinShape defaultShape,
                       @Nullable Consumer<Consumer<Node>> menuBuilder) {
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