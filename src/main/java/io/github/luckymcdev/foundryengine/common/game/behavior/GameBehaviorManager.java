package io.github.luckymcdev.foundryengine.common.game.behavior;

import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import io.github.luckymcdev.foundryengine.common.registry.RegistryRef;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Manager for all Game Behavior.
 * THIS WILL MAYBE BE CHANGED TO BE JUST EVENTS! USE WITH CAUTION
 */
public class GameBehaviorManager {
    private final GenericRegistry<Identifier, GameBehavior> BEHAVIORS = new GenericRegistry<>();

    public void register(Identifier id, GameBehavior gameBehavior) {
        gameBehavior.onRegister();
        this.BEHAVIORS.register(id, gameBehavior);
    }

    public void remove(Identifier id) {
        this.BEHAVIORS.get(id).onUnregister();
        this.BEHAVIORS.remove(id);
    }

    public void enable(Identifier id) {
        RegistryRef<Identifier, GameBehavior> ref = this.BEHAVIORS.getRef(id);
        ref.get().enable();
    }

    public void disable(Identifier id) {
        RegistryRef<Identifier, GameBehavior> ref = this.BEHAVIORS.getRef(id);
        ref.get().disable();
    }

    public boolean isEnabled(Identifier id) {
        RegistryRef<Identifier, GameBehavior> ref = this.BEHAVIORS.getRef(id);
        return ref.get().isEnabled();
    }

    public <T extends GameBehavior> List<T> getBehaviors(Class<T> type) {
        List<T> result = new ArrayList<>();
        for (GameBehavior behavior : BEHAVIORS.values()) {
            if (type.isInstance(behavior) && behavior.isEnabled()) {
                result.add((T) behavior);
            }
        }
        return result;
    }
}
