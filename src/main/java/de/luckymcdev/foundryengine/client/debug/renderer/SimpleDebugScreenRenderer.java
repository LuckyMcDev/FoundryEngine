package de.luckymcdev.foundryengine.client.debug.renderer;

import de.luckymcdev.foundryengine.client.debug.screen.SimpleDebugScreenEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.util.debug.DebugValueAccess;
import org.joml.Vector3d;
import org.jspecify.annotations.NonNull;

/**
 * An inline implementation for a DebugRenderer.
 * For Text see {@link SimpleDebugScreenEntry}
 */
public class SimpleDebugScreenRenderer implements DebugRenderer.SimpleDebugRenderer {
	private final SimpleDebugScreenRendererContext<Minecraft, Vector3d, DebugValueAccess, Frustum, Float> context;
	private final Vector3d camPos;
	private final Minecraft minecraft;

	public SimpleDebugScreenRenderer(Minecraft minecraft, SimpleDebugScreenRendererContext<Minecraft, Vector3d, DebugValueAccess, Frustum, Float> renderer) {
		this.minecraft = minecraft;
		this.context = renderer;
		this.camPos = new Vector3d();
	}

	@Override
	public void emitGizmos(double camX, double camY, double camZ, @NonNull DebugValueAccess debugValueAccess, @NonNull Frustum frustum, float partialTick) {
		camPos.x = camX;
		camPos.y = camY;
		camPos.z = camZ;
		context.accept(minecraft, camPos, debugValueAccess, frustum, partialTick);
	}

	@FunctionalInterface
	public interface SimpleDebugScreenRendererContext<minecraft, camPos, debugValueAccess, frustum, partialTick> {
		void accept(Minecraft minecraft, Vector3d camPos, DebugValueAccess debugValueAccess, Frustum frustum, Float partialTicks);
	}
}
