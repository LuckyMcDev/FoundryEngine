package de.luckymcdev.foundryengine.common.feature;

import net.minecraft.resources.Identifier;

public class EngineFeature {
    private boolean enabled;
    private final Identifier id;

    public EngineFeature(Identifier id) {
        this.id = id;
        this.enabled = true;
    }

    public Identifier identifier() {
        return id;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void enable() {
        this.enabled = true;
    }

    public void disable() {
        this.enabled = false;
    }
}
