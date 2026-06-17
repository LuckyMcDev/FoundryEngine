package de.luckymcdev.foundryengine.common.graph.domain;

import com.google.gson.JsonElement;
import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.model.NodeModel;

import java.util.Map;
import java.util.UUID;

/**
 * Per-node handler for the JSON editing domain.
 * <p>
 * Each participating node composes its output {@link JsonElement}
 * from already-resolved input values.
 */
public interface JsonNodeHandler {

    /**
     * Compose this node's output JSON element.
     *
     * @param node the node model (carries configuration data)
     * @param graph the full graph
     * @param inputs resolved input JSON elements, keyed by pin UUID
     * @return the composed JSON output for this node
     */
    JsonElement compose(NodeModel node, GraphModel graph, Map<UUID, JsonElement> inputs);
}
