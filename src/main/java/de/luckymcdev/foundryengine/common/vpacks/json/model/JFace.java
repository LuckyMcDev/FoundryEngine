package de.luckymcdev.foundryengine.common.vpacks.json.model;

import com.google.gson.annotations.SerializedName;
import net.minecraft.core.Direction;

public class JFace implements Cloneable {
    private final float[] uv = new float[4];
    private final String texture;
    private String cullface;
    private Integer rotation;
    @SerializedName("tintindex")
    private Integer tintIndex;

    public JFace(String texture) {
        this.texture = "#" + texture;
    }

    public JFace uv(float x1, float y1, float x2, float y2) {
        this.uv[0] = x1;
        this.uv[1] = y1;
        this.uv[2] = x2;
        this.uv[3] = y2;
        return this;
    }

    public JFace cullface(Direction direction) {
        this.cullface = direction.getName();
        return this;
    }

    public JFace rotate90Degrees() {
        this.rotation = 90;
        return this;
    }

    public JFace rotate180Degrees() {
        this.rotation = 180;
        return this;
    }

    public JFace rotate270Degrees() {
        this.rotation = 270;
        return this;
    }

    public JFace tintIndex(int tintIndex) {
        this.tintIndex = tintIndex;
        return this;
    }

    @Override
    public JFace clone() {
        try {
            return (JFace) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
}
