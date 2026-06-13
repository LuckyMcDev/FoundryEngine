package de.luckymcdev.foundryengine.common.blueprint.serial;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintTypes;
import de.luckymcdev.foundryengine.common.blueprint.event.BuiltinNodes;
import de.luckymcdev.foundryengine.common.blueprint.graph.*;
import de.luckymcdev.foundryengine.common.blueprint.serial.model.SerializedBlueprint;
import de.luckymcdev.foundryengine.common.blueprint.serial.model.SerializedLink;
import de.luckymcdev.foundryengine.common.blueprint.serial.model.SerializedNode;
import de.luckymcdev.foundryengine.common.blueprint.serial.model.SerializedPin;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes and deserializes {@link BlueprintGraph} instances to/from JSON.
 * Also provides file-based save/load.
 */
public class BlueprintSerializer {
    public static final String EXTENSION = ".febp";
    private static final Logger LOGGER = LoggerFactory.getLogger(BlueprintSerializer.class);
    private final Gson gson;
    private final BlueprintEngine engine;

    public BlueprintSerializer(BlueprintEngine engine) {
        this.engine = engine;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(SerializedBlueprint.class, new BlueprintDeserializer(engine))
                .create();
    }

    private static @Nullable NodePinType<?> resolvePinType(String typeName) {
        return BlueprintTypes.byName(typeName);
    }

    /**
     * Serializes {@code graph} to a JSON string.
     * <p>
     * Node positions are NOT included here; they are purely a client-editor
     * concern. Use {@link #serialize(BlueprintGraph, NodePositionProvider)} if
     * you also want to persist layout.
     */
    public String serialize(BlueprintGraph graph) {
        return serialize(graph, (id) -> new float[]{0f, 0f});
    }

    /**
     * Serializes {@code graph} to JSON, using {@code positions} to supply each
     * node's grid-space position.
     */
    public String serialize(BlueprintGraph graph, NodePositionProvider positions) {
        List<SerializedNode> nodes = new ArrayList<>();
        List<SerializedPin> pins = new ArrayList<>();
        List<SerializedLink> links = new ArrayList<>();

        for (var node : graph.nodes.values()) {
            SerializedNode sn = new SerializedNode();
            sn.id = node.id;
            sn.identifier = node.identifier;
            sn.name = node.name;
            sn.category = node.category;
            sn.outputValues = new HashMap<>(node.outputValues);
            sn.data = node.data.isEmpty() ? null : new HashMap<>(node.data);
            float[] pos = positions.get(node.id);
            sn.posX = pos[0];
            sn.posY = pos[1];
            nodes.add(sn);
        }

        for (var pin : graph.pins.values()) {
            SerializedPin sp = new SerializedPin();
            sp.id = pin.id;
            sp.nodeId = pin.node.id;
            sp.label = pin.pin.label();
            sp.typeName = pin.pin.type().displayName;
            sp.connectionType = pin.pin.connectionType().name();
            sp.defaultValue = pin.defaultValue;
            sp.isConnected = pin.isConnected();
            pins.add(sp);
        }

        for (var pin : graph.pins.values()) {
            if (pin.inputLink != null) {
                SerializedLink sl = new SerializedLink();
                sl.sourcePinId = pin.inputLink.id;
                sl.targetPinId = pin.id;
                links.add(sl);
            }
        }

        SerializedBlueprint bp = new SerializedBlueprint();
        bp.nodes = nodes;
        bp.pins = pins;
        bp.links = links;
        return gson.toJson(bp);
    }

