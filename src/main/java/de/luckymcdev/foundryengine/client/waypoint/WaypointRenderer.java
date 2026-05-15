package de.luckymcdev.foundryengine.client.waypoint;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.render.WorldViewMatrix;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.util.ChatIcons;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public class WaypointRenderer {
    public void renderWaypoints(RenderLevelStageEvent.AfterLevel context) {
        Minecraft mc = Client.getMc();
        if (mc.level == null) return;
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

            int color = new Color(w.color()).argb();
            float prefixWidth = font.width(icon);

            font.drawInBatch(
                    icon,
                    -prefixWidth / 2f, -10f,
                    color,
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
                    -distWidth / 2f, 2f,
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