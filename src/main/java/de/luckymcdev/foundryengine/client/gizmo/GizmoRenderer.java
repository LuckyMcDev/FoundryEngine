package de.luckymcdev.foundryengine.client.gizmo;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Matrix4fc;

public final class GizmoRenderer {
	private GizmoRenderer() {
	}

	public static void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, CameraRenderState camera, Matrix4fc modelViewMatrix) {
		var lines = GizmoBuffer.lines();
		if (!lines.isEmpty()) {
			lines.render(poseStack, bufferSource, camera, modelViewMatrix);
		}

		var fills = GizmoBuffer.fills();
		if (!fills.isEmpty()) {
			fills.render(poseStack, bufferSource, camera, modelViewMatrix);
		}
		bufferSource.endBatch();
	}
}
