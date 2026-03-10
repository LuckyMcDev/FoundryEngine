package io.github.luckymcdev.foundryengine.common.script;

import io.github.luckymcdev.foundryengine.common.priority.Priority;
import net.neoforged.bus.api.IEventBus;

/**
 * An Entrypoint to a Bundle. Each Bundle can Contain multiple of these.
 * It is encouraged to follow the standard of either subclasses / same classes which are listeners of a respective event bus.
 */
public abstract class BundleEntrypoint {
    protected final IEventBus eventBus;
    protected final IEventBus bundleBus;

    protected BundleEntrypoint(IEventBus bundleBus, IEventBus eventBus) {
        this.bundleBus = bundleBus;
        this.eventBus = eventBus;
    }

    /**
     * Returns the Priority of this Entrypoint.
     *
     * @return the priority, NORMAL by default
     */
    public Priority getPriority() {
        return Priority.NORMAL;
    }

    /**
     * Called when the bundle is loaded. Override to register listeners, run setup logic, etc.
     */
    public abstract void onLoad();

    /**
     * Called when the bundle is reloaded or removed.
     */
    public abstract void onUnload();
}