package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable;

public class JPlaySound extends JConsumeEffect {
    private final JSound sound;

    public JPlaySound() {
        this(null);
    }

    public JPlaySound(JSound sound) {
        super("minecraft:play_sound");
        this.sound = sound;
    }
}
