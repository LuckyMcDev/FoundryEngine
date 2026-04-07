package de.luckymcdev.foundryengine.client.post;

import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.interfaces.EngineGameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EffectManager {
    private final GenericRegistry<Identifier, PrioritizedEffect> registry = new GenericRegistry<>();
    private final Set<Identifier> activeEffects = ConcurrentHashMap.newKeySet();

    public EffectManager() {
        register(new PrioritizedEffect(Identifier.withDefaultNamespace("creeper"), 10));
        register(new PrioritizedEffect(Identifier.withDefaultNamespace("spider"), 10));
        register(new PrioritizedEffect(Identifier.withDefaultNamespace("invert"), 50));
        register(new PrioritizedEffect(Identifier.withDefaultNamespace("blur"), 100));

        register(new PrioritizedEffect(Common.id("grayscale"), 55));
    }

    public void register(PrioritizedEffect effect) {
        registry.register(effect.id(), effect);
    }

    public void unregister(Identifier id) {
        setEffectActive(id, false);
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
        EngineGameRenderer renderer = (EngineGameRenderer) Minecraft.getInstance().gameRenderer;
        if (active) {
            if (activeEffects.add(id)) {
                renderer.engine$addEffect(id, priority);
            }
        } else {
            if (activeEffects.remove(id)) {
                renderer.engine$removeEffect(id);
            }
        }
    }

    public void clearAllEffects() {
        EngineGameRenderer renderer = (EngineGameRenderer) Minecraft.getInstance().gameRenderer;
        activeEffects.forEach(renderer::engine$removeEffect);
        activeEffects.clear();
    }

    public Collection<PrioritizedEffect> getEffects() {
        return this.registry.values();
    }

    public Collection<Identifier> getActiveEffects() {
        return Collections.unmodifiableSet(activeEffects);
    }
}