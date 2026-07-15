package de.luckymcdev.foundryengine.client.gizmo;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.core.Direction;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

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

	public static void renderLine(Vec3 from, Vec3 to, Color color) {
		renderLine(from, to, color, 3.0F);
	}

	public static void renderLine(Vec3 from, Vec3 to, Color color, float width) {
		GizmoBuffer.lines().addLine(from, to, color.argb(), width);
	}

	public static void renderArrow(Vec3 from, Vec3 to, Color color, float width) {
		int argb = color.argb();
		var lines = GizmoBuffer.lines();
		lines.addLine(from, to, argb, width);
		var rotation = new Quaternionf().rotationTo(
			new Vector3f(1.0F, 0.0F, 0.0F),
			to.subtract(from).toVector3f().normalize()
		);
		float len = (float) Mth.clamp(to.distanceTo(from) * 0.1, 0.1, 1.0);
		var tips = new Vector3f[]{
			rotation.transform(-len, len, 0.0F, new Vector3f()),
			rotation.transform(-len, 0.0F, len, new Vector3f()),
			rotation.transform(-len, -len, 0.0F, new Vector3f()),
			rotation.transform(-len, 0.0F, -len, new Vector3f())
		};
		for (var tip : tips) {
			lines.addLine(to.add(tip.x, tip.y, tip.z), to, argb, width);
		}
	}

	public static void renderBox(Vec3 center, double halfExtent, Color color) {
		double x0 = center.x - halfExtent, y0 = center.y - halfExtent, z0 = center.z - halfExtent;
		double x1 = center.x + halfExtent, y1 = center.y + halfExtent, z1 = center.z + halfExtent;
		int argb = color.argb();
		var fills = GizmoBuffer.fills();
		fills.addQuad(new Vec3(x1, y0, z0), new Vec3(x1, y1, z0), new Vec3(x1, y1, z1), new Vec3(x1, y0, z1), argb);
		fills.addQuad(new Vec3(x0, y0, z0), new Vec3(x0, y0, z1), new Vec3(x0, y1, z1), new Vec3(x0, y1, z0), argb);
		fills.addQuad(new Vec3(x0, y0, z0), new Vec3(x0, y1, z0), new Vec3(x1, y1, z0), new Vec3(x1, y0, z0), argb);
		fills.addQuad(new Vec3(x0, y0, z1), new Vec3(x1, y0, z1), new Vec3(x1, y1, z1), new Vec3(x0, y1, z1), argb);
		fills.addQuad(new Vec3(x0, y1, z0), new Vec3(x0, y1, z1), new Vec3(x1, y1, z1), new Vec3(x1, y1, z0), argb);
		fills.addQuad(new Vec3(x0, y0, z0), new Vec3(x1, y0, z0), new Vec3(x1, y0, z1), new Vec3(x0, y0, z1), argb);
	}

	public static void renderOutline(AABB bounds, Color color) {
		renderOutline(bounds, color, 2.5F);
	}

	public static void renderOutline(AABB bounds, Color color, float strokeWidth) {
		double x0 = bounds.minX, y0 = bounds.minY, z0 = bounds.minZ;
		double x1 = bounds.maxX, y1 = bounds.maxY, z1 = bounds.maxZ;
		int argb = color.argb();
		var lines = GizmoBuffer.lines();
		lines.addLine(new Vec3(x0, y0, z0), new Vec3(x1, y0, z0), argb, strokeWidth);
		lines.addLine(new Vec3(x0, y0, z0), new Vec3(x0, y1, z0), argb, strokeWidth);
		lines.addLine(new Vec3(x0, y0, z0), new Vec3(x0, y0, z1), argb, strokeWidth);
		lines.addLine(new Vec3(x1, y0, z0), new Vec3(x1, y1, z0), argb, strokeWidth);
		lines.addLine(new Vec3(x1, y1, z0), new Vec3(x0, y1, z0), argb, strokeWidth);
		lines.addLine(new Vec3(x0, y1, z0), new Vec3(x0, y1, z1), argb, strokeWidth);
		lines.addLine(new Vec3(x0, y1, z1), new Vec3(x0, y0, z1), argb, strokeWidth);
		lines.addLine(new Vec3(x0, y0, z1), new Vec3(x1, y0, z1), argb, strokeWidth);
		lines.addLine(new Vec3(x1, y0, z1), new Vec3(x1, y0, z0), argb, strokeWidth);
		lines.addLine(new Vec3(x0, y1, z1), new Vec3(x1, y1, z1), argb, strokeWidth);
		lines.addLine(new Vec3(x1, y0, z1), new Vec3(x1, y1, z1), argb, strokeWidth);
		lines.addLine(new Vec3(x1, y1, z0), new Vec3(x1, y1, z1), argb, strokeWidth);
	}

	public static void renderFlatTile(Vec3 center, double radius, Direction face, Color color) {
		Vec3 cornerA = new Vec3(center.x - radius, center.y, center.z - radius);
		Vec3 cornerB = new Vec3(center.x + radius, center.y, center.z + radius);
		double ax = cornerA.x, ay = cornerA.y, az = cornerA.z;
		double bx = cornerB.x, by = cornerB.y, bz = cornerB.z;
		int argb = color.argb();
		var fills = GizmoBuffer.fills();
		switch (face) {
			case DOWN -> fills.addQuad(new Vec3(ax, ay, az), new Vec3(bx, ay, az), new Vec3(bx, ay, bz), new Vec3(ax, ay, bz), argb);
			case UP -> fills.addQuad(new Vec3(ax, by, az), new Vec3(ax, by, bz), new Vec3(bx, by, bz), new Vec3(bx, by, az), argb);
			case NORTH -> fills.addQuad(new Vec3(ax, ay, az), new Vec3(ax, by, az), new Vec3(bx, by, az), new Vec3(bx, ay, az), argb);
			case SOUTH -> fills.addQuad(new Vec3(ax, ay, bz), new Vec3(bx, ay, bz), new Vec3(bx, by, bz), new Vec3(ax, by, bz), argb);
			case WEST -> fills.addQuad(new Vec3(ax, ay, az), new Vec3(ax, ay, bz), new Vec3(ax, by, bz), new Vec3(ax, by, az), argb);
			case EAST -> fills.addQuad(new Vec3(bx, ay, az), new Vec3(bx, by, az), new Vec3(bx, by, bz), new Vec3(bx, ay, bz), argb);
		}
	}

	public static void renderText(String text, Vec3 position, Color color, float scale) {
		var style = TextGizmo.Style.forColorAndCentered(color.argb()).withScale(scale);
		GizmoBuffer.lines().addText(position, text, style);
	}
}