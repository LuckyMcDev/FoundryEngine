package de.luckymcdev.foundryengine.common.easing;

import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A control point on a Bézier path, which may be an anchor or tangent point.
 */
public class BezierPoint {
    private final BezierPath path;
    private final boolean isTangent;
    private Vec3 pos;

    /**
     * Creates a new BezierPoint belonging to the given spline.
     */
    public BezierPoint(BezierSpline spline, Vec3 pos, boolean isTangent) {
        this.pos = pos;
        this.path = spline.path;
        this.isTangent = isTangent;
    }

    /**
     * Returns true if the owning path is a single point.
     */
    public boolean isSinglePoint() {
        return this.path.isSinglePoint();
    }

    /**
     * Returns the tangent points connected to this point.
     */
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

    /**
     * Returns the splines that this point belongs to.
     */
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

    /**
     * Returns the position of this point.
     */
    public Vec3 getPos() {
        return this.pos;
    }

    /**
     * Sets the position and updates connected tangents and spline coefficients.
     */
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

    /**
     * Returns the BezierPath this point belongs to.
     */
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

    /**
     * Returns true if this is an endpoint (first or last anchor).
     */
    public boolean isEnd() {
        return !this.isTangent && (this.isFirst() || this.isLast());
    }

    /**
     * Returns true if this is the first point in the path.
     */
    public boolean isFirst() {
        ArrayList<BezierPoint> points = this.path.getPoints();
        return points.getFirst() == this;
    }

    /**
     * Returns true if this is the last point in the path.
     */
    public boolean isLast() {
        ArrayList<BezierPoint> points = this.path.getPoints();
        return points.getLast() == this;
    }

    /**
     * Returns the anchor point this tangent belongs to (null if this is an anchor).
     */
    public BezierPoint getRoot() {
        if (!this.isTangent())
            return null;
        ArrayList<BezierPoint> points = this.path.getPoints();
        int index = this.getIndex();
        int alter = index + (2 * (index % 3) - 3);
        return points.get(alter);
    }

    /**
     * Returns the mirrored tangent point on the opposite side of the anchor.
     */
    public BezierPoint getMirrorTangent() {
        if (!this.isTangent() || this.path.isPointFirstOrLast(this.getRoot()))
            return null;
        ArrayList<BezierPoint> points = this.path.getPoints();
        int index = this.getIndex();
        int alter = index + (-2 * ((index + 1) % 3) + 2);
        return points.get(alter);
    }

    /**
     * Returns true if this point is a tangent control point.
     */
    public boolean isTangent() {
        return this.isTangent;
    }

    /**
     * Returns true if this point can be freely moved (not a tangent).
     */
    public boolean canBeModified() {
        return !this.isTangent;
    }
}
