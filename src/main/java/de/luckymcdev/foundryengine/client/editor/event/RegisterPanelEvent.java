package de.luckymcdev.foundryengine.client.editor.event;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
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
        Client.getEditorManager().register(panel);
    }
}
