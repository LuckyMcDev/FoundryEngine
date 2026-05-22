package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.jetbrains.annotations.ApiStatus;

public class EntityEvents {
    public static final EventGroupHolder<EntityJoinLevelEvent> JOIN_LEVEL = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ENTITY_JOIN_LEVEL, BlueprintContexts::entityJoinLevel);
    public static final EventGroupHolder<LivingDeathEvent> DEATH = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_LIVING_DEATH, BlueprintContexts::entityDeath);
    public static final EventGroupHolder<LivingDropsEvent> DROPS = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_LIVING_DROPS, BlueprintContexts::entityDrops);
    public static final EventGroupHolder<LivingDamageEvent.Post> HURT = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_LIVING_HURT, BlueprintContexts::entityHurt);
    public static final EventGroupHolder<EntityJoinLevelEvent> SPAWNED = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ENTITY_JOIN_LEVEL, BlueprintContexts::entityJoinLevel);
    public static final EventGroupHolder<EntityJoinLevelEvent> CHECK_SPAWN = new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_ENTITY_JOIN_LEVEL, BlueprintContexts::entityJoinLevel);

    public static void joinLevel(EventCallback<EntityJoinLevelEvent> cb) {
        JOIN_LEVEL.register(cb);
    }

    public static void death(EventCallback<LivingDeathEvent> cb) {
        DEATH.register(cb);
    }

    public static void drops(EventCallback<LivingDropsEvent> cb) {
        DROPS.register(cb);
    }

    public static void hurt(EventCallback<LivingDamageEvent.Post> cb) {
        HURT.register(cb);
    }

    public static void spawned(EventCallback<EntityJoinLevelEvent> cb) {
        SPAWNED.register(cb);
    }

    public static void checkSpawn(EventCallback<EntityJoinLevelEvent> cb) {
        CHECK_SPAWN.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {

        public static void register(IEventBus bus) {
            bus.addListener(JOIN_LEVEL::post);
            bus.addListener(DEATH::post);
            bus.addListener(DROPS::post);
            bus.addListener(HURT::post);
            bus.addListener(SPAWNED::post);
            bus.addListener(CHECK_SPAWN::post);
        }

        public static void clear() {
            JOIN_LEVEL.clear();
            DEATH.clear();
            DROPS.clear();
            HURT.clear();
            SPAWNED.clear();
            CHECK_SPAWN.clear();
        }
    }
}