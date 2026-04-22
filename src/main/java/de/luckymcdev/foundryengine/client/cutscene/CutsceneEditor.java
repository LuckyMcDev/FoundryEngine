package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.common.cutscene.CutsceneItems;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.network.CutscenePacket;
import de.luckymcdev.foundryengine.common.easing.BezierPath;
import de.luckymcdev.foundryengine.common.easing.BezierPoint;
import de.luckymcdev.foundryengine.common.easing.BezierSpline;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class CutsceneEditor {
    private static boolean wasUsing = false;
    private static int useTicks = 0;
    private static BezierPoint selectedPoint;
    private static boolean changed = false;

    public static void clientTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || CutsceneItems.EDITOR_ITEM == null) {
            reset();
            return;
        }

        boolean holding = mc.player.getMainHandItem().getItem() == CutsceneItems.EDITOR_ITEM
                || mc.player.getOffhandItem().getItem() == CutsceneItems.EDITOR_ITEM;

        if (!holding) {
            reset();
            return;
        }

        boolean using = mc.player.isUsingItem() && mc.player.getUseItem().getItem() == CutsceneItems.EDITOR_ITEM;

        if (using && !wasUsing) {
            wasUsing = true;
            useTicks = 0;
            selectedPoint = null;
            changed = false;
            CutsceneRenderer.storedPoint = null;
            CutsceneRenderer.storedDistance = 0;
        }

        if (using) {
            useTicks++;

            if (selectedPoint == null) {
                selectedPoint = pickHovered(mc.player);
                if (selectedPoint != null) {
                    CutsceneRenderer.storedPoint = selectedPoint;
                    CutsceneRenderer.storedDistance = selectedPoint.getPos().distanceTo(mc.player.getEyePosition());
                }
            }
        }

        if (!using && wasUsing) {
            wasUsing = false;

            if (selectedPoint != null && useTicks > 2) {
                changed = true;
            }

            if (changed) {
                ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
            }

            selectedPoint = null;
            CutsceneRenderer.storedPoint = null;
            useTicks = 0;
            changed = false;
        }
    }

    public static void addNodeAtEnd(Cutscene cutscene) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BezierPath path = cutscene.path;
        Vec3 newEnd = mc.player.getEyePosition();
        BezierPoint lastPoint = path.getPoints().getLast();

        BezierSpline newSpline = new BezierSpline(lastPoint, path, newEnd);
        path.splines.addLast(newSpline);
        path.updateLUT();

        ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
    }

    public static void addNodeAtStart(Cutscene cutscene) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        BezierPath path = cutscene.path;
        Vec3 newStart = mc.player.getEyePosition();
        BezierPoint firstPoint = path.getPoints().getFirst();

        BezierSpline newSpline = new BezierSpline(firstPoint, path, newStart);
        path.splines.addFirst(newSpline);
        path.updateLUT();

        ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
    }

    public static boolean removeLastNode(Cutscene cutscene) {
        BezierPath path = cutscene.path;
        if (path.isSinglePoint()) {
            CutsceneRenderer.cutscenes.remove(cutscene);
            ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
            return true;
        }
        BezierPoint lastPoint = path.getPoints().getLast();
        path.removePoint(lastPoint);
        ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
        return false;
    }

    public static boolean removeFirstNode(Cutscene cutscene) {
        BezierPath path = cutscene.path;
        if (path.isSinglePoint()) {
            CutsceneRenderer.cutscenes.remove(cutscene);
            ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
            return true;
        }
        BezierPoint firstPoint = path.getPoints().getFirst();
        path.removePoint(firstPoint);
        ClientPacketDistributor.sendToServer(new CutscenePacket(toNbt()));
        return false;
    }

    private static BezierPoint pickHovered(LivingEntity user) {
        for (Cutscene cutscene : CutsceneRenderer.cutscenes) {
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

    public static void updateStoredDistance(double delta) {
        CutsceneRenderer.storedDistance = Math.max(CutsceneRenderer.storedDistance + (delta * 0.25), 0);
    }

    public static CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (Cutscene cutscene : CutsceneRenderer.cutscenes) {
            list.add(cutscene.toNbt());
        }
        tag.put("CutsceneList", list);
        return tag;
    }

    private static void reset() {
        wasUsing = false;
        useTicks = 0;
        selectedPoint = null;
        changed = false;
        CutsceneRenderer.storedPoint = null;
    }
}