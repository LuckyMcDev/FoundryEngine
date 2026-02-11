package io.github.luckymcdev.client.editor;

import io.github.luckymcdev.common.registry.GenericRegistry;

import java.util.ArrayList;
import java.util.List;

public class BuiltInEditor {
    private static final GenericRegistry<String, Panel> OPEN_PANELS = new GenericRegistry<>();

    public void handle() {
        List<String> panelsToRemove = new ArrayList<>();
        OPEN_PANELS.getValues().forEach(panel -> {
            if (!panel.handle()) {
                panelsToRemove.add(panel.getId().toString());
            }
        });
        panelsToRemove.forEach(OPEN_PANELS::remove);
    }

    public void openPanel(Panel panel) {
        if (!OPEN_PANELS.contains(panel.getId().toString())) {
            OPEN_PANELS.register(panel.getId().toString(), panel);
            panel.open();
        }
    }

    public void closePanel(Panel panel) {
        if (OPEN_PANELS.contains(panel.getId().toString())) {
            OPEN_PANELS.remove(panel.getId().toString());
            panel.close();
        }
    }
}