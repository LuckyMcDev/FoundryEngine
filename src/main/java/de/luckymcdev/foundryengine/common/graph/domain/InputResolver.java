package de.luckymcdev.foundryengine.common.graph.domain;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/**
 * Wraps resolved input expressions for a node during codegen.
 * <p>
 * Positional access is the default (simplest common case), but
 * UUID-keyed access is also available for nodes that need to
 * distinguish inputs by identity rather than order.
 */
public final class InputResolver {
    private final String[] positional;
    private final Map<UUID, String> byPinId;

    public InputResolver(String[] positional, Map<UUID, String> byPinId) {
        this.positional = positional;
        this.byPinId = byPinId;
    }

    public String get(int index) {
        return index < positional.length ? positional[index] : null;
    }

    public @Nullable String get(UUID pinId) {
        return byPinId.get(pinId);
    }

    public int size() {
        return positional.length;
    }

    public boolean has(int index) {
        return index < positional.length && positional[index] != null;
    }
}
