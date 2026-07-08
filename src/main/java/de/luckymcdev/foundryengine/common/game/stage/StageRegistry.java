package de.luckymcdev.foundryengine.common.game.stage;

import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Collection;
import java.util.List;

public class StageRegistry {
	private final GenericRegistry<Identifier, StageMetadata> registry = new GenericRegistry<>();

	public StageMetadata register(Identifier id, Component displayName, Component description, Identifier... parents) {
		var metadata = new StageMetadata(displayName, description,
			parents.length > 0 ? List.of(parents) : List.of());
		registry.register(id, metadata);
		return metadata;
	}

	public StageMetadata register(Identifier id, Component displayName, Component description, List<Identifier> parents) {
		var metadata = new StageMetadata(displayName, description, parents);
		registry.register(id, metadata);
		return metadata;
	}

	public boolean contains(Identifier id) {
		return registry.contains(id);
	}

	public StageMetadata get(Identifier id) {
		return registry.get(id);
	}

	public Collection<Identifier> getStages() {
		return registry.keys();
	}

	public boolean hasParents(Identifier id) {
		var meta = registry.get(id);
		return meta != null && !meta.parents().isEmpty();
	}

	public List<Identifier> getParents(Identifier id) {
		var meta = registry.get(id);
		return meta != null ? meta.parents() : List.of();
	}

	public void clear() {
		registry.clear();
	}
}
