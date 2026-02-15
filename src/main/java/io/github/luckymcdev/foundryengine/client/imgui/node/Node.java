package io.github.luckymcdev.foundryengine.client.imgui.node;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public final List<NodePinInfo> inputPins;
    public final List<NodePinInfo> outputPins;
    public int id;
    public String name;
    public boolean selected;

    public Node(String name, List<NodePin> pins) {
        this.name = name;
        this.inputPins = new ArrayList<>();
        this.outputPins = new ArrayList<>();

        if (pins != null) {
            for (var pin : pins) {
                var pinInfo = new NodePinInfo(this, pin);

                if (pin.connectionType() == NodePinConnectionType.OUTPUT) {
                    outputPins.add(pinInfo);
                } else {
                    inputPins.add(pinInfo);
                }
            }
        }
    }

    public Node(List<NodePin> pins) {
        this("Node", pins);
    }
}