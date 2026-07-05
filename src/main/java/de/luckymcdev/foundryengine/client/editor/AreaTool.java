package de.luckymcdev.foundryengine.client.editor;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.gizmo.WorldGizmo;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.AABBArea;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.network.packets.editor.AreaPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AreaTool {
    double storedDistance = 0;
    private Area selectedArea;
    private boolean draggingMin = false;
    private boolean wasUsing = false;
    private int useTicks = 0;

    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        boolean using = EditorController.isUsingEditorItem();

        if (using && !wasUsing) {
            wasUsing = true;
            useTicks = 0;
            storedDistance = 0;
            selectedArea = null;
            draggingMin = false;
            Client.getAreaRenderer().selectedCornerArea = null;
            Client.getAreaRenderer().draggingMin = false;
            Client.getAreaRenderer().storedDistance = 0;
        }

        if (using) {
            useTicks++;
            if (selectedArea == null) {
                pickAreaCorner(mc);
            }
            if (selectedArea != null) {
                updateDraggedCorner(mc);
            }
        }

        if (!using && wasUsing) {
            wasUsing = false;
            if (selectedArea != null && useTicks > 2) {
                sendAreaUpdate();
            }
            selectedArea = null;
            Client.getAreaRenderer().selectedCornerArea = null;
        }
    }

    public boolean onScroll(double vertical) {
        if (selectedArea == null) return false;
        if (!wasUsing) return false;
        storedDistance = Math.max(storedDistance + (vertical * 0.25), 0);
        Client.getAreaRenderer().storedDistance = storedDistance;
        return true;
    }

    public void onDeactivated() {
        wasUsing = false;
        useTicks = 0;
        storedDistance = 0;
        selectedArea = null;
        draggingMin = false;
        Client.getAreaRenderer().selectedCornerArea = null;
    }

    public void render() {
        Client.getAreaRenderer().render();
    }

    private void sendAreaUpdate() {
        if (selectedArea == null) return;
        var packet = AreaPacket.update(selectedArea);
        Common.getNetworkManager().sendToServer(packet);
    }

    private void pickAreaCorner(Minecraft mc) {
        var areas = Common.getAreaManager().getAreasForDimension(mc.level.dimension());
        if (areas == null || areas.isEmpty()) return;

        Vec3 eye = mc.player.getEyePosition();
        Vec3 look = mc.player.getViewVector(1.0f);

        Area closestArea = null;
        boolean closestIsMin = false;
        double closestDist = Double.MAX_VALUE;

        for (Area area : areas) {
            AABB bounds = area.bounds();
            Vec3 minCorner = new Vec3(bounds.minX, bounds.minY, bounds.minZ);
            Vec3 maxCorner = new Vec3(bounds.maxX, bounds.maxY, bounds.maxZ);

            for (var entry : new Vec3[]{minCorner, maxCorner}) {
                if (WorldGizmo.isHovered(entry, eye, look)) {
                    double dist = entry.distanceTo(eye);
                    if (dist < closestDist) {
                        closestDist = dist;
                        closestArea = area;
                        closestIsMin = entry == minCorner;
                    }
                }
            }
        }

        if (closestArea != null) {
            selectedArea = closestArea;
            draggingMin = closestIsMin;
            Vec3 corner = closestIsMin
                    ? new Vec3(closestArea.bounds().minX, closestArea.bounds().minY, closestArea.bounds().minZ)
                    : new Vec3(closestArea.bounds().maxX, closestArea.bounds().maxY, closestArea.bounds().maxZ);
            storedDistance = corner.distanceTo(eye);
            Client.getAreaRenderer().selectedCornerArea = closestArea;
            Client.getAreaRenderer().draggingMin = closestIsMin;
            Client.getAreaRenderer().storedDistance = storedDistance;
        }
    }

    private void updateDraggedCorner(Minecraft mc) {
        if (!(selectedArea instanceof AABBArea aabbArea)) return;

        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 eye = mc.player.getEyePosition(partial);
        Vec3 look = mc.player.getViewVector(partial);
        Vec3 newPos = eye.add(look.scale(storedDistance));

        AABB oldBounds = aabbArea.bounds();
        AABB newBounds;

        if (draggingMin) {
            newBounds = new AABB(
                    Math.min(newPos.x, oldBounds.maxX - 0.5),
                    Math.min(newPos.y, oldBounds.maxY - 0.5),
                    Math.min(newPos.z, oldBounds.maxZ - 0.5),
                    oldBounds.maxX, oldBounds.maxY, oldBounds.maxZ
            );
        } else {
            newBounds = new AABB(
                    oldBounds.minX, oldBounds.minY, oldBounds.minZ,
                    Math.max(newPos.x, oldBounds.minX + 0.5),
                    Math.max(newPos.y, oldBounds.minY + 0.5),
                    Math.max(newPos.z, oldBounds.minZ + 0.5)
            );
        }

        aabbArea.setBounds(newBounds);
    }
}
