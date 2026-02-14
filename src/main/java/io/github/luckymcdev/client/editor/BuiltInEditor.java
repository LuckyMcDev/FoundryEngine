package io.github.luckymcdev.client.editor;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.common.registry.GenericRegistry;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class BuiltInEditor {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final GenericRegistry<ResourceLocation, Panel> PANELS = new GenericRegistry<>();
    private static final GenericRegistry<ResourceLocation, Panel> OPEN_PANELS = new GenericRegistry<>();

    public BuiltInEditor() {
    }

    public void register(Panel panel) {
        PANELS.register(panel.getId(), panel);
    }

    public void remove(Panel panel) {
        PANELS.remove(panel.getId());
    }

    public void handleTick() {
        OPEN_PANELS.getValues().forEach(Panel::handleTick);
    }

    public void handleRender() {
        List<ResourceLocation> panelsToRemove = new ArrayList<>();
        OPEN_PANELS.getValues().forEach(panel -> {
            if (!panel.handleRender()) {
                panelsToRemove.add(panel.getId());
            }
        });
        panelsToRemove.forEach(OPEN_PANELS::remove);
    }

    public void togglePanel(Panel panel) {
        if(checkOpen(panel)) {
            closePanel(panel);
        } else {
            openPanel(panel);
        }
    }
    public void openPanel(Panel panel) {
        if (!checkOpen(panel)) {
            OPEN_PANELS.register(panel.getId(), panel);
            panel.open();
        }
    }

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