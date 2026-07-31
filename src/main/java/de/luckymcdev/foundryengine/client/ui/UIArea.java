package de.luckymcdev.foundryengine.client.ui;

import java.util.Objects;

public class UIArea {
	public int x;
	public int y;
	public int width;
	public int height;

	public UIArea(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = Math.max(0, width);
		this.height = Math.max(0, height);
	}

	public UIArea shrink(int amount) {
		return new UIArea(this.x + amount, this.y + amount, this.width - amount * 2, this.height - amount * 2);
	}

	public UIArea lerp(UIArea other, float delta) {
		int lerpedX = this.x + (int) (delta * (other.x - this.x));
		int lerpedY = this.y + (int) (delta * (other.y - this.y));
		int lerpedW = this.width + (int) (delta * (other.width - this.width));
		int lerpedH = this.height + (int) (delta * (other.height - this.height));
		return new UIArea(lerpedX, lerpedY, lerpedW, lerpedH);
	}

	public boolean isInArea(double x, double y) {
		return x > this.x && y > this.y && x < this.x + this.width && y < this.y + this.height;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof UIArea other)) {
			return false;
		}
		return this.x == other.x && this.y == other.y && this.width == other.width && this.height == other.height;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.x, this.y, this.width, this.height);
	}

	@Override
	public String toString() {
		return String.format("(%1$d %2$d, %3$d x %4$d)", this.x, this.y, this.width, this.height);
	}
}
