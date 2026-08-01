package de.luckymcdev.foundryengine.common.game.stage;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StageRegistry {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final GenericRegistry<Identifier, StageMetadata> registry = new GenericRegistry<>();

	public StageMetadata register(Identifier id, Component displayName, Component description, Identifier... parents) {
		var metadata = new StageMetadata(displayName, description,
			parents.length > 0 ? List.of(parents) : List.of());
		registry.register(id, metadata);
		return metadata;
	}

	public StageMetadata register(Identifier id, Component displayName, Component description, List<Identifier> parents) {
		var metadata = new StageMetadata(displayName, description, filterCyclicParents(id, parents));
		registry.register(id, metadata);
		return metadata;
	}

	/**
	 * Drops declared parents that would introduce a cycle (the new stage is transitively
	 * reachable from the declared parent) and warns instead of crashing the registration.
	 */
	private List<Identifier> filterCyclicParents(Identifier id, List<Identifier> parents) {
		List<Identifier> filtered = new ArrayList<>(parents.size());
		for (Identifier parent : parents) {
			if (parent.equals(id)) {
				LOGGER.warn("Stage [{}] cannot declare itself as a parent; ignoring self-reference", id);
				continue;
			}
			if (wouldCreateCycle(id, parent)) {
				LOGGER.warn("Stage [{}] declaring parent [{}] would create a cycle; ignoring parent", id, parent);
				continue;
			}
			filtered.add(parent);
		}
		return filtered;
	}

	/**
	 * Returns true if {@code parent} transitively depends on {@code id}, which would make
	 * declaring {@code parent} a parent of {@code id} a cycle.
	 */
	private boolean wouldCreateCycle(Identifier id, Identifier parent) {
		Set<Identifier> visited = new HashSet<>();
		Deque<Identifier> stack = new ArrayDeque<>();
		stack.addLast(parent);
		while (!stack.isEmpty()) {
			Identifier current = stack.removeLast();
			if (current.equals(id)) {
				return true;
			}
			if (!visited.add(current)) {
				continue;
			}
			stack.addAll(getParents(current));
		}
		return false;
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
