package de.luckymcdev.foundryengine.client.editor.feature;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.HandlePicker;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.AABBArea;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.network.packets.editor.AreaPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class AreaEditorFeature extends DragEditorFeature {
    private Area selectedArea;
    private boolean draggingMin = false;

    @Override
    protected void onDragStart() {
        selectedArea = null;
        draggingMin = false;
        Client.getAreaRenderer().selectedCornerArea = null;
        Client.getAreaRenderer().draggingMin = false;
        Client.getAreaRenderer().storedDistance = 0;
    }

    @Override
    protected void onDragTick(Minecraft mc) {
        if (selectedArea == null) {
            pickAreaCorner(mc);
        }
        if (selectedArea != null) {
            updateDraggedCorner(mc);
        }
    }

    @Override
    protected void onDragEnd() {
        if (selectedArea != null && useTicks > 2) {
            sendAreaUpdate();
        }
        selectedArea = null;
        Client.getAreaRenderer().selectedCornerArea = null;
    }

    @Override
    public void render() {
        Client.getAreaRenderer().render();
    }

    @Override
    protected void onDistanceChanged() {
        Client.getAreaRenderer().storedDistance = storedDistance;
    }

    @Override
    public boolean onScroll(double vertical) {
        if (selectedArea == null) return false;
        return super.onScroll(vertical);
    }

    @Override
    protected void reset() {
        super.reset();
        selectedArea = null;
        draggingMin = false;
        Client.getAreaRenderer().selectedCornerArea = null;
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
                if (HandlePicker.isHovered(entry, eye, look)) {
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