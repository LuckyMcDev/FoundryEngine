package de.luckymcdev.foundryengine.client.area;

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
    public static @Nullable Area selectedCornerArea;
    public static boolean draggingMin = false;
    public static double storedDistance = 0;

    public static void render() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ClientLevel level = mc.level;
        List<Area> areas = Common.getAreaManager().getAreasForDimension(level.dimension());
        if (areas == null || areas.isEmpty()) return;

        for (Area area : areas) {
            area.drawDebugOutline(Color.RED.argb());

            var b = area.bounds();
            Vec3 min = new Vec3(b.minX, b.minY, b.minZ);
            Vec3 max = new Vec3(b.maxX, b.maxY, b.maxZ);

            boolean isSelected = selectedCornerArea != null && selectedCornerArea.id().equals(area.id());
            int dimColor = isSelected ? 0x88FFFFFF : 0x44FFFFFF;
            int brightColor = 0xFFFFFFFF;

            HandleRenderer.renderHandle(min, 0.15, isSelected && draggingMin ? brightColor : dimColor);
            HandleRenderer.renderHandle(max, 0.15, isSelected && !draggingMin ? brightColor : dimColor);
        }
    }
}
