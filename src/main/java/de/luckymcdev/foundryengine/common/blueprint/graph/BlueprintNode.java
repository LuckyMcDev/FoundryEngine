package de.luckymcdev.foundryengine.common.blueprint.graph;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single node in the blueprint graph.
 */
public class BlueprintNode {
    public final List<NodePinInfo> inputPins;
    public final List<NodePinInfo> outputPins;
    public final Map<String, Object> outputValues = new HashMap<>();
    /**
     * Arbitrary node metadata (editor UI state, comments, colors, etc).
     * <p>
     * Kept separate from {@link #outputValues} so runtime execution doesn't
     * clobber editor-only state.
     */
    public final Map<String, Object> data = new HashMap<>();
    public int id;
    /**
     * Stable identifier used by the runtime (event dispatch, behavior lookup, etc).
     * Display/UI should generally use {@link #name}.
     */
    public String identifier;
    public String name;
    public @Nullable String category;
    /**
     * Only meaningful in the client editor; ignored server/common side.
     */
    public boolean selected;

    public BlueprintNode(String name, @Nullable String category, List<NodePin> pins) {
        this.identifier = name;
        this.name = name;
        this.category = category;
        this.inputPins = new ArrayList<>();
        this.outputPins = new ArrayList<>();

        for (var pin : pins) {
            var pinInfo = new NodePinInfo(this, pin);
            if (pin.connectionType() == NodePinConnectionType.OUTPUT) {
                outputPins.add(pinInfo);
            } else {
                inputPins.add(pinInfo);
            }
        }
    }

    public BlueprintNode(String name, List<NodePin> pins) {
        this(name, null, pins);
    }

    public BlueprintNode(List<NodePin> pins) {
        this("Node", null, pins);
    }

    public void setOutput(String label, @Nullable Object value) {
        outputValues.put(label, value);
    }

    public @Nullable Object getOutput(String label) {
        return outputValues.get(label);
    }

    public @Nullable NodePinInfo inputPin(String label) {
        for (var p : inputPins) if (p.pin.label().equals(label)) return p;
        return null;
    }

    public @Nullable NodePinInfo outputPin(String label) {
        for (var p : outputPins) if (p.pin.label().equals(label)) return p;
        return null;
    }
}
