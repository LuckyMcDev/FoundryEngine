package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.CutsceneItems;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.easing.BezierPoint;
import de.luckymcdev.foundryengine.common.easing.BezierSpline;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CutsceneRenderer {
    private static final double ENDPOINT_DIST = 0.5;
    private static final double ENDPOINT_W = 0.9;
    private static final double ENDPOINT_H = 0.475;
    private static final List<Vec3> ENDPOINT_RELATIVE_POINTS = List.of(
            new Vec3(ENDPOINT_DIST, ENDPOINT_H, ENDPOINT_W),
            new Vec3(ENDPOINT_DIST, ENDPOINT_H, -ENDPOINT_W),
            new Vec3(ENDPOINT_DIST, -ENDPOINT_H, -ENDPOINT_W),
            new Vec3(ENDPOINT_DIST, -ENDPOINT_H, ENDPOINT_W)
    );
    private static final List<Vec3> NODE_RELATIVE_POINTS = ENDPOINT_RELATIVE_POINTS.stream()
            .map(v -> v.scale(0.55))
            .toList();
    private static final double POINT_SIZE = 0.05;
    public static BezierPoint storedPoint;
    public static double storedDistance;

    public static List<Cutscene> getCutscenes() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return List.of();
        return Common.getCutsceneManager().getCutscenes(mc.level.dimension());
    }

    public static Cutscene findByName(String name) {
        for (Cutscene c : getCutscenes()) {
            if (c.getName().equals(name)) return c;
        }
        return null;
    }

    public static void render() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;
        if (CutsceneItems.EDITOR_ITEM == null) return;

        boolean holding = mc.player.getMainHandItem().getItem() == CutsceneItems.EDITOR_ITEM
                || mc.player.getOffhandItem().getItem() == CutsceneItems.EDITOR_ITEM;
        if (!holding) return;

        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);

        for (Cutscene cutscene : getCutscenes()) {
            renderCutscene(cutscene);
        }

        if (storedPoint != null) {
            mc.player.sendOverlayMessage(Component.literal(
                    "Scroll to push/pull points. Current distance: " + String.format("%.2f", storedDistance)
            ));

            Vec3 eye = mc.player.getEyePosition(partial);
            Vec3 look = mc.player.getViewVector(partial);
            Vec3 newPos = eye.add(look.scale(storedDistance));
            storedPoint.setPos(newPos);

            for (Cutscene cutscene : getCutscenes()) {
                for (BezierPoint near : cutscene.path.getPoints()) {
                    if (near == storedPoint) continue;
                    if (near.getPos().distanceTo(storedPoint.getPos()) <= 0.1) {
                        storedPoint.setPos(near.getPos());
                        break;
                    }
                }
            }

            if (!storedPoint.isTangent()) {
                for (Cutscene cutscene : getCutscenes()) {
                    if (cutscene.path == storedPoint.getPath()) {
                        Vec2 playerRot = new Vec2(mc.player.getXRot(), mc.player.getYRot());
                        cutscene.setRotationForAnchorPoint(storedPoint, playerRot);
                        break;
                    }
                }
            }
        }
    }

    private static void renderCutscene(Cutscene cutscene) {
        // Draw orientation frames at every anchor (node) so per-node rotations are visible in-editor.
        var anchors = cutscene.path.getAnchorPoints();
        var rots = cutscene.getAnchorRotations();
        int count = Math.min(anchors.size(), rots.size());

        for (int i = 0; i < count; i++) {
            Vec3 pos = anchors.get(i).getPos();
            Vec2 rot = rots.get(i);

            boolean endpoint = (i == 0 || i == count - 1);
            int base = cutscene.getColorArgb();
            int color;
            if (i == 0) color = (0xA0000000) | (base & 0x00FFFFFF);
            else if (i == count - 1) color = (0xA0000000) | (base & 0x00FFFFFF);
            else color = (0x70000000) | (base & 0x00FFFFFF);

            renderPointsRelative(pos, endpoint ? ENDPOINT_RELATIVE_POINTS : NODE_RELATIVE_POINTS, rot, color);
        }

        renderBezierPath(cutscene);
    }

    private static void renderBezierPath(Cutscene cutscene) {
        int lineColor = cutscene.getColorArgb();
        for (BezierSpline spline : cutscene.path.splines) {
            double delta = 0.01;
            for (double d = 0.00; d < 1; d += delta) {
                Vec3 pos1 = spline.lerp(d);
                Vec3 pos2 = spline.lerp(d + delta);
                Gizmos.line(pos1, pos2, lineColor, 5);
            }
        }

        LivingEntity viewer = Minecraft.getInstance().player;
        for (BezierPoint point : cutscene.path.getPoints()) {
            renderBezierPoint(viewer, point);
        }
    }

    private static void renderBezierPoint(LivingEntity viewer, BezierPoint point) {
        if (viewer.getEyePosition().distanceTo(point.getPos()) < 0.1) return;

        int color = getBezierPointColor(point);
        if (point.isHovered(viewer)) color = 0xFFFFFFFF;
        if (point.getPath().isSinglePoint()) color = 0xFFFF00FF;

        if (point.isTangent()) {
            BezierPoint root = point.getRoot();
            if (root != null) {
                Gizmos.line(point.getPos(), root.getPos(), scaleAlpha(getBezierPointColor(point), 0.5f), 5);
            }
        }

        AABB bb = new AABB(
                point.getPos().add(new Vec3(POINT_SIZE, POINT_SIZE, POINT_SIZE)),
                point.getPos().subtract(new Vec3(POINT_SIZE, POINT_SIZE, POINT_SIZE))
        );
        Gizmos.cuboid(bb, GizmoStyle.stroke(color), true);
    }

    private static int getBezierPointColor(BezierPoint point) {
        int red = 0xFFFFFF00;
        int blue = 0xFF0000FF;
        if (point.isTangent()) {
            BezierPoint root = point.getRoot();
            if (root != null) {
                if (root.isFirst()) return red;
                if (root.isLast()) return blue;
            }
        } else {
            if (point.isFirst()) return red;
            if (point.isLast()) return blue;
        }
        return 0xFF808080;
    }

    private static int scaleAlpha(int color, float alpha) {
        int a = (int) ((color >> 24 & 0xFF) * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void renderPointsRelative(Vec3 origin, List<Vec3> relativePoints, Vec2 rot, int color) {
        ArrayList<Vec3> translatedPoints = new ArrayList<>();
        for (Vec3 point : relativePoints) {
            translatedPoints.add(rotatePointRelative(origin, point, rot));
        }
        for (Vec3 point : translatedPoints) {
            Gizmos.line(origin, point, color, 3);
        }
        for (int i = 0; i < translatedPoints.size() - 1; i++) {
            Gizmos.line(translatedPoints.get(i), translatedPoints.get(i + 1), color, 3);
        }
        Gizmos.line(translatedPoints.getLast(), translatedPoints.getFirst(), color, 3);
    }

    private static Vec3 rotatePointRelative(Vec3 origin, Vec3 relativeCoordinate, Vec2 rot) {
        double yawRadians = Math.toRadians(rot.y) + Math.PI * 0.5;
        double pitchRadians = Math.toRadians(rot.x);

        double x = relativeCoordinate.x * Math.cos(yawRadians) * Math.cos(pitchRadians) + origin.x;
        double y = relativeCoordinate.x * Math.sin(pitchRadians) * -1 + origin.y;
        double z = relativeCoordinate.x * Math.sin(yawRadians) * Math.cos(pitchRadians) + origin.z;

        x -= relativeCoordinate.z * Math.sin(yawRadians);
        z += relativeCoordinate.z * Math.cos(yawRadians);

        x += relativeCoordinate.y * Math.sin(pitchRadians) * Math.cos(yawRadians);
        y += relativeCoordinate.y * Math.cos(pitchRadians);
        z += relativeCoordinate.y * Math.sin(pitchRadians) * Math.sin(yawRadians);

        return new Vec3(x, y, z);
    }
}
