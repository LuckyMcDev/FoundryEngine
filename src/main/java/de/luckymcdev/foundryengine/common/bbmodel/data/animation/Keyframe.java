package de.luckymcdev.foundryengine.common.bbmodel.data.animation;

import com.google.gson.annotations.SerializedName;
import de.luckymcdev.foundryengine.common.util.color.Color;

import java.util.List;

public class Keyframe {

    private String channel;
    @SerializedName("data_points")
    private List<DataPoint> dataPoints;
    private String uuid;
    private Float time;
    private Color color;
    private String interpolation;
    private String easing;
    private Object[] easingArgs;
    @SerializedName("bezier_left_time")
    private Float[] bezierLeftTime;
    @SerializedName("bezier_right_time")
    private Float[] bezierRightTime;
    @SerializedName("bezier_left_value")
    private Float[] bezierLeftValue;
    @SerializedName("bezier_right_value")
    private Float[] bezierRightValue;

    public String getChannel() {
        return channel;
    }

    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }

    public String getUuid() {
        return uuid;
    }

    public Float getTime() {
        return time;
    }

    public Color getColor() {
        return color;
    }

    public String getInterpolation() {
        return interpolation;
    }

    public String getEasing() {
        return easing;
    }

    public Object[] getEasingArgs() {
        return easingArgs;
    }

    public Float[] getBezierLeftTime() {
        return bezierLeftTime;
    }

    public Float[] getBezierRightTime() {
        return bezierRightTime;
    }

    public Float[] getBezierLeftValue() {
        return bezierLeftValue;
    }

    public Float[] getBezierRightValue() {
        return bezierRightValue;
    }
}
