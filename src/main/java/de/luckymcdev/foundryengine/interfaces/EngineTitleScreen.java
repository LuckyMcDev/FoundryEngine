package de.luckymcdev.foundryengine.interfaces;

import de.luckymcdev.foundryengine.common.game.behavior.GameBehaviorCancellation;
import net.minecraft.client.gui.screens.TitleScreen;

/**
 * Title Screen Extension
 */
public interface EngineTitleScreen extends EngineInterface<TitleScreen> {
    GameBehaviorCancellation engine$onSingleplayerClick();

    GameBehaviorCancellation engine$onMultiplayerClick();

    GameBehaviorCancellation engine$onRealmsClick();
}