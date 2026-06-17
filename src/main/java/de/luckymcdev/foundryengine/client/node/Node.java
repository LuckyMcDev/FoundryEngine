package de.luckymcdev.foundryengine.client.node;

import java.util.ArrayList;
import java.util.List;

public class Node {
    public final List<NodePinInfo> inputPins;
    public final List<NodePinInfo> outputPins;
    public int id;
    public boolean selected;
    public NodeBuilder builder;

    public Node(List<NodePin> pins) {
        this.inputPins = new ArrayList<>(1);
        this.outputPins = new ArrayList<>(1);
        for (var pin : pins) {
            var pinInfo = new NodePinInfo(this, pin);
            if (pin.connectionType() == NodePinConnectionType.OUTPUT) {
                outputPins.add(pinInfo);
            } else {
                inputPins.add(pinInfo);
            }
        }
    }

    public void setBuilder(NodeBuilder builder) {
        this.builder = builder;
        builder.setNode(this);
    }
}
