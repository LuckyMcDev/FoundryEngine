package de.luckymcdev.foundryengine.client.gizmo;

//? if 26.1 {
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
//?}
//? if 26.2 {

/*import net.minecraft.client.renderer.SubmitNodeCollector;
*///?}
import net.minecraft.client.renderer.state.level.CameraRenderState;
//? if 26.1 {
import org.joml.Matrix4fc;
 //?}

public final class GizmoRenderer {
	private GizmoRenderer() {
	}

	//? if 26.1 {
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
	//?} elif 26.2 {
	/*public static void render(SubmitNodeCollector collector, CameraRenderState camera, boolean onTop) {
		GizmoBuffer.lines().submit(collector, camera, onTop);
		GizmoBuffer.fills().submit(collector, camera, onTop);
	}
	*///?}
}
