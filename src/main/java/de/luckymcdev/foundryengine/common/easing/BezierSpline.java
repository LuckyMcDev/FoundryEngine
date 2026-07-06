package de.luckymcdev.foundryengine.common.easing;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a cubic Bézier spline with control points and coefficients.
 * Provides interpolation and rendering functionality.
 */
public class BezierSpline {
	public BezierPoint v1;
	public BezierPoint v1_tangent;
	public BezierPoint v2;
	public BezierPoint v2_tangent;
	public BezierPath path;
	public List<BezierPoint> points = new ArrayList<>();

	private Vec3 coefficient1;
	private Vec3 coefficient2;
	private Vec3 coefficient3;
	private boolean coefficientsValid = false;

	public BezierSpline(Vec3 v1, Vec3 v1_tangent, Vec3 v2_tangent, Vec3 v2, BezierPath path) {
		this.path = path;
		buildInit(v1, v1_tangent, v2_tangent, v2);
	}

	public BezierSpline(BezierSpline prevSpline, Vec3 v1_tangent, Vec3 v2_tangent, Vec3 v2, BezierPath path) {
		this.path = path;
		buildInit(prevSpline, v1_tangent, v2_tangent, v2);
	}

	public BezierSpline(Vec3 initialPos, BezierPath owner) {
		this.path = owner;
		this.v1 = new BezierPoint(this, initialPos, false);
		points = List.of(v1);
		updateCoeffs();
	}

	public BezierSpline(BezierPoint point, BezierPath owner, Vec3 newEnd) {
		this.path = owner;
		if (point.isSinglePoint()) {
			this.v1 = point;
			this.v2 = new BezierPoint(this, newEnd, false);
			Vec3 offset = v1.getPos().subtract(v2.getPos()).scale(0.5);
			this.v1_tangent = new BezierPoint(this, v1.getPos().subtract(offset), true);
			this.v2_tangent = new BezierPoint(this, v1.getPos().subtract(offset), true);

		} else if (point.isFirst()) {
			this.v2 = point;
			Vec3 offset = v2.getTangents().getFirst().getPos().subtract(v2.getPos());
			this.v2_tangent = new BezierPoint(this, v2.getPos().subtract(offset), true);
			this.v1 = new BezierPoint(this, newEnd, false);
			this.v1_tangent = new BezierPoint(this, v1.getPos().subtract(offset), true);
		} else {
			this.v1 = point;
			Vec3 offset = v1.getTangents().getFirst().getPos().subtract(v1.getPos());
			this.v1_tangent = new BezierPoint(this, v1.getPos().subtract(offset), true);
			this.v2 = new BezierPoint(this, newEnd, false);
			this.v2_tangent = new BezierPoint(this, v2.getPos().subtract(offset), true);
		}
		points = List.of(v1, v1_tangent, v2_tangent, v2);
		updateCoeffs();
		this.path.updateLUT();
	}

	public BezierSpline(BezierPoint point, BezierPath owner, Player player, boolean startAtPlayer) {
		this.path = owner;
		if (startAtPlayer) {
			this.v2 = new BezierPoint(this, point.getPos(), false);
			this.v2_tangent = new BezierPoint(this, point.getPos().subtract(player.getEyePosition()).scale(0.5).add(player.getEyePosition()), true);
			this.v1_tangent = new BezierPoint(this, point.getPos().subtract(player.getEyePosition()).scale(0.5).add(player.getEyePosition()), true);
			this.v1 = new BezierPoint(this, player.getEyePosition(), false);
		} else {
			this.v1 = new BezierPoint(this, point.getPos(), false);
			this.v2_tangent = new BezierPoint(this, point.getPos().subtract(player.getEyePosition()).scale(0.5).add(player.getEyePosition()), true);
			this.v1_tangent = new BezierPoint(this, point.getPos().subtract(player.getEyePosition()).scale(0.5).add(player.getEyePosition()), true);
			this.v2 = new BezierPoint(this, player.getEyePosition(), false);
		}
		points = List.of(v1, v1_tangent, v2_tangent, v2);
		updateCoeffs();
	}

