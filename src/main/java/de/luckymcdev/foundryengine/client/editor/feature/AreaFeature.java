package de.luckymcdev.foundryengine.client.editor.feature;

import de.luckymcdev.foundryengine.client.area.AreaRenderer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.area.Area;
import de.luckymcdev.foundryengine.common.network.packets.editor.AreaPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class AreaFeature extends DragEditorFeature {
    public static final double PICK_THRESHOLD = 0.8;

    private Area selectedArea;
    private boolean draggingMin = false;

    @Override
    protected void onDragStart() {
        selectedArea = null;
        draggingMin = false;
        AreaRenderer.selectedCornerArea = null;
        AreaRenderer.draggingMin = false;
        AreaRenderer.storedDistance = 0;
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
        AreaRenderer.selectedCornerArea = null;
    }

    @Override
    public void render() {
        AreaRenderer.render();
    }

    @Override
    protected void onDistanceChanged() {
        AreaRenderer.storedDistance = storedDistance;
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
        AreaRenderer.selectedCornerArea = null;
    }

    private void sendAreaUpdate() {
        if (selectedArea == null) return;
        AABB b = selectedArea.bounds();
        Area updated = Area.of(selectedArea.id(), new Vec3(b.minX, b.minY, b.minZ), new Vec3(b.maxX, b.maxY, b.maxZ), selectedArea.dimension(), selectedArea.color());
        var packet = AreaPacket.update(updated);
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
                Vec3 toCorner = entry.subtract(eye);
                double t = toCorner.dot(look);
                if (t < 0) continue;

                Vec3 projected = eye.add(look.scale(t));
                double dist = projected.distanceTo(entry);
                if (dist < PICK_THRESHOLD) {
                    double totalDist = t;
                    if (totalDist < closestDist) {
                        closestDist = totalDist;
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
            AreaRenderer.selectedCornerArea = closestArea;
            AreaRenderer.draggingMin = closestIsMin;
            AreaRenderer.storedDistance = storedDistance;
        }
    }

    private void updateDraggedCorner(Minecraft mc) {
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
        Vec3 eye = mc.player.getEyePosition(partial);
        Vec3 look = mc.player.getViewVector(partial);
        Vec3 newPos = eye.add(look.scale(storedDistance));

        AABB oldBounds = selectedArea.bounds();
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

        Area newArea = Area.of(selectedArea.id(),
                new Vec3(newBounds.minX, newBounds.minY, newBounds.minZ),
                new Vec3(newBounds.maxX, newBounds.maxY, newBounds.maxZ),
                selectedArea.dimension(),
                selectedArea.color());

        List<Area> areas = Common.getAreaManager().getAreasForDimension(selectedArea.dimension());
        for (int i = 0; i < areas.size(); i++) {
            if (areas.get(i).id().equals(selectedArea.id())) {
                areas.set(i, newArea);
                break;
            }
        }

        selectedArea = newArea;
    }
}
