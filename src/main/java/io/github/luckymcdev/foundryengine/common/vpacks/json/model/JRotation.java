package io.github.luckymcdev.foundryengine.common.vpacks.json.model;

import net.minecraft.core.Direction;

public class JRotation {
    private final float[] origin = new float[3];
    private final char axis;
    private Float angle;
    private Boolean rescale;

    public JRotation(Direction.Axis axis) {
        this.axis = axis.getName().charAt(0);
    }

    public JRotation origin(float x, float y, float z) {
        this.origin[0] = x;
        this.origin[1] = y;
        this.origin[2] = z;
        return this;
    }

    public JRotation angle(float angle) {
        this.angle = angle;
        return this;
    }

    public JRotation rescale() {
        this.rescale = true;
        return this;
    }

    @Override
    public JRotation clone() {
        try {
            return (JRotation) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
