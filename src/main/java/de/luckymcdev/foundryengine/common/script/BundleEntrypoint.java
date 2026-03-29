package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig;
import de.luckymcdev.foundryengine.common.priority.Priority;
import net.neoforged.bus.api.IEventBus;

/**
 * An Entrypoint to a Bundle. Each Bundle can contain multiple of these.
 * It is encouraged to follow the standard of either subclasses / same classes which are listeners of a respective event bus.
 */
public abstract class BundleEntrypoint {
    protected final IEventBus eventBus;
    protected final IEventBus bundleBus;
    protected final BundleConfig bundleConfig;

    protected BundleEntrypoint(IEventBus bundleBus, IEventBus eventBus, BundleConfig bundleConfig) {
        this.bundleBus = bundleBus;
        this.eventBus = eventBus;
        this.bundleConfig = bundleConfig;
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
     * This is also where you should define your config values via {@link de.luckymcdev.foundryengine.common.bundle.config.BundleConfigSpec}.
     */
    public abstract void onLoad();

    /**
     * Called when the bundle is reloaded or removed.
     */
    public abstract void onUnload();
}