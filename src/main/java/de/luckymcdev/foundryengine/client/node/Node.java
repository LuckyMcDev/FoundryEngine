package de.luckymcdev.foundryengine.client.node;

import java.util.ArrayList;
import java.util.List;

public class Node<T> {
    public final List<NodePinInfo<T>> inputPins;
    public final List<NodePinInfo<T>> outputPins;
    public int id;
    public boolean selected;
    public NodeBuilder<T> builder;

    public Node(List<NodePin<T>> pins) {
        this.inputPins = new ArrayList<>(1);
        this.outputPins = new ArrayList<>(1);
        for (var pin : pins) {
            var pinInfo = new NodePinInfo<>(this, pin);
            if (pin.connectionType() == NodePinConnectionType.OUTPUT) {
                outputPins.add(pinInfo);
            } else {
                inputPins.add(pinInfo);
            }
        }
    }

    /**
     * Set builder and give it a back-reference.
     */
    public void setBuilder(NodeBuilder<T> builder) {
        this.builder = builder;
        builder.setNode(this);
    }
}