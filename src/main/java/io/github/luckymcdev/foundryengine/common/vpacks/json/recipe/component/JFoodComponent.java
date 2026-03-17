package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.annotations.SerializedName;

public class JFoodComponent extends AbstractJComponent {
    private final int nutrition;
    private final int saturation;
    @SerializedName("can_always_eat")
    private Boolean canAlwaysEat;

    public JFoodComponent(int nutrition, int saturation) {
        this.nutrition = nutrition;
        this.saturation = saturation;
    }

    public JFoodComponent canAlwaysEat(boolean canAlwaysEat) {
        this.canAlwaysEat = canAlwaysEat;
        return this;
    }
}
