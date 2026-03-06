package io.github.luckymcdev.foundryengine.common.game.behavior;

import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

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
