package de.luckymcdev.foundryengine.client.imgui.imnodes;

import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePin;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinConnectionType;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinInfo;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A single node in the blueprint graph.
 */
public class Node {
    public final List<NodePinInfo> inputPins;
    public final List<NodePinInfo> outputPins;
    public final Map<String, Object> outputValues = new HashMap<>();
    public int id;
    public String name;
    public @Nullable String category;
    public boolean selected;

    public Node(String name, @Nullable String category, List<NodePin> pins) {
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

    public Node(String name, List<NodePin> pins) {
        this(name, null, pins);
    }

    public Node(List<NodePin> pins) {
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