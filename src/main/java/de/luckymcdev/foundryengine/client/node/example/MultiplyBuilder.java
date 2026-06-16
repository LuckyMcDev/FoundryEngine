package de.luckymcdev.foundryengine.client.node.example;

import de.luckymcdev.foundryengine.client.node.*;
import imgui.ImGui;
import java.util.List;

public class MultiplyBuilder implements NodeBuilder<Double> {
    private Node<Double> node;

    @Override
    public List<NodePin<Double>> getPins() {
        return List.of(
                new NodePin<>(NodeTypes.DOUBLE, "A", NodePinConnectionType.REQUIRED_INPUT, NodePinShape.TRIANGLE),
                new NodePin<>(NodeTypes.DOUBLE, "B", NodePinConnectionType.REQUIRED_INPUT, NodePinShape.TRIANGLE),
                new NodePin<>(NodeTypes.DOUBLE, "Out", NodePinConnectionType.OUTPUT, NodePinShape.FILLED_TRIANGLE)
        );
    }

    @Override
    public boolean render() {
        ImGui.text("×");
        return false;
    }

    @Override
    public Double evaluate() {
        var inputs = node.inputPins;
        if (inputs.size() < 2) return 0.0;
        var leftLink = inputs.get(0).inputLink;
        var rightLink = inputs.get(1).inputLink;
        if (leftLink == null || rightLink == null) return 0.0;
        Double leftVal = leftLink.node.builder.evaluate();
        Double rightVal = rightLink.node.builder.evaluate();
        return leftVal * rightVal;
    }

    @Override
    public String getDisplayName() {
        return "Multiply";
    }

    @Override
    public void setNode(Node<Double> node) {
        this.node = node;
    }
}