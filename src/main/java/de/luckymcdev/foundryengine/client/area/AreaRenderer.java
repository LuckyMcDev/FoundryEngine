package de.luckymcdev.foundryengine.client.area;

import de.luckymcdev.foundryengine.client.editor.feature.AreaFeature;
import de.luckymcdev.foundryengine.client.render.HandleRenderer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AreaRenderer {
    private static final double HANDLE_SIZE = 0.125;

    public static @Nullable Area selectedCornerArea;
    public static boolean draggingMin = false;
    public static double storedDistance = 0;

    public static void render() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        ClientLevel level = mc.level;
        List<Area> areas = Common.getAreaManager().getAreasForDimension(level.dimension());
        if (areas == null || areas.isEmpty()) return;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getViewVector(1.0f);
        double threshold = AreaFeature.PICK_THRESHOLD;

        for (Area area : areas) {
            area.drawDebugOutline();

            var b = area.bounds();
            Vec3 min = new Vec3(b.minX, b.minY, b.minZ);
            Vec3 max = new Vec3(b.maxX, b.maxY, b.maxZ);

            boolean isSelected = selectedCornerArea != null && selectedCornerArea.id().equals(area.id());
            boolean hoverMin = HandleRenderer.isHovered(min, eye, look, threshold);
            boolean hoverMax = HandleRenderer.isHovered(max, eye, look, threshold);

            Color minColor = (isSelected && draggingMin) || hoverMin ? Color.WHITE : new Color(0x88FFFFFF);
            Color maxColor = (isSelected && !draggingMin) || hoverMax ? Color.WHITE : new Color(0x88FFFFFF);

            HandleRenderer.renderHandle(min, HANDLE_SIZE, minColor);
            HandleRenderer.renderHandle(max, HANDLE_SIZE, maxColor);
        }
    }

}
