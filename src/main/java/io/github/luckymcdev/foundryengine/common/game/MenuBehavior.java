package io.github.luckymcdev.foundryengine.common.game;

import net.minecraft.client.gui.screens.Screen;

public abstract class MenuBehavior extends GameBehavior {

    public GameBehaviorCancelation onSingleplayerButtonClick(Screen currentScreen) {
        return GameBehaviorCancelation.CONTINUE;
    }

    public GameBehaviorCancelation onMultiplayerButtonClick(Screen currentScreen) {
        return GameBehaviorCancelation.CONTINUE;
    }

    public GameBehaviorCancelation onOptionsButtonClick(Screen currentScreen) {
        return GameBehaviorCancelation.CONTINUE;
    }

    public GameBehaviorCancelation onQuitButtonClick(Screen currentScreen) {
        return GameBehaviorCancelation.CONTINUE;
    }
}