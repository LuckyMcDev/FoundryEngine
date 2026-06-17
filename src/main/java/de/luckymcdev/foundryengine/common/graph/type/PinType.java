package de.luckymcdev.foundryengine.common.graph.type;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.resources.Identifier;

import java.util.Set;

/**
 * Describes a pin's data type. Replaces the generic {@code NodePinType<T>}
 * with a flat, identifier-keyed descriptor that all domains share.
 *
 * <p>Type compatibility is checked via {@link #canConnectTo(PinType)}.
 * By default only identical types connect; subclasses or instances built
 * with {@link #withCompatible(Set)} can add implicit coercion rules
 * (e.g. {@code INT → FLOAT}, {@code FLOAT → VEC2}).
 */
public class PinType {

    public static final PinType EXEC = new PinType("exec", "Exec", 0xFFCC4444, NodePinShapeRef.QUAD_FILLED, Set.of());
    public static final PinType BOOL = new PinType("bool", "Boolean", 0xFF4488FF, NodePinShapeRef.CIRCLE_FILLED, Set.of());
    public static final PinType INT = new PinType("int", "Integer", 0xFF44AA44, NodePinShapeRef.TRIANGLE_FILLED, Set.of());
    public static final PinType FLOAT = new PinType("float", "Float", 0xFF44CCFF, NodePinShapeRef.TRIANGLE, Set.of(INT));
    public static final PinType STRING = new PinType("string", "String", 0xFFFFAA44, NodePinShapeRef.CIRCLE, Set.of());
    public static final PinType DOUBLE = new PinType("double", "Double", 0xFF44CCFF, NodePinShapeRef.CIRCLE_FILLED, Set.of(INT, FLOAT));
    public static final PinType VEC2 = new PinType("vec2", "Vector2", 0xFF88FF44, NodePinShapeRef.DIAMOND, Set.of());
    public static final PinType VEC3 = new PinType("vec3", "Vector3", 0xFF66DD22, NodePinShapeRef.DIAMOND, Set.of(VEC2));
    public static final PinType VEC4 = new PinType("vec4", "Vector4", 0xFF44BB00, NodePinShapeRef.DIAMOND_FILLED, Set.of(VEC3));
    public static final PinType MAT4 = new PinType("mat4", "Matrix4", 0xFFCC8844, NodePinShapeRef.SQUARE_FILLED, Set.of());
    public static final PinType SAMPLER = new PinType("sampler", "Sampler", 0xFFFF4488, NodePinShapeRef.SQUARE, Set.of());
    public static final PinType JSON_OBJECT = new PinType("json_object", "Object", 0xFFAA66CC, NodePinShapeRef.SQUARE_FILLED, Set.of());
    public static final PinType JSON_ARRAY = new PinType("json_array", "Array", 0xFFCC66AA, NodePinShapeRef.SQUARE, Set.of());
    public static final PinType JSON_VALUE = new PinType("json_value", "Value", 0xFF888888, NodePinShapeRef.CIRCLE, Set.of());
    public static final PinType ANY = new PinType("any", "Any", 0xFFAAAAAA, NodePinShapeRef.CIRCLE, Set.of());

    private final Identifier id;
    private final String displayName;
    private final int color;
    private final NodePinShapeRef defaultShape;
    private final IntSet compatibleIds;

    public PinType(String id, String displayName, int color, NodePinShapeRef defaultShape, Set<PinType> compatible) {
        this.id = Identifier.fromNamespaceAndPath("foundryengine", id);
        this.displayName = displayName;
        this.color = color;
        this.defaultShape = defaultShape;
        this.compatibleIds = new IntArraySet(compatible.size() + 1);
        this.compatibleIds.add(this.id.hashCode());
        for (var type : compatible) {
            this.compatibleIds.add(type.id.hashCode());
        }
    }

    public Identifier id() { return id; }
    public String displayName() { return displayName; }
    public int color() { return color; }
    public NodePinShapeRef defaultShape() { return defaultShape; }

    public boolean canConnectTo(PinType other) {
        return this == ANY || other == ANY
                || compatibleIds.contains(other.id.hashCode());
    }

    @Override
    public String toString() {
        return "PinType[" + id + "]";
    }
}
