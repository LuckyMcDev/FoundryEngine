package de.luckymcdev.foundryengine.common.game.behavior;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.priority.Priority;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.slf4j.Logger;

/**
 * A Game Behavior, which makes you load into a specific world by default.
 * <br>
 * THIS WILL MAYBE BE CHANGED TO BE JUST EVENTS! USE WITH CAUTION
 */
public class DirectWorldLoadBehavior extends MenuBehavior {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final String worldName;

    public DirectWorldLoadBehavior(String worldName) {
        this.worldName = worldName;
        this.enabled = false;
    }

    @Override
    public GameBehaviorCancellation onSingleplayerButtonClick(Screen currentScreen) {
        Minecraft minecraft = Minecraft.getInstance();

        minecraft.setScreen(null);

        try {
            minecraft.createWorldOpenFlows().openWorld(worldName, () -> minecraft.setScreen(currentScreen));
        } catch (Exception e) {
            LOGGER.error(e.getLocalizedMessage());
            minecraft.setScreen(currentScreen);
        }

        return GameBehaviorCancellation.CANCEL;
    }

    @Override
    public Priority getPriority() {
        return Priority.HIGHEST;
    }
}