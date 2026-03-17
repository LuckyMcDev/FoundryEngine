package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import net.minecraft.resources.Identifier;

public class JDamageResistanceComponent extends AbstractJComponent {
    private final Identifier types;

    public JDamageResistanceComponent(Identifier types) {
        this.types = types;
    }
}
