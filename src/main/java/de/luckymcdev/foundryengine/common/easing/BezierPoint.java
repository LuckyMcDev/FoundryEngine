package de.luckymcdev.foundryengine.common.easing;

import de.luckymcdev.foundryengine.client.Client;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BezierPoint {
    private static final double size = 0.05;
    private final BezierPath path;
    private Vec3 pos;
    private final boolean isTangent;

    public BezierPoint(BezierSpline spline, Vec3 pos, boolean isTangent) {
        this.pos = pos;
        this.path = spline.path;
        this.isTangent = isTangent;
    }

    public boolean isSinglePoint() {
        return this.path.isSinglePoint();
    }

    public List<BezierPoint> getTangents() {
        ArrayList<BezierPoint> send = new ArrayList<>();
        ArrayList<BezierPoint> points = this.path.getPoints();
        if (this.isTangent)
            return send;
        if (isLast())
            send.add(points.get(points.size() - 2));
        else
            send.add(points.get(this.getIndex() + 1));
        if (!isFirst())
            send.add(points.get(this.getIndex() - 1));
        return send;
    }

    private int getIndex() {
        return this.path.getPoints().indexOf(this);
    }

    public ArrayList<BezierSpline> getSplines() {
        if (this.isTangent)
            return this.getRoot().getSplines();
        ArrayList<BezierSpline> send = new ArrayList<>();
        int index = this.getIndex();
        int splineIndex = (index - 1) / 3;
        send.add(this.path.splines.get(splineIndex));
        if (!this.isEnd())
            send.add(this.path.splines.get(splineIndex + 1));
        return send;
    }

    public Vec3 getPos() {
        return this.pos;
    }

    public void setPos(Vec3 newPos) {
        if (!this.isSinglePoint()) {
            if (this.isTangent()) {
                if (!this.path.isPointFirstOrLast(this.getRoot()))
                    this.getMirrorTangent().pos = this.getRoot().getPos().subtract(newPos.subtract(this.getRoot().getPos()));
            } else {
                List<BezierPoint> tangents = this.getTangents();
                Vec3 offset = tangents.getFirst().getPos().subtract(this.getPos());
                tangents.get(0).pos = newPos.add(offset);
                if (!this.isEnd())
                    tangents.get(1).pos = newPos.subtract(offset);
            }
        }
        this.pos = newPos;
        for (BezierSpline spline : this.getSplines())
            spline.updateCoeffs();
        this.path.updateLUT();
    }

    public BezierPath getPath() {
        return this.path;
    }

    public void render() {
        if (this.path == null)
            return;
        if (Client.getPlayer().getEyePosition().distanceTo(this.pos) < 0.1)
            return;
        int color = this.getColor();
        if (this.isHovered(Client.getPlayer()))
            color = 0xFFFFFFFF;
        else if (this.path.isSinglePoint())
            color = 0xFFFF00FF;
        if (this.isTangent())
            Gizmos.line(this.pos, this.getRoot().pos, scaleAlpha(this.getColor(), 0.5f), 5);
        Gizmos.cuboid(new AABB(this.pos.add(new Vec3(size, size, size)), this.pos.subtract(new Vec3(size, size, size))), GizmoStyle.stroke(color), true);
    }

    private int getColor() {
        int red = 0xFFFFFF00;
        int blue = 0xFF0000FF;
        if (this.isTangent) {
            if (this.getRoot().isFirst())
                return red;
            if (this.getRoot().isLast())
                return blue;
        } else {
            if (this.isFirst())
                return red;
            if (this.isLast())
                return blue;
        }
        return 0xFF808080;
    }

    public boolean isEnd() {
        return !this.isTangent && (this.isFirst() || this.isLast());
    }

    public boolean isFirst() {
        ArrayList<BezierPoint> points = this.path.getPoints();
        return points.getFirst() == this;
    }

    public boolean isLast() {
        ArrayList<BezierPoint> points = this.path.getPoints();
        return points.getLast() == this;
    }

    public boolean isHovered(LivingEntity entity) {
        double x = this.pos.x - entity.getEyePosition().x;
        double y = this.pos.y - entity.getEyePosition().y;
        double z = this.pos.z - entity.getEyePosition().z;
        double rotation = Math.toDegrees(Math.atan2(x, z)) * -1;
        double playerRot = Mth.wrapDegrees(entity.getYRot());

        double playerRot2 = Mth.wrapDegrees(entity.getXRot());
        double hypot = Math.sqrt(Math.pow(x, 2) + Math.pow(z, 2));
        double rotation2 = Math.toDegrees(Math.atan2(y, hypot)) * -1;

        return Math.abs(playerRot - rotation) < 1 && Math.abs(playerRot2 - rotation2) < 1;
    }

    public BezierPoint getRoot() {
        if (!this.isTangent())
            return null;
        ArrayList<BezierPoint> points = this.path.getPoints();
        int index = this.getIndex();
        int alter = index + (2 * (index % 3) - 3);
        return points.get(alter);
    }

    public BezierPoint getMirrorTangent() {
        if (!this.isTangent() || this.path.isPointFirstOrLast(this.getRoot()))
            return null;
        ArrayList<BezierPoint> points = this.path.getPoints();
        int index = this.getIndex();
        int alter = index + (-2 * ((index + 1) % 3) + 2);
        return points.get(alter);
    }

    public boolean isTangent() {
        return this.isTangent;
    }

    public boolean canBeModified() {
        return !this.isTangent;
    }

    private int scaleAlpha(int color, float alpha) {
        int a = (int) ((color >> 24 & 0xFF) * alpha);
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}