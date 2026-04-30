package de.luckymcdev.foundryengine.common.easing;

import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

/**
 * Represents a complete Bézier path composed of multiple splines.
 * Provides arc-length parameterization for constant-speed motion.
 */
public class BezierPath {
    private static final int LUT_RESOLUTION = 100;
    private static final double DISTANCE_EPSILON = 1e-6;
    private final double[] lutDistances = new double[LUT_RESOLUTION];
    public ArrayList<BezierSpline> splines = new ArrayList<>();
    private boolean lutValid = false;

    public BezierPath(Vec3 startPos) {
        this.splines.add(new BezierSpline(startPos, this));
        this.updateLUT();
    }

    /**
     * Creates a BezierPath from serialized NBT data.
     */
    public BezierPath(ListTag list) {
        this.init(list);
    }

    public boolean isSinglePoint() {
        return this.splines.size() == 1 && this.splines.getFirst().isSinglePoint();
    }

    public BezierPath withPlayerOrigin(Player player) {
        BezierPath newPath = new BezierPath(Vec3.ZERO);
        ArrayList<BezierSpline> tempSplines = new ArrayList<>(this.splines);

        if (tempSplines.getFirst().isSinglePoint()) {
            tempSplines.remove(0);
            tempSplines.addFirst(new BezierSpline(this.getPoints().getFirst(), newPath, player, true));
        } else {
            tempSplines.addFirst(new BezierSpline(this.getPoints().getFirst(), newPath, player));
        }

        newPath.splines = tempSplines;
        newPath.updateLUT();
        return newPath;
    }

    public BezierPath withPlayerEnd(Player player) {
        BezierPath newPath = new BezierPath(Vec3.ZERO);
        ArrayList<BezierSpline> tempSplines = new ArrayList<>(this.splines);

        if (tempSplines.getFirst().isSinglePoint()) {
            tempSplines.remove(0);
            tempSplines.add(new BezierSpline(this.getPoints().getLast(), newPath, player, false));
        } else {
            tempSplines.add(new BezierSpline(this.getPoints().getLast(), newPath, player));
        }

        newPath.splines = tempSplines;
        newPath.updateLUT();
        return newPath;
    }

    /**
     * Converts a distance parameter (0-1) to a time parameter using the LUT.
     * This enables constant-speed motion along the path.
     */
    private double distanceToT(double t) {
        if (t < 0.0 || t > 1.0) {
            return t;
        }

        double maxDistance = lutDistances[LUT_RESOLUTION - 1];
        if (maxDistance < DISTANCE_EPSILON) {
            return t;
        }

        double desiredDistance = maxDistance * t;
        int index = findClosestFloorIndex(desiredDistance);

        if (index >= LUT_RESOLUTION - 1) {
            return 1.0;
        }

        double between = (desiredDistance - lutDistances[index]) /
                (lutDistances[index + 1] - lutDistances[index]);
        return ((double) index + between) / (double) (LUT_RESOLUTION - 1);
    }

    private int findClosestFloorIndex(double distance) {
        for (int i = 1; i < LUT_RESOLUTION; i++) {
            if (lutDistances[i] >= distance) {
                return i - 1;
            }
        }
        return LUT_RESOLUTION - 2;
    }

    /**
     * Updates the Look-Up Table (LUT) for arc-length parameterization.
     * This should be called whenever any point on the path changes.
     */
    public void updateLUT() {
        lutDistances[0] = 0.0;
        Vec3 prevPoint = this.splines.get(0).lerp(0);
        double totalDist = 0;

        for (int i = 1; i < LUT_RESOLUTION; i++) {
            Vec3 nextPoint = lerp(i / (double) (LUT_RESOLUTION - 1));
            double distance = prevPoint.distanceTo(nextPoint);
            totalDist += distance;
            lutDistances[i] = totalDist;
            prevPoint = nextPoint;
        }

        lutValid = true;
    }

    /**
     * Returns the approximate arc-length distance (in blocks) along the path at the given time parameter (0..1)
     * of {@link #lerp(double)}. This is derived from the same LUT used for constant-speed motion.
     */
    public double getDistanceAtTime(double time) {
        if (!lutValid) updateLUT();

        if (time <= 0.0) return 0.0;
        if (time >= 1.0) return lutDistances[LUT_RESOLUTION - 1];

        double pos = time * (LUT_RESOLUTION - 1);
        int idx = (int) Math.floor(pos);
        double frac = pos - idx;

        if (idx >= LUT_RESOLUTION - 1) return lutDistances[LUT_RESOLUTION - 1];
        double a = lutDistances[idx];
        double b = lutDistances[idx + 1];
        return a + (b - a) * frac;
    }

