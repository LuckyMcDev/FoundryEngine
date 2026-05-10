package de.luckymcdev.foundryengine.common.event;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class TitleScreenModifyEvent extends Event implements ICancellableEvent {
    private final ButtonType buttonType;

    public TitleScreenModifyEvent(ButtonType buttonType) {
        this.buttonType = buttonType;
    }

    public ButtonType getButtonType() {
        return buttonType;
    }

    public enum ButtonType {
        SINGLEPLAYER,
        MULTIPLAYER,
        REALMS
    }
}