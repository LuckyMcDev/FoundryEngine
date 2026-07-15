package de.luckymcdev.foundryengine.client.gizmo;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.GizmoProperties;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class WorldGizmo {
	private static final double DEFAULT_HOVER_DEGREES = 5.0;

	public static boolean isHovered(Vec3 target, Vec3 eye, Vec3 look) {
		return isHovered(target, eye, look, DEFAULT_HOVER_DEGREES);
	}

	public static boolean isHovered(Vec3 target, Vec3 eye, Vec3 look, double thresholdDegrees) {
		Vec3 toTarget = target.subtract(eye);
		double t = toTarget.dot(look);
		if (t < 0) {
			return false;
		}
		double hAngle = Math.toDegrees(Math.atan2(
			toTarget.subtract(look.scale(t)).length(), t
		));
		return hAngle < thresholdDegrees;
	}

	public static boolean isHoveringBox(Vec3 eye, Vec3 look, AABB box, double maxDistance) {
		Vec3 end = eye.add(look.scale(maxDistance));
		return box.clip(eye, end).isPresent();
	}

	public static GizmoProperties renderLine(Vec3 from, Vec3 to, Color color) {
		return renderLine(from, to, color, 3.0F);
	}

	public static GizmoProperties renderLine(Vec3 from, Vec3 to, Color color, float width) {
		return Gizmos.line(from, to, color.argb(), width);
	}

	public static GizmoProperties renderArrow(Vec3 from, Vec3 to, Color color, float width) {
		return Gizmos.arrow(from, to, color.argb(), width);
	}

	public static GizmoProperties renderBox(Vec3 center, double halfExtent, Color color) {
		AABB bb = new AABB(
			center.x - halfExtent, center.y - halfExtent, center.z - halfExtent,
			center.x + halfExtent, center.y + halfExtent, center.z + halfExtent
		);
		return Gizmos.cuboid(bb, GizmoStyle.fill(color.argb()));
	}

	public static GizmoProperties renderOutline(AABB bounds, Color color) {
		return Gizmos.cuboid(bounds, GizmoStyle.stroke(color.argb()));
	}

	public static GizmoProperties renderOutline(AABB bounds, Color color, float strokeWidth) {
		return Gizmos.cuboid(bounds, GizmoStyle.stroke(color.argb(), strokeWidth));
	}

	public static GizmoProperties renderFlatTile(Vec3 center, double radius, Direction face, Color color) {
		Vec3 cornerA = new Vec3(center.x - radius, center.y, center.z - radius);
		Vec3 cornerB = new Vec3(center.x + radius, center.y, center.z + radius);
		return Gizmos.rect(cornerA, cornerB, face, GizmoStyle.fill(color.argb()));
	}

	public static GizmoProperties renderText(String text, Vec3 position, Color color, float scale) {
		TextGizmo.Style style = TextGizmo.Style.forColorAndCentered(color.argb()).withScale(scale);
		return Gizmos.billboardText(text, position, style);
	}
}