    /**
     * Returns the normalized arc-length distance (0..1) along the path at the given time parameter (0..1)
     * of {@link #lerp(double)}.
     */
    public double getNormalizedDistanceAtTime(double time) {
        double total = getTotalLength();
        if (total < DISTANCE_EPSILON) return time;
        return getDistanceAtTime(time) / total;
    }

    /**
     * Linear interpolation across all splines using time parameter t (0-1).
     */
    private Vec3 lerp(double t) {
        if (t >= 1.0) {
            return this.splines.getLast().lerp(1.0);
        }

        t = t * splines.size();
        int splineIndex = (int) t;

        if (splineIndex >= splines.size()) {
            splineIndex = splines.size() - 1;
        }

        return this.splines.get(splineIndex).lerp(t % 1);
    }

    /**
     * Interpolates a position using arc-length parameterization.
     * This provides constant-speed motion along the path.
     *
     * @param t parameter from 0 to 1 representing distance along the path
     */
    public Vec3 lerpSpeedWeighted(double t) {
        return lerp(distanceToT(t));
    }

    /**
     * Gets all control points in the path.
     */
    public ArrayList<BezierPoint> getPoints() {
        ArrayList<BezierPoint> send = new ArrayList<>();
        for (BezierSpline spline : this.splines) {
            for (BezierPoint point : spline.points) {
                if (!send.contains(point)) {
                    send.add(point);
                }
            }
        }
        return send;
    }

    /**
     * Returns all non-tangent ("node") points in order from start to end.
     */
    public ArrayList<BezierPoint> getAnchorPoints() {
        ArrayList<BezierPoint> anchors = new ArrayList<>();

        if (this.splines.isEmpty()) return anchors;

        BezierSpline first = this.splines.getFirst();
        anchors.add(first.v1);

        if (!first.isSinglePoint()) {
            for (BezierSpline spline : this.splines) {
                anchors.add(spline.v2);
            }
        }

        return anchors;
    }

    /**
     * Returns the number of non-tangent ("node") points in this path.
     */
    public int getAnchorPointCount() {
        if (this.splines.isEmpty()) return 0;
        BezierSpline first = this.splines.getFirst();
        if (first.isSinglePoint()) return 1;
        return this.splines.size() + 1;
    }

    /**
     * Removes a point and its associated spline(s).
     */
    public void removePoint(BezierPoint point) {
        if (this.splines.size() == 1) {
            BezierSpline spline = this.splines.getFirst();
            if (spline.isSinglePoint()) {
                return;
            }
            if (point.isLast()) {
                this.splines.set(0, new BezierSpline(spline.v1.getPos(), this));
            } else {
                this.splines.set(0, new BezierSpline(spline.v2.getPos(), this));
            }
            return;
        }

        for (BezierSpline spline : point.getSplines()) {
            this.splines.remove(spline);
        }
        this.updateLUT();
    }

    /**
     * Checks if a point is at the start or end of the path.
     */
    public boolean isPointFirstOrLast(BezierPoint point) {
        ArrayList<BezierPoint> points = this.getPoints();
        return points.getFirst() == point || points.getLast() == point;
    }

    private void init(ListTag list) {
        ArrayList<Vec3> points = new ArrayList<>();
        int pointCount = list.size() / 3;

        for (int i = 0; i < pointCount; i++) {
            points.add(new Vec3(
                    list.getDouble(i * 3).orElseThrow(),
                    list.getDouble((i * 3) + 1).orElseThrow(),
                    list.getDouble((i * 3) + 2).orElseThrow()
            ));
        }

        ArrayList<BezierSpline> newSplines = new ArrayList<>();
        int splineCount = (points.size() - 1) / 3;

        for (int i = 0; i < splineCount; i++) {
            if (i == 0) {
                newSplines.add(new BezierSpline(points.get(0), points.get(1), points.get(2), points.get(3), this));
            } else {
                newSplines.add(new BezierSpline(newSplines.get(i - 1), points.get(1), points.get(2), points.get(3), this));
            }
            points.removeFirst();
            points.removeFirst();
            points.removeFirst();
        }

        if (splineCount == 0) {
            newSplines.add(new BezierSpline(points.getFirst(), this));
        }

        this.splines = newSplines;
        this.updateLUT();
    }

    /**
     * Serializes the path to NBT format.
     */
    public ListTag toNbt() {
        ListTag list = new ListTag();
        for (BezierPoint point : this.getPoints()) {
            list.add(DoubleTag.valueOf(point.getPos().x));
            list.add(DoubleTag.valueOf(point.getPos().y));
            list.add(DoubleTag.valueOf(point.getPos().z));
        }
        return list;
    }

    /**
     * Gets the total arc length of the path.
     */
    public double getTotalLength() {
        return lutDistances[LUT_RESOLUTION - 1];
    }

    /**
     * Invalidates the LUT cache, forcing recalculation on next use.
     */
    public void invalidateLUT() {
        lutValid = false;
    }
}
