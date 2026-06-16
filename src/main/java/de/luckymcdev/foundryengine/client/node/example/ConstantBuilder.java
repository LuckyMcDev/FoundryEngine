package de.luckymcdev.foundryengine.client.node.example;

import de.luckymcdev.foundryengine.client.node.*;
import imgui.ImGui;
import java.util.List;

public class ConstantBuilder implements NodeBuilder<Double> {
    private double value = 0.0;
    private Node<Double> node;

    @Override
    public List<NodePin<Double>> getPins() {
        return List.of(
                new NodePin<>(NodeTypes.DOUBLE, "Out", NodePinConnectionType.OUTPUT, NodePinShape.FILLED_CIRCLE)
        );
    }

    @Override
    public boolean render() {
        float[] val = {(float) value};
        boolean changed = ImGui.sliderFloat("Value", val, -10f, 10f);
        if (changed) value = val[0];
        return changed;
    }

    @Override
    public Double evaluate() {
        return value;
    }

    @Override
    public String getDisplayName() {
        return "Constant";
    }

    @Override
    public void setNode(Node<Double> node) {
        this.node = node;
    }
}