package io.github.luckymcdev.foundryengine.client.editor;

import com.j256.ormlite.stmt.query.Ge;
import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.client.editor.popup.PopUp;
import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BuiltInEditor {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final GenericRegistry<Identifier, Panel> PANELS = new GenericRegistry<>();
    private static final GenericRegistry<Identifier, PopUp> POPUPS = new GenericRegistry<>();

    public BuiltInEditor() {}

    public void register(Panel panel) {
        PANELS.register(panel.getId(), panel);
    }

    public void remove(Panel panel) {
        closePanel(panel);
        PANELS.remove(panel.getId());
    }

    public void openPanel(Panel panel) {
        if (!isOpen(panel)) {
            panel.open();
        }
    }

    public void closePanel(Panel panel) {
        if (isOpen(panel)) {
            panel.close();
        }
    }

    public void togglePanel(Panel panel) {
        if (isOpen(panel)) closePanel(panel);
        else openPanel(panel);
    }

    public boolean isOpen(Panel panel) {
        return panel != null && panel.isOpen();
    }

    public void closeAllPanels() {
        List<Panel> snapshot = new ArrayList<>();
        PANELS.forEach(panel -> {
            if (panel.isOpen()) snapshot.add(panel);
        });
        snapshot.forEach(this::closePanel);
    }

    public void handleTick() {
        PANELS.forEach(panel -> {
            if (panel.isOpen()) panel.handleTick();
        });
    }

    public void handleRender() {
        List<Panel> toClose = new ArrayList<>();

        PANELS.forEach(panel -> {
            if (panel.isOpen() && !panel.handleRender()) {
                toClose.add(panel);
            }
        });

        toClose.forEach(this::closePanel);
    }

    public GenericRegistry<Identifier, Panel> getPanels() {
        return PANELS;
    }
}
