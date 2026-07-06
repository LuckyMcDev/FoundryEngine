package de.luckymcdev.foundryengine.client.waypoint;

import com.mojang.blaze3d.platform.InputConstants;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.render.WorldViewMatrix;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.util.ChatIcons;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

public class ClientWaypointManager {
	public static final KeyMapping PRIMARY_WAYPOINT_KEY = new KeyMapping(
		"key.foundryengine.primary_waypoint",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_B,
		Client.EDITOR_CATEGORY
	);
	public static final KeyMapping REMOVE_WAYPOINT_KEY = new KeyMapping(
		"key.foundryengine.remove_waypoint",
		InputConstants.Type.KEYSYM,
		GLFW.GLFW_KEY_M,
		Client.EDITOR_CATEGORY
	);

	public void renderWaypoints(RenderLevelStageEvent.AfterLevel context) {
		Minecraft mc = Client.getMc();
		if (mc.level == null) {
			return;
		}
		MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
		Vec3 cameraPos = context.getLevelRenderState().cameraRenderState.pos;
		Font font = mc.font;

		for (var w : Common.getWaypointManager().getWaypoints(mc.level.dimension())) {
			Vec3 wpPos = new Vec3(w.x() + 0.5, w.y() + 1.2, w.z() + 0.5);
			double dist = cameraPos.distanceTo(wpPos);

			String distText = String.format("%.1fb", dist);
			Component icon = Component.literal(w.icon()).setStyle(ChatIcons.ICONS);

			float baseScale = 0.05f;
			float distScale = (float) Math.max(1.0, dist / 10.0);
			float finalScale = baseScale * distScale;

			Matrix4f iconMv = WorldViewMatrix.from(context)
				.at((float) wpPos.x, (float) wpPos.y, (float) wpPos.z)
				.billboard()
				.scale(finalScale, -finalScale, finalScale)
				.buildModelView();

			Color color = w.color();
			float prefixWidth = font.width(icon);

			font.drawInBatch(
				icon,
				-prefixWidth / 2.0f, -10.0f,
				color.argb(),
				false,
				iconMv,
				bufferSource,
				Font.DisplayMode.SEE_THROUGH,
				0,
				15728880
			);

			Matrix4f textMv = new Matrix4f(iconMv).scale(0.6f);
			float distWidth = font.width(distText);

			font.drawInBatch(
				distText,
				-distWidth / 2.0f, 2.0f,
				0xFFFFFFFF,
				false,
				textMv,
				bufferSource,
				Font.DisplayMode.SEE_THROUGH,
				0,
				15728880
			);
		}
		bufferSource.endBatch();
	}
}