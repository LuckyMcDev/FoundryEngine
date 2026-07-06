package de.luckymcdev.foundryengine.client.command.suggest.nbt;

import com.mojang.brigadier.Message;

public record NbtSuggestionComponent(String text) implements Message {
	@Override
	public String getString() {
		return text;
	}
}