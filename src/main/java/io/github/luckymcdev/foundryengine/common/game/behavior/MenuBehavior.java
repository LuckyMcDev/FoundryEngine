package io.github.luckymcdev.foundryengine.common.game.behavior;

import net.minecraft.client.gui.screens.Screen;

public abstract class MenuBehavior extends GameBehavior {

    public GameBehaviorCancellation onSingleplayerButtonClick(Screen currentScreen) {
        return GameBehaviorCancellation.CONTINUE;
    }

    public GameBehaviorCancellation onMultiplayerButtonClick(Screen currentScreen) {
        return GameBehaviorCancellation.CONTINUE;
    }

    public GameBehaviorCancellation onRealmsButtonClick(Screen currentScreen) {
        return GameBehaviorCancellation.CONTINUE;
    }

    public GameBehaviorCancellation onOptionsButtonClick(Screen currentScreen) {
        return GameBehaviorCancellation.CONTINUE;
    }

    public GameBehaviorCancellation onQuitButtonClick(Screen currentScreen) {
        return GameBehaviorCancellation.CONTINUE;
    }
}