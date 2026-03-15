package io.github.luckymcdev.foundryengine.client.editor;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The BuiltInEditor is a Handlder for all Panels.
 * To add a Panel to this Editor, register it via {@link io.github.luckymcdev.foundryengine.client.editor.event.RegisterPanelEvent}
 * Methods provided are for opening and closing as well as registering / removing Panels from the Registry.
 */
public class EditorManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final GenericRegistry<Identifier, Panel> PANELS = new GenericRegistry<>();

    public EditorManager() {
        // No need for setting anything so empty.
    }

    /**
     * Registers a Panel.
     *
     * @param panel the Panel to register.
     */
    public void register(Panel panel) {
        PANELS.register(panel.getId(), panel);
    }

    /**
     * Removes a Panel
     * @param panel the Panel to remove
     */
    public void remove(Panel panel) {
        closePanel(panel);
        PANELS.remove(panel.getId());
    }

    /**
     * Opens a Panel
     * @param panel the Panel to open
     */
    public void openPanel(Panel panel) {
        if (!isOpen(panel)) {
            panel.open();
        }
    }

    /**
     * Closes a Panel
     * @param panel the Panel to close
     */
    public void closePanel(Panel panel) {
        if (isOpen(panel)) {
            panel.close();
        }
    }

    /**
     * Toggles a Panel between Open and Close depending on current State.
     * @param panel the Panel to Toggle.
     */
    public void togglePanel(Panel panel) {
        if (isOpen(panel)) closePanel(panel);
        else openPanel(panel);
    }

    /**
     * Returns if a Panel is currently Open.
     * INFO: Panel open State is managed by the Panel itself. {@link Panel#open}
     * @param panel the Panel to check
     * @return the current openState.
     */
    public boolean isOpen(Panel panel) {
        return panel != null && panel.isOpen();
    }

    /**
     * Closes All Panels managed by the EditorManager.
     */
    public void closeAllPanels() {
        List<Panel> snapshot = new ArrayList<>();
        PANELS.forEach(panel -> {
            if (panel.isOpen()) snapshot.add(panel);
        });
        snapshot.forEach(this::closePanel);
    }

    /**
     * Calls the {@link Panel#handleTick()} method for all managed Panels.
     */
    public void handleTick() {
        PANELS.forEach(panel -> {
            if (panel.isOpen()) panel.handleTick();
        });
    }

    /**
     * Calls the {@link Panel#handleRender()} method for all managed Panels.
     */
    public void handleRender() {
        List<Panel> toClose = new ArrayList<>();

        PANELS.forEach(panel -> {
            if (panel.isOpen() && !panel.handleRender()) {
                toClose.add(panel);
            }
        });

        toClose.forEach(this::closePanel);
    }

    /**
     * Returns the {@link GenericRegistry} which contains all Panels
     * @return the Panel Registry.
     */
    public GenericRegistry<Identifier, Panel> getPanels() {
        return PANELS;
    }
}
