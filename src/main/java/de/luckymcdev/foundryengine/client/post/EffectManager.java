package de.luckymcdev.foundryengine.client.post;

import de.luckymcdev.foundryengine.interfaces.EngineGameRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class EffectManager {
    private final Set<Identifier> ACTIVE_MOD_EFFECTS = new HashSet<>();

    /**
     * Enables an Effect
     */
    public void setEffectActive(Identifier id, int priority, boolean active) {
        EngineGameRenderer stack = (EngineGameRenderer) Minecraft.getInstance().gameRenderer;
        if (active) {
            if (ACTIVE_MOD_EFFECTS.add(id)) {
                stack.engine$addEffect(id, priority);
            }
        } else {
            if (ACTIVE_MOD_EFFECTS.remove(id)) {
                stack.engine$removeEffect(id);
            }
        }
    }

    /**
     * Clears all mod-managed effects from the GameRenderer stack.
     */
    public void clearAllEffects() {
        EngineGameRenderer stack = (EngineGameRenderer) Minecraft.getInstance().gameRenderer;
        for (Identifier id : ACTIVE_MOD_EFFECTS) {
            stack.engine$removeEffect(id);
        }
        ACTIVE_MOD_EFFECTS.clear();
    }

    /**
     * Returns an unmodifiable view of currently active modded effects.
     */
    public Set<Identifier> getActiveEffects() {
        return Collections.unmodifiableSet(ACTIVE_MOD_EFFECTS);
    }
}