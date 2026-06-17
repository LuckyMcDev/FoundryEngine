package de.luckymcdev.foundryengine.client.node;

import de.luckymcdev.foundryengine.common.graph.type.NodePinShapeRef;
import de.luckymcdev.foundryengine.common.graph.type.PinType;

public record NodePin(PinType type, String label, NodePinConnectionType connectionType, NodePinShape shape) {
    public NodePin withShape(NodePinShape shape) {
        return new NodePin(type, label, connectionType, shape);
    }

    public static NodePinShape toShape(NodePinShapeRef ref) {
        return switch (ref) {
            case CIRCLE -> NodePinShape.CIRCLE;
            case CIRCLE_FILLED -> NodePinShape.FILLED_CIRCLE;
            case TRIANGLE -> NodePinShape.TRIANGLE;
            case TRIANGLE_FILLED -> NodePinShape.FILLED_TRIANGLE;
            case QUAD, SQUARE -> NodePinShape.SQUARE;
            case QUAD_FILLED, SQUARE_FILLED -> NodePinShape.FILLED_SQUARE;
            case DIAMOND -> NodePinShape.TRIANGLE;
            case DIAMOND_FILLED -> NodePinShape.FILLED_TRIANGLE;
        };
    }
}
