package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.annotations.SerializedName;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable.JConsumeEffect;

import java.util.ArrayList;
import java.util.List;

public class JDeathProtectionComponent extends AbstractJComponent {
    @SerializedName("death_effects")
    private final List<JConsumeEffect> deathEffects = new ArrayList<>();

    public JDeathProtectionComponent deathEffect(JConsumeEffect effect) {
        this.deathEffects.add(effect);
        return this;
    }

    public JDeathProtectionComponent clearAllEffects() {
        return this.deathEffect(new JConsumeEffect("minecraft:clear_all_effects"));
    }
}
