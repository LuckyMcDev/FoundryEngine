package io.github.luckymcdev.foundryengine.interfaces;

import io.github.luckymcdev.foundryengine.common.game.GameBehaviorCancelation;

public interface FeTitleScreen {
    GameBehaviorCancelation fe$onSingleplayerClick();

    GameBehaviorCancelation fe$onMultiplayerClick();

    GameBehaviorCancelation fe$onRealmsClick();
}