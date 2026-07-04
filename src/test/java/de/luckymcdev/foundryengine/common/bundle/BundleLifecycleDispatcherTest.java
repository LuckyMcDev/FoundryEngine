package de.luckymcdev.foundryengine.common.bundle;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class BundleLifecycleDispatcherTest {

    @Test
    void fireLoaded_CallsListener() {
        BundleLifecycleDispatcher dispatcher = new BundleLifecycleDispatcher();
        AtomicBoolean called = new AtomicBoolean(false);
        dispatcher.register(new BundleLifecycleListener() {
            @Override
            public void onBundleLoaded(Bundle bundle) {
                called.set(true);
            }
        });
        dispatcher.fireLoaded(null);
        assertTrue(called.get());
    }

    @Test
    void firePreUnload_CallsListener() {
        BundleLifecycleDispatcher dispatcher = new BundleLifecycleDispatcher();
        AtomicBoolean called = new AtomicBoolean(false);
        dispatcher.register(new BundleLifecycleListener() {
            @Override
            public void onBundlePreUnload(Bundle bundle) {
                called.set(true);
            }
        });
        dispatcher.firePreUnload(null);
        assertTrue(called.get());
    }

    @Test
    void fireUnloaded_CallsListener() {
        BundleLifecycleDispatcher dispatcher = new BundleLifecycleDispatcher();
        AtomicBoolean called = new AtomicBoolean(false);
        dispatcher.register(new BundleLifecycleListener() {
            @Override
            public void onBundleUnloaded(Bundle bundle) {
                called.set(true);
            }
        });
        dispatcher.fireUnloaded(null);
        assertTrue(called.get());
    }

    @Test
    void fireReloadStarted_CallsListener() {
        BundleLifecycleDispatcher dispatcher = new BundleLifecycleDispatcher();
        AtomicBoolean called = new AtomicBoolean(false);
        dispatcher.register(new BundleLifecycleListener() {
            @Override
            public void onBundleReloadStarted() {
                called.set(true);
            }
        });
        dispatcher.fireReloadStarted();
        assertTrue(called.get());
    }

    @Test
    void fireReloadCompleted_CallsListener() {
        BundleLifecycleDispatcher dispatcher = new BundleLifecycleDispatcher();
        AtomicBoolean called = new AtomicBoolean(false);
        dispatcher.register(new BundleLifecycleListener() {
            @Override
            public void onBundleReloadCompleted() {
                called.set(true);
            }
        });
        dispatcher.fireReloadCompleted();
        assertTrue(called.get());
    }

    @Test
    void multipleListeners_AllCalled() {
        BundleLifecycleDispatcher dispatcher = new BundleLifecycleDispatcher();
        AtomicInteger counter = new AtomicInteger();
        dispatcher.register(new BundleLifecycleListener() {
            @Override public void onBundleLoaded(Bundle bundle) { counter.incrementAndGet(); }
        });
        dispatcher.register(new BundleLifecycleListener() {
            @Override public void onBundleLoaded(Bundle bundle) { counter.incrementAndGet(); }
        });
        dispatcher.fireLoaded(null);
        assertEquals(2, counter.get());
    }

    @Test
    void unregister_ListenerNotCalled() {
        BundleLifecycleDispatcher dispatcher = new BundleLifecycleDispatcher();
        AtomicBoolean called = new AtomicBoolean(false);
        BundleLifecycleListener listener = new BundleLifecycleListener() {
            @Override
            public void onBundleLoaded(Bundle bundle) {
                called.set(true);
            }
        };
        dispatcher.register(listener);
        dispatcher.unregister(listener);
        dispatcher.fireLoaded(null);
        assertFalse(called.get());
    }

    @Test
    void noListeners_NoException() {
        BundleLifecycleDispatcher dispatcher = new BundleLifecycleDispatcher();
        assertDoesNotThrow(() -> {
            dispatcher.fireLoaded(null);
            dispatcher.firePreUnload(null);
            dispatcher.fireUnloaded(null);
            dispatcher.fireReloadStarted();
            dispatcher.fireReloadCompleted();
        });
    }
}
