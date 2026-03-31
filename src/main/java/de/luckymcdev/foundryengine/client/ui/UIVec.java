package de.luckymcdev.foundryengine.client.ui;

public class UIVec {
    public double scaleX;
    public double scaleY;
    public int offsetX;
    public int offsetY;

    public UIVec(double scaleX, double scaleY, int offsetX, int offsetY) {
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public static UIVec scaled(double scaleX, double scaleY) {
        return new UIVec(scaleX, scaleY, 0, 0);
    }

    public static UIVec offset(int offsetX, int offsetY) {
        return new UIVec(0, 0, offsetX, offsetY);
    }
}
