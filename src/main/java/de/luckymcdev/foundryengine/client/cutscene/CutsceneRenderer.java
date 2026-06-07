package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.client.render.HandleRenderer;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.easing.BezierPoint;
import de.luckymcdev.foundryengine.common.easing.BezierSpline;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
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
    private static final double POINT_SIZE = 0.125;
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
        var anchors = cutscene.path.getAnchorPoints();
        var rots = cutscene.getAnchorRotations();
        var holdTicks = cutscene.getAnchorHoldTicks();
        int count = Math.min(Math.min(anchors.size(), rots.size()), holdTicks.size());

        for (int i = 0; i < count; i++) {
            Vec3 pos = anchors.get(i).getPos();
            Vec2 rot = rots.get(i);

            Color base = cutscene.getColor();
            Color color;
            if (i == 0 || i == count - 1) color = new Color(base.r(), base.g(), base.b(), 0.627f);
            else color = new Color(base.r(), base.g(), base.b(), 0.439f);

            renderPointsRelative(pos, i == 0 || i == count - 1 ? ENDPOINT_RELATIVE_POINTS : NODE_RELATIVE_POINTS, rot, color);

            // Draw a larger ring around anchors with hold ticks
            int hold = holdTicks.get(i);
            if (hold > 0) {
                Color holdColor = new Color(0xFFFFCC00);
                HandleRenderer.renderHandle(pos, POINT_SIZE * 2.5, holdColor);
            }
        }

        renderBezierPath(cutscene);
    }

    private static void renderBezierPath(Cutscene cutscene) {
        Color lineColor = cutscene.getColor();
        for (BezierSpline spline : cutscene.path.splines) {
            double delta = 0.01;
            for (double d = 0.00; d < 1; d += delta) {
                Vec3 pos1 = spline.lerp(d);
                Vec3 pos2 = spline.lerp(d + delta);
                HandleRenderer.renderLine(pos1, pos2, lineColor, 5);
            }
        }

        LivingEntity viewer = Minecraft.getInstance().player;
        for (BezierPoint point : cutscene.path.getPoints()) {
            renderBezierPoint(viewer, point);
        }
    }

    private static void renderBezierPoint(LivingEntity viewer, BezierPoint point) {
        if (viewer.getEyePosition().distanceTo(point.getPos()) < 0.1) return;

        Color color = getBezierPointColor(point);
        if (point.isHovered(viewer)) color = Color.WHITE;
        if (point.getPath().isSinglePoint()) color = new Color(0xFFFF00FF);

        if (point.isTangent()) {
            BezierPoint root = point.getRoot();
            if (root != null) {
                HandleRenderer.renderLine(point.getPos(), root.getPos(), scaleAlpha(getBezierPointColor(point), 0.5f), 5);
            }
        }

        HandleRenderer.renderHandle(point.getPos(), POINT_SIZE, color);
    }

    private static Color getBezierPointColor(BezierPoint point) {
        Color red = new Color(0xFFFFFF00);
        Color blue = new Color(0xFF0000FF);
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
        return new Color(0xFF808080);
    }

    private static Color scaleAlpha(Color color, float alpha) {
        return new Color(color.r(), color.g(), color.b(), color.a() * alpha);
    }

    private static void renderPointsRelative(Vec3 origin, List<Vec3> relativePoints, Vec2 rot, Color color) {
        ArrayList<Vec3> translatedPoints = new ArrayList<>();
        for (Vec3 point : relativePoints) {
            translatedPoints.add(rotatePointRelative(origin, point, rot));
        }
        for (Vec3 point : translatedPoints) {
            HandleRenderer.renderLine(origin, point, color, 3);
        }
        for (int i = 0; i < translatedPoints.size() - 1; i++) {
            HandleRenderer.renderLine(translatedPoints.get(i), translatedPoints.get(i + 1), color, 3);
        }
        HandleRenderer.renderLine(translatedPoints.getLast(), translatedPoints.getFirst(), color, 3);
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
