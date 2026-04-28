package de.luckymcdev.foundryengine.common.script;

import de.luckymcdev.foundryengine.common.priority.Priority;

/**
 * An Entrypoint to a Bundle.
 */
public interface BundleEntrypoint {

    /**
     * Returns the Priority of this Entrypoint.
     *
     * @return the priority, NORMAL by default
     */
    default Priority getPriority() {
        return Priority.NORMAL;
    }

    /**
     * Called when the bundle is loaded.
     */
    void onLoad();

    /**
     * Called when the bundle is reloaded or removed.
     */
    void onUnload();
}