package de.luckymcdev.foundryengine.common.blueprint.graph;

import de.luckymcdev.foundryengine.common.blueprint.nodes.PinRenderer;
import de.luckymcdev.foundryengine.common.util.color.Color;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class NodePinType<T> {
    public final String displayName;
    public final NodePinShape defaultShape;
    public final Color color;
    public final List<NodePin> singleOutput;
    public final List<NodePin> singleRequiredInput;
    public final @Nullable List<String> enumValues;
    public final @Nullable PinRenderer renderer;
    public final @Nullable Class<?> runtimeType;
    private final Set<String> compatibleNames;

    public NodePinType(String displayName, NodePinShape defaultShape, Color color,
                       @Nullable List<String> enumValues, @Nullable PinRenderer renderer,
                       @Nullable Class<?> runtimeType, String... compatibleWith) {
        this.displayName = displayName;
        this.defaultShape = defaultShape;
        this.color = color;
        this.enumValues = enumValues;
        this.renderer = renderer;
        this.runtimeType = runtimeType;
        this.singleOutput = List.of(output("Out"));
        this.singleRequiredInput = List.of(required("In"));
        this.compatibleNames = new LinkedHashSet<>();
        this.compatibleNames.add(displayName);
        this.compatibleNames.addAll(List.of(compatibleWith));
    }

    // ---- Convenience constructors ----

    /**
     * Auto color, no enum, no renderer.
     */
    public NodePinType(String displayName, NodePinShape defaultShape, String... compatibleWith) {
        this(displayName, defaultShape, deriveColor(displayName), null, null, null, compatibleWith);
    }

    /**
     * Auto color, enum values + combo renderer, optional compat.
     */
    public NodePinType(String displayName, NodePinShape defaultShape,
                       List<String> enumValues, String... compatibleWith) {
        this(displayName, defaultShape, deriveColor(displayName), enumValues,
                enumValues != null && !enumValues.isEmpty() ? PinRenderer.enumPin(enumValues) : null,
                null, compatibleWith);
    }

    /**
     * Auto color, enum, no shape override (FILLED_CIRCLE).
     */
    public NodePinType(String displayName, List<String> enumValues, String... compatibleWith) {
        this(displayName, NodePinShape.FILLED_CIRCLE, enumValues, compatibleWith);
    }

    /**
     * Explicit color + renderer.
     */
    public NodePinType(String displayName, NodePinShape defaultShape, Color color,
                       @Nullable PinRenderer renderer, String... compatibleWith) {
        this(displayName, defaultShape, color, null, renderer, null, compatibleWith);
    }

    /**
     * Explicit renderer, auto color.
     */
    public NodePinType(String displayName, NodePinShape defaultShape,
                       @Nullable PinRenderer renderer, String... compatibleWith) {
        this(displayName, defaultShape, deriveColor(displayName), null, renderer, null, compatibleWith);
    }

    /**
     * Auto color, no enum, no renderer, with runtime Class.
     */
    public NodePinType(String displayName, NodePinShape defaultShape,
                       Class<?> runtimeType, String... compatibleWith) {
        this(displayName, defaultShape, deriveColor(displayName), null, null, runtimeType, compatibleWith);
    }

    private static Color deriveColor(String name) {
        float hue = (name.hashCode() & 0x7FFF) / 32767f;
        return new Color(0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.75f, 0.9f));
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
        if ("Any".equals(this.displayName) || "Any".equals(other.displayName)) return true;
        return this.compatibleNames.contains(other.displayName)
                || other.compatibleNames.contains(this.displayName);
    }

    public float r() {
        return color.r();
    }

    public float g() {
        return color.g();
    }

    public float b() {
        return color.b();
    }

    public float a() {
        return color.a();
    }
}
