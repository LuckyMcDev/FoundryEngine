package de.luckymcdev.foundryengine.client.render;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class HandleRenderer {
    private static final double DEFAULT_SIZE = 0.15;

    public static void renderHandle(Vec3 position, Color color) {
        renderHandle(position, DEFAULT_SIZE, color);
    }

    public static void renderHandle(Vec3 position, double size, Color color) {
        AABB bb = new AABB(
                position.add(new Vec3(-size, -size, -size)),
                position.add(new Vec3(size, size, size))
        );
        Gizmos.cuboid(bb, GizmoStyle.stroke(color.argb()), true);
    }

    public static void renderHandle(Vec3 position, double size, Color outlineColor, Color fillColor) {
        AABB bb = new AABB(
                position.add(new Vec3(-size, -size, -size)),
                position.add(new Vec3(size, size, size))
        );
        Gizmos.cuboid(bb, GizmoStyle.fill(fillColor.argb()));
    }

    public static void renderLine(Vec3 from, Vec3 to, Color color) {
        renderLine(from, to, color, 3);
    }

    public static void renderLine(Vec3 from, Vec3 to, Color color, int width) {
        Gizmos.line(from, to, color.argb(), width);
    }

    public static void renderOutline(AABB bounds, Color color) {
        Gizmos.cuboid(bounds, GizmoStyle.stroke(color.argb()));
    }

    public static boolean isHovered(Vec3 position, Vec3 eye, Vec3 look, double threshold) {
        Vec3 toPos = position.subtract(eye);
        double t = toPos.dot(look);
        if (t < 0) return false;
        Vec3 projected = eye.add(look.scale(t));
        return projected.distanceTo(position) < threshold;
    }
}