	public BezierSpline(BezierPoint point, BezierPath owner, Player player) {
		this.path = owner;
		if (point.isFirst()) {
			this.v2 = point;
			Vec3 offset = v2.getPos().subtract(v2.getTangents().getFirst().getPos().subtract(v2.getPos()));
			this.v2_tangent = new BezierPoint(this, offset, true);
			this.v1_tangent = new BezierPoint(this, offset, true);
			this.v1 = new BezierPoint(this, player.getEyePosition(), false);
		} else {
			this.v1 = point;
			Vec3 offset = v1.getPos().subtract(v1.getTangents().getFirst().getPos().subtract(v1.getPos()));
			this.v1_tangent = new BezierPoint(this, offset, true);
			this.v2_tangent = new BezierPoint(this, offset, true);
			this.v2 = new BezierPoint(this, player.getEyePosition(), false);
		}
		points = List.of(v1, v1_tangent, v2_tangent, v2);
		updateCoeffs();
	}

	/**
	 * Returns true if this spline consists of a single point.
	 */
	public boolean isSinglePoint() {
		return this.points.size() == 1;
	}

	/**
	 * Returns true if this spline contains the given point.
	 */
	public boolean containsPoint(BezierPoint point) {
		return this.points.contains(point);
	}

	private void buildInit(Vec3 v1, Vec3 v1_tangent, Vec3 v2_tangent, Vec3 v2) {
		this.v1 = new BezierPoint(this, v1, false);
		this.v1_tangent = new BezierPoint(this, v1_tangent, true);
		this.v2_tangent = new BezierPoint(this, v2_tangent, true);
		this.v2 = new BezierPoint(this, v2, false);
		points = List.of(this.v1, this.v1_tangent, this.v2_tangent, this.v2);
		updateCoeffs();
	}

	private void buildInit(BezierSpline prevSpline, Vec3 v1_tangent, Vec3 v2_tangent, Vec3 v2) {
		this.v1 = prevSpline.v2;
		this.v1_tangent = new BezierPoint(this, v1_tangent, true);
		this.v2_tangent = new BezierPoint(this, v2_tangent, true);
		this.v2 = new BezierPoint(this, v2, false);
		points = List.of(this.v1, this.v1_tangent, this.v2_tangent, this.v2);
		updateCoeffs();
	}

	/**
	 * Updates the cubic Bézier coefficients. Only recalculates if the spline is not a single point.
	 */
	public void updateCoeffs() {
		if (this.isSinglePoint()) {
			coefficientsValid = false;
			return;
		}
		coefficient1 = v1.getPos().scale(-1.0)
			.add(v1_tangent.getPos().scale(3.0))
			.add(v2_tangent.getPos().scale(-3.0))
			.add(v2.getPos());
		coefficient2 = v1.getPos().scale(3.0)
			.add(v1_tangent.getPos().scale(-6.0))
			.add(v2_tangent.getPos().scale(3.0));
		coefficient3 = v1.getPos().scale(-3.0).add(v1_tangent.getPos().scale(3.0));
		coefficientsValid = true;
	}

	/**
	 * Interpolates a position on the spline at parameter t (0 to 1).
	 * Uses cubic Bézier curve formula: B(t) = (1-t)³P0 + 3(1-t)²tP1 + 3(1-t)t²P2 + t³P3
	 */
	public Vec3 lerp(double t) {
		if (this.isSinglePoint()) {
			return this.points.getFirst().getPos();
		}

		if (!coefficientsValid) {
			updateCoeffs();
		}

		double t2 = t * t;
		double t3 = t2 * t;

		return coefficient1.scale(t3)
			.add(coefficient2.scale(t2))
			.add(coefficient3.scale(t))
			.add(v1.getPos());
	}

	/**
	 * Gets the derivative (tangent) of the spline at parameter t.
	 * Used for calculating arc length and velocity.
	 */
	public Vec3 derivative(double t) {
		if (this.isSinglePoint()) {
			return Vec3.ZERO;
		}

		double t2 = t * t;

		return coefficient1.scale(3.0 * t2)
			.add(coefficient2.scale(2.0 * t))
			.add(coefficient3);
	}

	/**
	 * Calculates the approximate length of the spline segment.
	 */
	/**
	 * Returns the approximate length of this spline segment.
	 */
	public double getLength() {
		double length = 0;
		double step = 0.01;
		Vec3 prev = lerp(0);

		for (double t = step; t <= 1.0; t += step) {
			Vec3 current = lerp(t);
			length += prev.distanceTo(current);
			prev = current;
		}

		return length;
	}
}
