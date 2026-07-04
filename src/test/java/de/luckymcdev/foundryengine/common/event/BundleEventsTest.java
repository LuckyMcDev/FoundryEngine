package de.luckymcdev.foundryengine.common.event;

import net.minecraft.server.MinecraftServer;
import net.neoforged.testframework.junit.EphemeralTestServerProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(EphemeralTestServerProvider.class)
class BundleEventsTest {

    @Test
    void registry_RegisterAndPost_CallbackExecuted() {
        AtomicBoolean called = new AtomicBoolean(false);
        BundleEvents.registry(event -> called.set(true));

        // Post via internal API
        var collector = new de.luckymcdev.foundryengine.common.registry.RegistryCollector();
        var registryEvent = new de.luckymcdev.foundryengine.common.event.registry.RegistryEvent(null, collector);
        BundleEvents.Internal.postRegistry(registryEvent);

        assertTrue(called.get());
    }

    @Test
    void registry_MultipleCallbacks_AllExecuted() {
        AtomicInteger count = new AtomicInteger();
        BundleEvents.registry(event -> count.incrementAndGet());
        BundleEvents.registry(event -> count.incrementAndGet());
        BundleEvents.registry(event -> count.incrementAndGet());

        var collector = new de.luckymcdev.foundryengine.common.registry.RegistryCollector();
        var registryEvent = new de.luckymcdev.foundryengine.common.event.registry.RegistryEvent(null, collector);
        BundleEvents.Internal.postRegistry(registryEvent);

        assertEquals(3, count.get());
    }

    @Test
    void vanillaGame_RegisterAndPost_CallbackExecuted() {
        AtomicBoolean called = new AtomicBoolean(false);
        BundleEvents.vanillaGame(event -> called.set(true));

        var vanillaEvent = new net.neoforged.neoforge.event.VanillaGameEvent(
                null,
                net.minecraft.world.level.gameevent.GameEvent.STEP,
                net.minecraft.world.phys.Vec3.ZERO,
                net.minecraft.world.level.gameevent.GameEvent.Context.of((net.minecraft.world.entity.Entity) null));
        BundleEvents.Internal.postVanillaGame(vanillaEvent);

        assertTrue(called.get());
    }

    @Test
    void commonSetup_RegisterAndPost_CallbackExecuted() {
        AtomicBoolean called = new AtomicBoolean(false);
        BundleEvents.commonSetup(event -> called.set(true));
        BundleEvents.Internal.postCommonSetup(null);
        assertTrue(called.get());
    }

    @Test
    void clear_RemovesAllCallbacks() {
        AtomicBoolean called = new AtomicBoolean(false);
        BundleEvents.registry(event -> called.set(true));
        BundleEvents.Internal.clear();

        var collector = new de.luckymcdev.foundryengine.common.registry.RegistryCollector();
        var registryEvent = new de.luckymcdev.foundryengine.common.event.registry.RegistryEvent(null, collector);
        BundleEvents.Internal.postRegistry(registryEvent);

        assertFalse(called.get());
    }

    @Test
    void custom_RegisterAndPost_CallbackExecuted(MinecraftServer server) {
        AtomicBoolean called = new AtomicBoolean(false);
        BundleEvents.custom(TestCustomEvent.class, event -> called.set(true));

        BundleEvents.Internal.postCustom(new TestCustomEvent());
        assertTrue(called.get());
    }

    private static class TestCustomEvent extends net.neoforged.bus.api.Event {
    }
}
