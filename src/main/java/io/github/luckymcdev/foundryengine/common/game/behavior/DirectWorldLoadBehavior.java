package io.github.luckymcdev.foundryengine.common.game.behavior;

import com.mojang.logging.LogUtils;
import io.github.luckymcdev.foundryengine.common.priority.Priority;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

public class DirectWorldLoadBehavior extends MenuBehavior {
    private static final Logger LOGGER = LogUtils.getLogger();
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
            minecraft.createWorldOpenFlows().openWorld(worldName, () -> minecraft.setScreen(currentScreen));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            minecraft.setScreen(currentScreen);
        }

        return GameBehaviorCancelation.CANCEL;
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }
}