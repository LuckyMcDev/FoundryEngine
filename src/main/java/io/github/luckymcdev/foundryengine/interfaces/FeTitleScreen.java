package io.github.luckymcdev.foundryengine.interfaces;

import io.github.luckymcdev.foundryengine.common.game.behavior.GameBehaviorCancellation;

/**
 * Title Screen Extension
 */
public interface FeTitleScreen {
    GameBehaviorCancellation fe$onSingleplayerClick();

    GameBehaviorCancellation fe$onMultiplayerClick();

    GameBehaviorCancellation fe$onRealmsClick();
}