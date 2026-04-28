package de.luckymcdev.foundryengine.common.blueprint.engine;

import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinShape;
import de.luckymcdev.foundryengine.common.blueprint.graph.NodePinType;

/**
 * Central catalogue of built-in {@link NodePinType} instances that mirror
 * Unreal Engine 5's visual language.
 */
public final class BlueprintTypes {
    /**
     * White triangles – control flow.
     */
    public static final NodePinType<Void> EXEC =
            new NodePinType<>("Exec", NodePinShape.FILLED_TRIANGLE, 0xFF_FFFFFF, null);
    /**
     * Red circle – boolean value.
     */
    public static final NodePinType<Boolean> BOOL =
            new NodePinType<>("Bool", NodePinShape.FILLED_CIRCLE, 0xFF_E05555, null);
    /**
     * Teal circle – integer value.
     */
    public static final NodePinType<Integer> INT =
            new NodePinType<>("Int", NodePinShape.FILLED_CIRCLE, 0xFF_4FC3A1, null);
    /**
     * Green circle – floating-point value.
     */
    public static final NodePinType<Float> FLOAT =
            new NodePinType<>("Float", NodePinShape.FILLED_CIRCLE, 0xFF_83C567, null);
    /**
     * Magenta circle – text value.
     */
    public static final NodePinType<String> STRING =
            new NodePinType<>("String", NodePinShape.FILLED_CIRCLE, 0xFF_D85BC9, null);
    /**
     * Blue square – arbitrary object / reference.
     */
    public static final NodePinType<Object> OBJECT =
            new NodePinType<>("Object", NodePinShape.FILLED_SQUARE, 0xFF_4F8FE8, null);
    /**
     * Gray circle – wildcard, compatible with any other type.
     */
    public static final NodePinType<Object> ANY =
            new NodePinType<>("Any", NodePinShape.CIRCLE, 0xFF_AAAAAA, null);

    private BlueprintTypes() {
    }
}