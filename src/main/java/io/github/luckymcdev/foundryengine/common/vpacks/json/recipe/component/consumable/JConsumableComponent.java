package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable;

import com.google.gson.annotations.SerializedName;
import io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component.AbstractJComponent;

import java.util.ArrayList;
import java.util.List;

public class JConsumableComponent extends AbstractJComponent {
    @SerializedName("on_consume_effects")
    private final List<JConsumeEffect> onConsumeEffects = new ArrayList<>();
    @SerializedName("consume_seconds")
    private Float consumeSeconds;
    private JConsumeAnimation animation;
    private JSound sound;
    @SerializedName("has_consume_particles")
    private Boolean hasConsumeParticles;

    public JConsumableComponent consumeSeconds(float consumeSeconds) {
        this.consumeSeconds = consumeSeconds;
        return this;
    }

    public JConsumableComponent animation(JConsumeAnimation animation) {
        this.animation = animation;
        return this;
    }

    public JConsumableComponent sound(JSound sound) {
        this.sound = sound;
        return this;
    }

    public JConsumableComponent hasConsumeParticles(boolean hasConsumeParticles) {
        this.hasConsumeParticles = hasConsumeParticles;
        return this;
    }

    public JConsumableComponent onConsumeEffect(JConsumeEffect effect) {
        this.onConsumeEffects.add(effect);
        return this;
    }

    public JConsumableComponent clearAllEffects() {
        return this.onConsumeEffect(new JConsumeEffect("minecraft:clear_all_effects"));
    }

    // TODO: Add methods for adding specific effects
}
