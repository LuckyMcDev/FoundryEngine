package de.luckymcdev.foundryengine.common.easing;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BezierPoint {
    private final BezierPath path;
    private final boolean isTangent;
    private Vec3 pos;

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

    private Color getColor() {
        Color red = new Color(0xFFFFFF00);
        Color blue = new Color(0xFF0000FF);
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
        return new Color(0xFF808080);
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
}
