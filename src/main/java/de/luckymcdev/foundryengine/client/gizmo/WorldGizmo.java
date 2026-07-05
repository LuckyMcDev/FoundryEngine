package de.luckymcdev.foundryengine.client.gizmo;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WorldGizmo {
    private static final double HOVER_DEGREES = 1.0;

    private WorldGizmo() {
    }

    public static boolean isHovered(Vec3 target, Vec3 eye, Vec3 look) {
        Vec3 toTarget = target.subtract(eye);
        double t = toTarget.dot(look);
        if (t < 0) return false;
        double hAngle = Math.toDegrees(Math.atan2(
            toTarget.subtract(look.scale(t)).length(), t
        ));
        return hAngle < HOVER_DEGREES;
    }

    public static void renderLine(Vec3 from, Vec3 to, Color color) {
        renderLine(from, to, color, 3);
    }

    public static void renderLine(Vec3 from, Vec3 to, Color color, int width) {
        Gizmos.line(from, to, color.argb(), width);
    }

    public static void renderBox(Vec3 center, double halfExtent, Color color) {
        AABB bb = new AABB(
            center.add(new Vec3(-halfExtent, -halfExtent, -halfExtent)),
            center.add(new Vec3(halfExtent, halfExtent, halfExtent))
        );
        Gizmos.cuboid(bb, GizmoStyle.fill(color.argb()));
    }

    public static void renderOutline(AABB bounds, Color color) {
        Gizmos.cuboid(bounds, GizmoStyle.stroke(color.argb()));
    }
}
