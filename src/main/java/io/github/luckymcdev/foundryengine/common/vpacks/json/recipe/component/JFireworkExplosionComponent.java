package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class JFireworkExplosionComponent extends AbstractJComponent {
    private final Shape shape;
    private List<Integer> colors;
    @SerializedName("fade_colors")
    private List<Integer> fadeColors;
    @SerializedName("has_trail")
    private Boolean hasTrail;
    @SerializedName("has_twinkle")
    private Boolean hasTwinkle;

    public JFireworkExplosionComponent(Shape shape) {
        this.shape = shape;
    }

    public JFireworkExplosionComponent color(int color) {
        if (this.colors == null) {
            this.colors = new ArrayList<>();
        }
        this.colors.add(color);
        return this;
    }

    public JFireworkExplosionComponent fadeColor(int fadeColor) {
        if (this.fadeColors == null) {
            this.fadeColors = new ArrayList<>();
        }
        this.fadeColors.add(fadeColor);
        return this;
    }

    public JFireworkExplosionComponent hasTrail(boolean hasTrail) {
        this.hasTrail = hasTrail;
        return this;
    }

    public JFireworkExplosionComponent hasTwinkle(boolean hasTwinkle) {
        this.hasTrail = hasTwinkle;
        return this;
    }

    public enum Shape {
        @SerializedName("small_ball")
        SmallBall,
        @SerializedName("large_ball")
        LargeBall,
        @SerializedName("state")
        Star,
        @SerializedName("creeper")
        Creeper,
        @SerializedName("burst")
        Burst
    }
}
