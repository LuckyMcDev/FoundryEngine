package io.github.luckymcdev.foundryengine.client.editor.event;

import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.neoforged.bus.api.Event;

/**
 * Event for registering panels in the built-in editor.
 */
public class RegisterPanelEvent extends Event {
    /**
     * Registers a panel with the built-in editor.
     *
     * @param panel The panel to register.
     */
    public void register(Panel panel) {
        Instances.getEditorManager().register(panel);
    }
}
