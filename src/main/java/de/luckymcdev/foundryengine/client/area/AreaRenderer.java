package de.luckymcdev.foundryengine.client.area;

import de.luckymcdev.foundryengine.client.editor.feature.AreaFeature;
import de.luckymcdev.foundryengine.client.render.HandleRenderer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.area.module.AreaRenderModule;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class AreaRenderer {
    private final double HANDLE_SIZE = 0.125;

    public @Nullable Area selectedCornerArea;
    public boolean draggingMin = false;
    public double storedDistance = 0;

    public void render() {
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

    public void renderAreaModules(RenderLevelStageEvent.AfterLevel event) {
        var mc = Minecraft.getInstance();
        if (!(mc.level instanceof ClientLevel level)) return;

        var poseStack = event.getPoseStack();
        var buffer = mc.renderBuffers().bufferSource();
        float partialTick = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        for (Area area : Common.getAreaManager().getAreasForDimension(level.dimension())) {
            for (Identifier mid : area.moduleIds()) {
                var module = Common.getAreaManager().getModuleType(mid);
                if (module instanceof AreaRenderModule renderModule) {
                    renderModule.render(level, area, poseStack, buffer, partialTick);
                }
            }
        }
    }
}
