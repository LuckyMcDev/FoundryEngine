package de.luckymcdev.foundryengine.api.event;

import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.event.BlueprintContexts;
import de.luckymcdev.foundryengine.common.event.EventCallback;
import de.luckymcdev.foundryengine.common.event.EventGroupHolder;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jetbrains.annotations.ApiStatus;

public class LevelEvents {
    public static final EventGroupHolder<LevelEvent.Load> LOAD =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_LEVEL_LOAD, BlueprintContexts::levelLoad);
    public static final EventGroupHolder<LevelEvent.Unload> UNLOAD =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_LEVEL_UNLOAD, BlueprintContexts::levelUnload);
    public static final EventGroupHolder<LevelEvent.Save> SAVE =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_LEVEL_SAVE, BlueprintContexts::levelSave);
    public static final EventGroupHolder<LevelTickEvent.Post> TICK =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_LEVEL_TICK, BlueprintContexts::levelTick);
    public static final EventGroupHolder<ExplosionEvent.Start> BEFORE_EXPLOSION =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_BEFORE_EXPLOSION, BlueprintContexts::beforeExplosion);
    public static final EventGroupHolder<ExplosionEvent.Detonate> AFTER_EXPLOSION =
            new EventGroupHolder<>(BlueprintEngine.BuiltinNodes.EVENT_AFTER_EXPLOSION, BlueprintContexts::afterExplosion);

    public static void load(EventCallback<LevelEvent.Load> cb) {
        LOAD.register(cb);
    }

    public static void unload(EventCallback<LevelEvent.Unload> cb) {
        UNLOAD.register(cb);
    }

    public static void save(EventCallback<LevelEvent.Save> cb) {
        SAVE.register(cb);
    }

    public static void tick(EventCallback<LevelTickEvent.Post> cb) {
        TICK.register(cb);
    }

    public static void beforeExplosion(EventCallback<ExplosionEvent.Start> cb) {
        BEFORE_EXPLOSION.register(cb);
    }

    public static void afterExplosion(EventCallback<ExplosionEvent.Detonate> cb) {
        AFTER_EXPLOSION.register(cb);
    }

    @ApiStatus.Internal
    public static class Internal {
        public static void postLoad(LevelEvent.Load e) {
            LOAD.post(e);
        }

        public static void postUnload(LevelEvent.Unload e) {
            UNLOAD.post(e);
        }

        public static void postSave(LevelEvent.Save e) {
            SAVE.post(e);
        }

        public static void postTick(LevelTickEvent.Post e) {
            TICK.post(e);
        }

        public static void postBeforeExplosion(ExplosionEvent.Start e) {
            BEFORE_EXPLOSION.post(e);
        }

        public static void postAfterExplosion(ExplosionEvent.Detonate e) {
            AFTER_EXPLOSION.post(e);
        }

        public static void register(IEventBus bus) {
            bus.addListener(Internal::postLoad);
            bus.addListener(Internal::postUnload);
            bus.addListener(Internal::postSave);
            bus.addListener(Internal::postTick);
            bus.addListener(Internal::postBeforeExplosion);
            bus.addListener(Internal::postAfterExplosion);
        }

        public static void clear() {
            LOAD.clear();
            UNLOAD.clear();
            SAVE.clear();
            TICK.clear();
            BEFORE_EXPLOSION.clear();
            AFTER_EXPLOSION.clear();
        }
    }
}