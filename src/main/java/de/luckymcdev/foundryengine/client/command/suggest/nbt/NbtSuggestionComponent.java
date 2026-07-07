package de.luckymcdev.foundryengine.client.command.suggest.nbt;

import com.mojang.brigadier.Message;

public record NbtSuggestionComponent(String text, int priority) implements Message {
	public NbtSuggestionComponent(String text) {
		this(text, SuggestionData.Entry.NORMAL);
	}

	@Override
	public String getString() {
		return text;
	}
}