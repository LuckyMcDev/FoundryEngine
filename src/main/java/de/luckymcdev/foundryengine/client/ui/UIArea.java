package de.luckymcdev.foundryengine.client.ui;

import net.minecraft.world.phys.Vec2;

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

	public UIArea getSubArea(UIVec pos, UIVec size, Vec2 anchorPoint) {
		int anchorX = this.x + (int) (this.width * pos.scaleX) + pos.offsetX;
		int anchorY = this.y + (int) (this.height * pos.scaleY) + pos.offsetY;
		int sizeX = (int) (this.width * size.scaleX) + size.offsetX;
		int sizeY = (int) (this.height * size.scaleY) + size.offsetY;
		int posX = anchorX - (int) (sizeX * anchorPoint.x);
		int posY = anchorY - (int) (sizeY * anchorPoint.y);
		return new UIArea(posX, posY, sizeX, sizeY);
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
	public String toString() {
		return String.format("(%1$d %2$d, %3$d x %4$d)", this.x, this.y, this.width, this.height);
	}
}
