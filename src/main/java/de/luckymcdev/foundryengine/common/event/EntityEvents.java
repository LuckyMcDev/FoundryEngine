package de.luckymcdev.foundryengine.common.event;

import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import org.jetbrains.annotations.ApiStatus;

public class EntityEvents {
    public static final EventGroupHolder<EntityJoinLevelEvent> JOIN_LEVEL = new EventGroupHolder<>();
    public static final EventGroupHolder<LivingDeathEvent> DEATH = new EventGroupHolder<>();
    public static final EventGroupHolder<LivingDropsEvent> DROPS = new EventGroupHolder<>();
    public static final EventGroupHolder<LivingDamageEvent.Post> HURT = new EventGroupHolder<>();
    public static final EventGroupHolder<EntityJoinLevelEvent> SPAWNED = new EventGroupHolder<>();
    public static final EventGroupHolder<EntityJoinLevelEvent> CHECK_SPAWN = new EventGroupHolder<>();

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
        public static void postJoinLevel(EntityJoinLevelEvent e) {
            JOIN_LEVEL.post(e);
        }

        public static void postDeath(LivingDeathEvent e) {
            DEATH.post(e);
        }

        public static void postDrops(LivingDropsEvent e) {
            DROPS.post(e);
        }

        public static void postHurt(LivingDamageEvent.Post e) {
            HURT.post(e);
        }

        public static void postSpawned(EntityJoinLevelEvent e) {
            SPAWNED.post(e);
        }

        public static void postCheckSpawn(EntityJoinLevelEvent e) {
            CHECK_SPAWN.post(e);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postJoinLevel);
            bus.addListener(Internal::postDeath);
            bus.addListener(Internal::postDrops);
            bus.addListener(Internal::postHurt);
            bus.addListener(Internal::postSpawned);
            bus.addListener(Internal::postCheckSpawn);
        }

        public static void clear() {
            JOIN_LEVEL.clear();
            DEATH.clear();
            DROPS.clear();
            HURT.clear();
            SPAWNED.clear();
            CHECK_SPAWN.clear();
        }

        static {
            Common.registerEventClear(Internal::clear);
        }
    }
}