    /**
     * Deserializes JSON into {@code graph}, clearing its existing state first.
     *
     * @return a map of {@code nodeId → [x, y]} for callers that want to restore
     * visual positions (e.g. the client editor).
     */
    public Map<Integer, float[]> deserialize(String json, BlueprintGraph graph) {
        try {
            SerializedBlueprint bp = gson.fromJson(json, SerializedBlueprint.class);
            if (bp == null) throw new JsonSyntaxException("null blueprint");

            graph.clear();

            Map<Integer, BlueprintNode> nodeMap = new HashMap<>();
            Map<Integer, float[]> nodePositions = new HashMap<>();

            for (var sn : bp.nodes) {
                BlueprintNode node = new BlueprintNode(sn.name, new ArrayList<>());
                node.id = sn.id;
                String rawId = (sn.identifier != null && !sn.identifier.isBlank()) ? sn.identifier : sn.name;
                String migrated = BuiltinNodes.idFromLegacyName(rawId);
                node.identifier = migrated != null ? migrated : rawId;
                node.category = sn.category;
                node.outputValues.putAll(sn.outputValues);
                if (sn.data != null) node.data.putAll(sn.data);
                nodeMap.put(sn.id, node);
                nodePositions.put(sn.id, new float[]{sn.posX, sn.posY});
                graph.addNode(node, false);
            }

            Map<Integer, NodePinInfo> pinMap = new HashMap<>();

            for (var sp : bp.pins) {
                BlueprintNode node = nodeMap.get(sp.nodeId);
                if (node == null) continue;

                var pinType = resolvePinType(sp.typeName);
                if (pinType == null) continue;

                var nodePin = new NodePin(
                        pinType,
                        sp.label,
                        NodePinConnectionType.valueOf(sp.connectionType),
                        pinType.defaultShape
                );

                var pinInfo = new NodePinInfo(node, nodePin);
                pinInfo.id = sp.id;
                pinInfo.defaultValue = sp.defaultValue;

                pinMap.put(sp.id, pinInfo);
                graph.pins.put(sp.id, pinInfo);

                if ("OUTPUT".equals(sp.connectionType)) {
                    node.outputPins.add(pinInfo);
                } else {
                    node.inputPins.add(pinInfo);
                }
            }

            for (var sl : bp.links) {
                var srcPin = pinMap.get(sl.sourcePinId);
                var dstPin = pinMap.get(sl.targetPinId);
                if (srcPin != null && dstPin != null) dstPin.inputLink = srcPin;
            }

            graph.resetLastId();
            return nodePositions;

        } catch (JsonSyntaxException e) {
            throw new EngineException("Failed to deserialize blueprint: " + e.getMessage(), e);
        }
    }

    /**
     * Saves the given graph to a file, including node positions.
     *
     * @param graph     the graph to save
     * @param positions provider for node positions
     * @param file      the target file path
     * @throws IOException if an I/O error occurs
     */
    public void saveToFile(BlueprintGraph graph, NodePositionProvider positions, Path file) throws IOException {
        String json = serialize(graph, positions);
        Files.createDirectories(file.getParent());
        Files.writeString(file, json);
        LOGGER.info("Blueprint saved to {}", file);
    }

    /**
     * Loads a blueprint from a file into the given graph.
     *
     * @param file  the source file path
     * @param graph the graph to populate (cleared before loading)
     * @return a map of {@code nodeId → [x, y]} for restoring visual positions
     * @throws IOException if an I/O error occurs or the file cannot be read
     */
    public Map<Integer, float[]> loadFromFile(Path file, BlueprintGraph graph) throws IOException {
        String json = Files.readString(file);
        return deserialize(json, graph);
    }

    /**
     * Supplies grid-space position for a node by id. Client editor provides ImNodes coords.
     */
    @FunctionalInterface
    public interface NodePositionProvider {
        float[] get(int nodeId);
    }

    private record BlueprintDeserializer(BlueprintEngine engine)
            implements JsonDeserializer<SerializedBlueprint> {

        @Override
        public SerializedBlueprint deserialize(JsonElement json, Type typeOfT,
                                               JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            SerializedBlueprint bp = new SerializedBlueprint();
            bp.nodes = context.deserialize(obj.get("nodes"), new TypeToken<List<SerializedNode>>() {
            }.getType());
            bp.pins = context.deserialize(obj.get("pins"), new TypeToken<List<SerializedPin>>() {
            }.getType());
            bp.links = context.deserialize(obj.get("links"), new TypeToken<List<SerializedLink>>() {
            }.getType());
            return bp;
        }
    }
}
