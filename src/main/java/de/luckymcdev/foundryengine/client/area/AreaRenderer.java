package de.luckymcdev.foundryengine.client.area;

import de.luckymcdev.foundryengine.client.editor.builtin.area.AreaPanel;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.area.AreaManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

import java.util.List;

public class AreaRenderer {
    public static void render() {
        if (!AreaPanel.INSTANCE.showDebugOutlines) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        ClientLevel level = mc.level;
        AreaManager areaManager = Common.getAreaManager();
        List<Area> areas = areaManager.getAreasForDimension(level.dimension());

        if (areas == null || areas.isEmpty()) {
            return;
        }
        for (Area area : areas) {
            area.drawDebugOutline();
        }
    }
}