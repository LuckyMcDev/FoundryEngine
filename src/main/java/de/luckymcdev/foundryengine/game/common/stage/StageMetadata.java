package de.luckymcdev.foundryengine.game.common.stage;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public record StageMetadata(
	Component displayName,
	Component description,
	List<Identifier> parents
) {
	public StageMetadata {
		parents = List.copyOf(parents);
	}

	public boolean hasParent(Identifier parent) {
		return parents.contains(parent);
	}
}
