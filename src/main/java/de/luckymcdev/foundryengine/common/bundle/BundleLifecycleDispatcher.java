package de.luckymcdev.foundryengine.common.bundle;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BundleLifecycleDispatcher {
    private final List<BundleLifecycleListener> listeners = new CopyOnWriteArrayList<>();

    public void register(BundleLifecycleListener listener) {
        listeners.add(listener);
    }

    public void unregister(BundleLifecycleListener listener) {
        listeners.remove(listener);
    }

    void fireLoaded(Bundle bundle) {
        listeners.forEach(l -> l.onBundleLoaded(bundle));
    }

    void firePreUnload(Bundle bundle) {
        listeners.forEach(l -> l.onBundlePreUnload(bundle));
    }

    void fireUnloaded(Bundle bundle) {
        listeners.forEach(l -> l.onBundleUnloaded(bundle));
    }

    void fireReloadStarted() {
        listeners.forEach(BundleLifecycleListener::onBundleReloadStarted);
    }

    void fireReloadCompleted() {
        listeners.forEach(BundleLifecycleListener::onBundleReloadCompleted);
    }
}
