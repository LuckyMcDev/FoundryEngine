package de.luckymcdev.foundryengine.common.graph.registry;

import com.google.gson.JsonObject;
import de.luckymcdev.foundryengine.common.graph.type.PinType;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Describes the structure of a node type — domain-agnostic.
 * <p>
 * The actual codegen/behavior per domain is registered separately in each
 * {@link de.luckymcdev.foundryengine.common.graph.domain.GraphDomain}'s handler map.
 */
public final class NodeDefinition {
    private final Identifier id;
    private final String displayName;
    private final String category;
    private final List<PinDef> inputs;
    private final List<PinDef> outputs;
    private final JsonObject defaultData;

    public NodeDefinition(Identifier id, String displayName, String category,
                          List<PinDef> inputs, List<PinDef> outputs,
                          JsonObject defaultData) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.defaultData = defaultData != null ? defaultData.deepCopy() : new JsonObject();
    }

    public Identifier id() { return id; }
    public String displayName() { return displayName; }
    public String category() { return category; }
    public List<PinDef> inputs() { return inputs; }
    public List<PinDef> outputs() { return outputs; }
    public JsonObject defaultData() { return defaultData.deepCopy(); }

    public record PinDef(String name, PinType type, boolean required) {}

    @Override
    public String toString() {
        return "NodeDef[" + id + "]";
    }
}
