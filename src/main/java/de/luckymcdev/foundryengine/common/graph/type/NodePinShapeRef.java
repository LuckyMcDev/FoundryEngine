package de.luckymcdev.foundryengine.common.graph.type;

/**
 * Shape references for pin rendering, matching ImNodes pin shapes.
 * Domain-agnostic so the API model doesn't depend on ImGui/ImNodes.
 */
public enum NodePinShapeRef {
    CIRCLE,
    CIRCLE_FILLED,
    TRIANGLE,
    TRIANGLE_FILLED,
    QUAD,
    QUAD_FILLED,
    DIAMOND,
    DIAMOND_FILLED,
    SQUARE,
    SQUARE_FILLED;
}
