package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.client.editor.feature.DragEditorFeature;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.network.CutscenePacket;
import de.luckymcdev.foundryengine.common.easing.BezierPath;
import de.luckymcdev.foundryengine.common.easing.BezierPoint;
import de.luckymcdev.foundryengine.common.easing.BezierSpline;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Client-side in-world cutscene editor input handler.
 * <p>
 * Owned by {@link de.luckymcdev.foundryengine.client.Client} (instance, not static state).
 */
public class CutsceneEditor extends DragEditorFeature {
    private BezierPoint selectedPoint;
    private boolean changed = false;

    private static BezierPoint pickHovered(LivingEntity user) {
        for (Cutscene cutscene : CutsceneRenderer.getCutscenes()) {
            for (BezierSpline spline : cutscene.path.splines) {
                for (BezierPoint point : spline.points) {
                    if (point.isHovered(user)) {
                        return point;
                    }
                }
            }
        }
        return null;
    }

    @Override
    protected void onDragStart() {
        selectedPoint = null;
        changed = false;
        CutsceneRenderer.storedPoint = null;
        CutsceneRenderer.storedDistance = 0;
    }

    @Override
    protected void onDragTick(Minecraft mc) {
        if (selectedPoint == null) {
            selectedPoint = pickHovered(mc.player);
            if (selectedPoint != null) {
                CutsceneRenderer.storedPoint = selectedPoint;
                storedDistance = selectedPoint.getPos().distanceTo(mc.player.getEyePosition());
                CutsceneRenderer.storedDistance = storedDistance;
            }
        }
    }

    @Override
    protected void onDragEnd() {
        if (selectedPoint != null && useTicks > 2) {
            changed = true;
        }

        if (changed) {
            ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
        }

        selectedPoint = null;
        CutsceneRenderer.storedPoint = null;
        changed = false;
    }

    @Override
    protected void onDistanceChanged() {
        CutsceneRenderer.storedDistance = storedDistance;
    }

    @Override
    public boolean onScroll(double vertical) {
        if (CutsceneRenderer.storedPoint == null) return false;
        return super.onScroll(vertical);
    }

    @Override
    protected void reset() {
        super.reset();
        selectedPoint = null;
        changed = false;
        CutsceneRenderer.storedPoint = null;
    }

    public void addNodeAtEnd(Cutscene cutscene) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BezierPath path = cutscene.path;
        Vec3 newEnd = mc.player.getEyePosition();
        BezierPoint lastPoint = path.getPoints().getLast();

        BezierSpline newSpline = new BezierSpline(lastPoint, path, newEnd);
        path.splines.addLast(newSpline);
        path.updateLUT();

        cutscene.insertAnchorRotationAtEnd(new Vec2(mc.player.getXRot(), mc.player.getYRot()));

        ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
    }

    public void addNodeAtStart(Cutscene cutscene) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BezierPath path = cutscene.path;
        Vec3 newStart = mc.player.getEyePosition();
        BezierPoint firstPoint = path.getPoints().getFirst();

        BezierSpline newSpline = new BezierSpline(firstPoint, path, newStart);
        path.splines.addFirst(newSpline);
        path.updateLUT();

        cutscene.insertAnchorRotationAtStart(new Vec2(mc.player.getXRot(), mc.player.getYRot()));

        ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
    }

    public boolean removeLastNode(Cutscene cutscene) {
        BezierPath path = cutscene.path;
        if (path.isSinglePoint()) {
            var list = CutsceneRenderer.getCutscenes();
            if (!list.isEmpty()) list.remove(cutscene);
            ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
            return true;
        }
        BezierPoint lastPoint = path.getPoints().getLast();
        path.removePoint(lastPoint);
        cutscene.removeAnchorRotationAtEnd();
        ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
        return false;
    }

    public boolean removeFirstNode(Cutscene cutscene) {
        BezierPath path = cutscene.path;
        if (path.isSinglePoint()) {
            var list = CutsceneRenderer.getCutscenes();
            if (!list.isEmpty()) list.remove(cutscene);
            ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
            return true;
        }
        BezierPoint firstPoint = path.getPoints().getFirst();
        path.removePoint(firstPoint);
        cutscene.removeAnchorRotationAtStart();
        ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
        return false;
    }

    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Cutscene cutscene : CutsceneRenderer.getCutscenes()) {
            list.add(cutscene.toNbt());
        }
        tag.put("CutsceneList", list);
        return tag;
    }
}
