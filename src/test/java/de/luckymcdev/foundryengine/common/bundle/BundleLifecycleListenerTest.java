package de.luckymcdev.foundryengine.common.bundle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BundleLifecycleListenerTest {

    @Test
    void defaultMethods_DoNotThrow() {
        BundleLifecycleListener listener = new BundleLifecycleListener() {};
        assertDoesNotThrow(() -> listener.onBundleLoaded(null));
        assertDoesNotThrow(() -> listener.onBundlePreUnload(null));
        assertDoesNotThrow(() -> listener.onBundleUnloaded(null));
        assertDoesNotThrow(listener::onBundleReloadStarted);
        assertDoesNotThrow(listener::onBundleReloadCompleted);
    }
}
