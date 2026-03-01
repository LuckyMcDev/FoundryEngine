package io.github.luckymcdev.foundryengine.common.script;

import net.neoforged.bus.api.IEventBus;

public abstract class BundleEntrypoint {
    protected final IEventBus eventBus;
    protected final IEventBus bundleBus;

    protected BundleEntrypoint(IEventBus bundleBus, IEventBus eventBus) {
        this.bundleBus = bundleBus;
        this.eventBus = eventBus;
    }

    /**
     * Called when the bundle is loaded. Override to register listeners, run setup logic, etc.
     */
    public abstract void onLoad();

    /**
     * Called when the bundle is reloaded or removed.
     */
    public final void onUnload() {
        // This removes any @SubscribeEvent methods on this object from the bus
        eventBus.unregister(this);
    }
}