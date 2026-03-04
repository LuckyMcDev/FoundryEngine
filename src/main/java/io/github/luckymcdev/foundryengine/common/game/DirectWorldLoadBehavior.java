package io.github.luckymcdev.foundryengine.common.game;

import io.github.luckymcdev.foundryengine.common.priority.Priority;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public class DirectWorldLoadBehavior extends MenuBehavior {

    private final String worldName;

    public DirectWorldLoadBehavior(String worldName) {
        this.worldName = worldName;
        this.enabled = true;
    }

    @Override
    public GameBehaviorCancelation onSingleplayerButtonClick(Screen currentScreen) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.setScreen(null);

        try {
            minecraft.createWorldOpenFlows().openWorld(worldName, () -> {
                minecraft.setScreen(currentScreen);
            });
        } catch (Exception e) {
            e.printStackTrace();
            minecraft.setScreen(currentScreen);
        }

        return GameBehaviorCancelation.CANCEL;
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }
}