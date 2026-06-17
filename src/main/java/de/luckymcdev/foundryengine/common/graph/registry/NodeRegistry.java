package de.luckymcdev.foundryengine.common.graph.registry;

import net.minecraft.resources.Identifier;

import java.util.*;

/**
 * Global registry of node <em>structure</em> definitions.
 * <p>
 * This is shared across all domains. A domain decides which definitions
 * it supports by registering handlers for them — if a definition has
 * no handler in a given domain, it simply won't appear in that domain's
 * editor palette.
 */
public final class NodeRegistry {
    public static final NodeRegistry INSTANCE = new NodeRegistry();

    private final Map<Identifier, NodeDefinition> definitions = new LinkedHashMap<>();

    public void register(NodeDefinition def) {
        definitions.put(def.id(), def);
    }

    public NodeDefinition get(Identifier id) {
        return definitions.get(id);
    }

    public Collection<NodeDefinition> all() {
        return Collections.unmodifiableCollection(definitions.values());
    }

    public List<NodeDefinition> byCategory(String category) {
        return definitions.values().stream()
                .filter(d -> d.category().equals(category))
                .toList();
    }

    public Set<String> categories() {
        return definitions.values().stream()
                .map(NodeDefinition::category)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    public int size() {
        return definitions.size();
    }
}
