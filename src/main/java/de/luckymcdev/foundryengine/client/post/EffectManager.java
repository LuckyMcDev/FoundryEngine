package de.luckymcdev.foundryengine.client.post;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.interfaces.EngineGameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Collection;

public class EffectManager {
    private final GenericRegistry<Identifier, PrioritizedEffect> registry = new GenericRegistry<>();

    public EffectManager() {
        register(new PrioritizedEffect(Common.mId("creeper"), 10));
        register(new PrioritizedEffect(Common.mId("spider"), 10));
        register(new PrioritizedEffect(Common.mId("invert"), 50));
        register(new PrioritizedEffect(Common.mId("blur"), 100));

        register(new PrioritizedEffect(Common.id("grayscale"), 55));
        register(new PrioritizedEffect(Common.id("depth_vis"), 40));
        register(new PrioritizedEffect(Common.id("bloom"), 10));
    }

    private EngineGameRenderer renderer() {
        return (EngineGameRenderer) Minecraft.getInstance().gameRenderer;
    }

    public void register(PrioritizedEffect effect) {
        registry.register(effect.id(), effect);
    }

    public void unregister(Identifier id) {
        renderer().engine$removeEffect(id);
        registry.remove(id);
    }

    public void enable(Identifier id) {
        setEffectActive(id, true);
    }

    public void disable(Identifier id) {
        setEffectActive(id, false);
    }

    public void setEffectActive(Identifier id, boolean active) {
        PrioritizedEffect effect = registry.get(id);
        setEffectActive(id, effect.priority(), active);
    }

    public void setEffectActive(Identifier id, int priority, boolean active) {
        if (active) {
            renderer().engine$addEffect(id, priority);
        } else {
            renderer().engine$removeEffect(id);
        }
    }

    public void clearAllEffects() {
        renderer().engine$clearEffects();
    }

    public void reload(Identifier id) {
        renderer().engine$invalidate(id);
    }

    public void reload() {
        for (PrioritizedEffect effect : registry.values()) {
            reload(effect.id());
        }
    }

    public Collection<PrioritizedEffect> getEffects() {
        return this.registry.values();
    }

    public Collection<Identifier> getActiveEffects() {
        return renderer().engine$getActiveEffects();
    }
}