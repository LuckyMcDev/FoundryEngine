package de.luckymcdev.foundryengine.client.gizmo;

import net.minecraft.client.renderer.gizmos.DrawableGizmoPrimitives;

public final class GizmoBuffer {
	private static DrawableGizmoPrimitives lines = new DrawableGizmoPrimitives();
	private static DrawableGizmoPrimitives fills = new DrawableGizmoPrimitives();

	private GizmoBuffer() {
	}

	public static void startFrame() {
		lines = new DrawableGizmoPrimitives();
		fills = new DrawableGizmoPrimitives();
	}

	public static DrawableGizmoPrimitives lines() {
		return lines;
	}

	public static DrawableGizmoPrimitives fills() {
		return fills;
	}
}
