package io.github.luckymcdev.foundryengine.client.editor;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * The Built-In Editor
 */
public class BuiltInEditor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final GenericRegistry<Identifier, Panel> PANELS = new GenericRegistry<>();
    private static final GenericRegistry<Identifier, Panel> OPEN_PANELS = new GenericRegistry<>();

    /**
     * Instantiates a new Built-in editor.
     */
    public BuiltInEditor() {
    }

    /**
     * Registers a new panel to the Editor.
     *
     * @param panel the panel
     */
    public void register(Panel panel) {
        PANELS.register(panel.getId(), panel);
    }

    /**
     * Removes a Panel.
     *
     * @param panel the panel
     */
    public void remove(Panel panel) {
        PANELS.remove(panel.getId());
    }

    /**
     * Handles Ticking for all Panels.
     */
    public void handleTick() {
        OPEN_PANELS.forEach(Panel::handleTick);
    }

    /**
     * Handles Rendering for all Panels.
     */
    public void handleRender() {
        List<Identifier> panelsToRemove = new ArrayList<>();
        OPEN_PANELS.forEach(panel -> {
            if (!panel.handleRender()) {
                panelsToRemove.add(panel.getId());
            }
        });
        panelsToRemove.forEach(OPEN_PANELS::remove);
    }

    /**
     * Toggles a Panel On and Off.
     *
     * @param panel the panel
     */
    public void togglePanel(Panel panel) {
        if(checkOpen(panel)) {
            closePanel(panel);
        } else {
            openPanel(panel);
        }
    }

    /**
     * Opens a Panel.
     * Ref: {@link BuiltInEditor#togglePanel(Panel panel)}
     *
     * @param panel the panel
     */
    public void openPanel(Panel panel) {
        if (!checkOpen(panel)) {
            OPEN_PANELS.register(panel.getId(), panel);
            panel.open();
        }
    }

    /**
     * Closes a Panel.
     * Ref: {@link BuiltInEditor#togglePanel(Panel panel)}
     *
     * @param panel the panel
     */
    public void closePanel(Panel panel) {
        if (checkOpen(panel)) {
            OPEN_PANELS.remove(panel.getId());
            panel.close();
        }
    }

    private boolean checkOpen(Panel panel) {
        return OPEN_PANELS.contains(panel.getId());
    }
}