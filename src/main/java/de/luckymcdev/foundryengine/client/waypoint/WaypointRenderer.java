package de.luckymcdev.foundryengine.client.waypoint;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.render.WorldViewMatrix;
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
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        Vec3 cameraPos = context.getLevelRenderState().cameraRenderState.pos;
        Font font = mc.font;

        for (Waypoint wp : WaypointManager.getWaypoints()) {
            Vec3 wpPos = new Vec3(wp.getX() + 0.5, wp.getY() + 1.2, wp.getZ() + 0.5);
            double dist = cameraPos.distanceTo(wpPos);

            String distText = String.format("%.1fb", dist);

            Component icon = wp.icon();

            float baseScale = 0.05f;
            float distScale = (float) Math.max(1.0, dist / 10.0);
            float finalScale = baseScale * distScale;

            Matrix4f textMv = WorldViewMatrix.from(context)
                    .at((float) wpPos.x, (float) wpPos.y, (float) wpPos.z)
                    .billboard()
                    .scale(finalScale, -finalScale, finalScale)
                    .buildModelView();

            int color = wp.color().argb();
            float prefixWidth = font.width(icon);
            font.drawInBatch(
                    icon,
                    -prefixWidth / 2f, -10f,
                    color,
                    false,
                    textMv,
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    0,
                    15728880
            );
            textMv.scale(0.6f);

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