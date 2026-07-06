package de.luckymcdev.foundryengine.common.event.modification;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class TitleScreenModificationEvent extends Event implements ICancellableEvent {
	private final ButtonType buttonType;

	public TitleScreenModificationEvent(ButtonType buttonType) {
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