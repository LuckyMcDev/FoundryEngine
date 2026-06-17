package de.luckymcdev.foundryengine.client.node.example;

import de.luckymcdev.foundryengine.client.node.*;
import imgui.ImGui;
import java.util.List;

public class MultiplyBuilder implements NodeBuilder {
    private Node node;

    @Override
    public List<NodePin> getPins() {
        return List.of(
                new NodePin(NodeTypes.DOUBLE.type, "A", NodePinConnectionType.REQUIRED_INPUT, NodePinShape.TRIANGLE),
                new NodePin(NodeTypes.DOUBLE.type, "B", NodePinConnectionType.REQUIRED_INPUT, NodePinShape.TRIANGLE),
                new NodePin(NodeTypes.DOUBLE.type, "Out", NodePinConnectionType.OUTPUT, NodePinShape.FILLED_TRIANGLE)
        );
    }

    @Override
    public boolean render() {
        ImGui.text("x");
        return false;
    }

    @Override
    public Object evaluate() {
        var inputs = node.inputPins;
        if (inputs.size() < 2) return 0.0;
        var leftLink = inputs.get(0).inputLink;
        var rightLink = inputs.get(1).inputLink;
        if (leftLink == null || rightLink == null) return 0.0;
        var leftVal = (Double) leftLink.node.builder.evaluate();
        var rightVal = (Double) rightLink.node.builder.evaluate();
        return leftVal * rightVal;
    }

    @Override
    public String getDisplayName() {
        return "Multiply";
    }

    @Override
    public void setNode(Node node) {
        this.node = node;
    }
}
