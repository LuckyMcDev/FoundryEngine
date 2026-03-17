package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable;

import com.google.gson.annotations.SerializedName;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class JApplyEffects extends JConsumeEffect {
    private final List<JEffect> effects = new ArrayList<>();

    public JApplyEffects() {
        super("minecraft:apply_effects");
    }

    public JApplyEffects effect(JEffect effect) {
        this.effects.add(effect);
        return this;
    }

    public static class JEffect {
        private final Identifier id;
        private Integer amplifier;
        private Integer duration;
        private Boolean ambient;
        @SerializedName("show_particles")
        private Boolean showParticles;
        @SerializedName("show_icon")
        private Boolean showIcon;
        @SerializedName("hidden_effect")
        private JEffect hiddenEffect;

        public JEffect(Identifier id) {
            this.id = id;
        }

        public JEffect amplifier(int amplifier) {
            this.amplifier = amplifier;
            return this;
        }

        public JEffect duration(int duration) {
            this.duration = duration;
            return this;
        }

        public JEffect ambient(boolean ambient) {
            this.ambient = ambient;
            return this;
        }

        public JEffect showParticles(boolean showParticles) {
            this.showParticles = showParticles;
            return this;
        }

        public JEffect showIcon(boolean showIcon) {
            this.showIcon = showIcon;
            return this;
        }

        public JEffect hiddenEffect(JEffect hiddenEffect) {
            this.hiddenEffect = hiddenEffect;
            return this;
        }
    }
}
