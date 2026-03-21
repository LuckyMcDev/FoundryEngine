package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import net.minecraft.resources.Identifier;

public class JSuspiciousStewEffect {
    private final Identifier id;
    private Integer duration;

    public JSuspiciousStewEffect(Identifier id) {
        this.id = id;
    }

    public JSuspiciousStewEffect duration(int duration) {
        this.duration = duration;
        return this;
    }
}
