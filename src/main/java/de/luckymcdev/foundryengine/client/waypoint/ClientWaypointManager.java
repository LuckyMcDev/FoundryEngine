package de.luckymcdev.foundryengine.client.waypoint;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.gizmo.WorldGizmo;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.util.color.Color;
import de.luckymcdev.foundryengine.common.waypoint.Waypoint;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.item.TrackingItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

//? if 26.1 {
import net.minecraft.network.chat.Component;
import de.luckymcdev.foundryengine.common.util.ChatIcons;
import de.luckymcdev.foundryengine.client.render.WorldViewMatrix;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
//?}
//? if 26.2 {
/*import com.mojang.blaze3d.GpuFormat;
 *///?}

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

	//? if 26.1 {
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

			float baseScale = 0.05f;
			float distScale = (float) Math.max(1.0, dist / 10.0);
			float finalScale = baseScale * distScale;

			String iconStr = w.icon();
			Color color = w.color();

			if (iconStr.contains(":")) {
				renderItemIcon(w, wpPos, mc, bufferSource, finalScale);
			} else {
				Component icon = Component.literal(iconStr).setStyle(ChatIcons.ICONS);
				float prefixWidth = font.width(icon);

				Matrix4f iconMv = WorldViewMatrix.from(context)
					.at((float) wpPos.x, (float) wpPos.y, (float) wpPos.z)
					.billboard()
					.scale(finalScale, -finalScale, finalScale)
					.buildModelView();

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
			}

			String distText = String.format("%.1fb", dist);
			Matrix4f textMv = WorldViewMatrix.from(context)
				.at((float) wpPos.x, (float) wpPos.y, (float) wpPos.z)
				.billboard()
				.scale(finalScale * 0.6f, -finalScale * 0.6f, finalScale * 0.6f)
				.buildModelView();
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

	private void renderItemIcon(Waypoint w, Vec3 wpPos, Minecraft mc,
	                            MultiBufferSource.BufferSource bufferSource, float scale) {
		var location = Identifier.parse(w.icon());
		var itemOpt = BuiltInRegistries.ITEM.getOptional(location);
		if (itemOpt.isEmpty() || itemOpt.get() == Items.AIR) {
			return;
		}
		ItemStack stack = new ItemStack(itemOpt.get());

		var resolver = mc.getItemModelResolver();
		var renderState = new TrackingItemStackRenderState();
		resolver.updateForTopItem(renderState, stack, ItemDisplayContext.GUI, mc.level, null, 0);

		var submitNodeCollector = mc.gameRenderer.getSubmitNodeStorage();
		var lighting = mc.gameRenderer.getLighting();
		Lighting.Entry lightingEntry = renderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
		lighting.setupFor(lightingEntry);

		PoseStack poseStack = new PoseStack();
		poseStack.translate(wpPos.x, wpPos.y, wpPos.z);
		poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
		poseStack.scale(scale, -scale, scale);

		renderState.submit(poseStack, submitNodeCollector, 15728880, OverlayTexture.NO_OVERLAY, 0);
	}
	//?} else {
	/*public void renderWaypoints(RenderLevelStageEvent.AfterLevel context, SubmitNodeStorage submitNodes) {
		Minecraft mc = Client.getMc();
		if (mc.level == null) {
			return;
		}
		Vec3 cameraPos = context.getLevelRenderState().cameraRenderState.pos;

		for (var w : Common.getWaypointManager().getWaypoints(mc.level.dimension())) {
			Vec3 wpPos = new Vec3(w.x() + 0.5, w.y() + 1.2, w.z() + 0.5);
			double dist = cameraPos.distanceTo(wpPos);

			float baseScale = 0.05f;
			float distScale = (float) Math.max(1.0, dist / 10.0);
			float finalScale = baseScale * distScale;

			String iconStr = w.icon();
			Color color = w.color();

			if (iconStr.contains(":")) {
				renderItemIcon(w, wpPos, mc, submitNodes, finalScale);
			} else {
				WorldGizmo.renderText(iconStr, wpPos, color, finalScale);
			}

			String distText = String.format("%.1fb", dist);
			Vec3 textPos = new Vec3(wpPos.x, wpPos.y - 0.25, wpPos.z);
			WorldGizmo.renderText(distText, textPos, Color.WHITE, finalScale * 0.6f);
		}
	}

	private void renderItemIcon(Waypoint w, Vec3 wpPos, Minecraft mc, SubmitNodeStorage submitNodes, float scale) {
		var location = Identifier.parse(w.icon());
		var itemOpt = BuiltInRegistries.ITEM.getOptional(location);
		if (itemOpt.isEmpty() || itemOpt.get() == Items.AIR) {
			return;
		}
		ItemStack stack = new ItemStack(itemOpt.get());

		var resolver = mc.getItemModelResolver();
		var renderState = new TrackingItemStackRenderState();
		resolver.updateForTopItem(renderState, stack, ItemDisplayContext.GUI, mc.level, null, 0);

		var lighting = mc.gameRenderer.lighting();
		Lighting.Entry lightingEntry = renderState.usesBlockLight() ? Lighting.Entry.ITEMS_3D : Lighting.Entry.ITEMS_FLAT;
		lighting.setupFor(lightingEntry);

		PoseStack poseStack = new PoseStack();
		poseStack.translate(wpPos.x, wpPos.y, wpPos.z);
		poseStack.mulPose(mc.gameRenderer.mainCamera().rotation());
		poseStack.scale(scale, -scale, scale);

		renderState.submit(poseStack, submitNodes, 15728880, OverlayTexture.NO_OVERLAY, 0);
	}
	*///?}
}