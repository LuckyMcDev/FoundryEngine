package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable;

import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class JRemoveEffects extends JConsumeEffect {
    private final List<Identifier> effects = new ArrayList<>();

    public JRemoveEffects() {
        super("minecraft:remove_effects");
    }

    public JRemoveEffects effect(Identifier effect) {
        this.effects.add(effect);
        return this;
    }
}
