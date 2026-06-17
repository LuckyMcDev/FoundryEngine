package de.luckymcdev.foundryengine.client.node.example;

import de.luckymcdev.foundryengine.client.node.*;
import imgui.ImGui;
import java.util.List;

public class ConstantBuilder implements NodeBuilder {
    private final float[] value = {0F};

    @Override
    public List<NodePin> getPins() {
        return List.of(NodeTypes.DOUBLE.output("Out").withShape(NodePinShape.FILLED_CIRCLE));
    }

    @Override
    public boolean render() {
        return ImGui.sliderFloat("Value", value, -10F, 10F);
    }

    @Override
    public Object evaluate() {
        return (double) value[0];
    }

    @Override
    public String getDisplayName() {
        return "Constant";
    }
}
