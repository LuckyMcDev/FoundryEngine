package de.luckymcdev.foundryengine.client.imgui.imnodes.blueprint;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import de.luckymcdev.foundryengine.client.imgui.imnodes.Node;
import de.luckymcdev.foundryengine.client.imgui.imnodes.NodeEditorInstance;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePin;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinConnectionType;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinInfo;
import de.luckymcdev.foundryengine.client.imgui.imnodes.pin.NodePinType;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import imgui.ImVec2;
import imgui.extension.imnodes.ImNodes;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Serializes and deserializes Blueprint graphs to/from JSON using Gson.
 */
public class BlueprintSerializer {
    private final Gson gson;
    private final BlueprintEngine engine;

    public BlueprintSerializer(BlueprintEngine engine) {
        this.engine = engine;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(SerializedBlueprint.class, new BlueprintDeserializer(engine))
                .create();
    }

    /**
     * Serializes a node editor instance to a JSON string.
     */
    public String serialize(NodeEditorInstance<?> editor) {
        List<SerializedNode> nodes = new ArrayList<>();
        List<SerializedPin> pins = new ArrayList<>();
        List<SerializedLink> links = new ArrayList<>();

        // Capture all nodes with their positions and metadata
        for (var node : editor.nodes.values()) {
            SerializedNode sn = new SerializedNode();
            sn.id = node.id;
            sn.name = node.name;
            sn.category = node.category;
            sn.outputValues = new HashMap<>(node.outputValues);

            // Capture node position in grid space
            ImVec2 pos = ImNodes.getNodeGridSpacePos(node.id);
            sn.posX = pos.x;
            sn.posY = pos.y;

            nodes.add(sn);
        }

        // Capture all pins with their metadata and default values
        for (var pin : editor.pins.values()) {
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

        // Capture all connections between pins
        for (var pin : editor.pins.values()) {
            if (pin.inputLink != null) {
                SerializedLink sl = new SerializedLink();
                sl.sourcePinId = pin.inputLink.id;
                sl.targetPinId = pin.id;
                links.add(sl);
            }
        }

        // Capture editor pan and zoom
        SerializedEditorState editorState = new SerializedEditorState();
        ImVec2 panning = ImNodes.editorContextGetPanning();
        editorState.panX = panning.x;
        editorState.panY = panning.y;

        SerializedBlueprint blueprint = new SerializedBlueprint();
        blueprint.nodes = nodes;
        blueprint.pins = pins;
        blueprint.links = links;
        blueprint.editorState = editorState;

        return gson.toJson(blueprint);
    }

    /**
     * Deserializes a JSON string back into a node editor instance.
     */
    public void deserialize(String json, NodeEditorInstance<?> editor) {
        try {
            SerializedBlueprint blueprint = gson.fromJson(json, SerializedBlueprint.class);
            if (blueprint == null) {
                throw new JsonSyntaxException("Failed to deserialize blueprint");
            }

            // Clear existing state
            editor.clear();

            Map<Integer, Node> nodeMap = new HashMap<>();
            Map<Integer, float[]> nodePositions = new HashMap<>();

            for (var sn : blueprint.nodes) {
                // Create a skeleton node
                Node node = new Node(sn.name, new ArrayList<>());
                node.id = sn.id;
                node.category = sn.category;
                node.outputValues.putAll(sn.outputValues);

                nodeMap.put(sn.id, node);
                nodePositions.put(sn.id, new float[]{sn.posX, sn.posY});
                editor.addNode(node, false);
            }

            Map<Integer, NodePinInfo> pinMap = new HashMap<>();

            for (var sp : blueprint.pins) {
                Node node = nodeMap.get(sp.nodeId);
                if (node == null) continue;

                // Resolve pin type from engine
                var pinType = resolvePinType(sp.typeName);
                if (pinType == null) continue;

                // Create NodePin and NodePinInfo
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
                editor.pins.put(sp.id, pinInfo);

                // Add to node's pin list
                if (sp.connectionType.equals("OUTPUT")) {
                    node.outputPins.add(pinInfo);
                } else {
                    node.inputPins.add(pinInfo);
                }
            }

            for (var sl : blueprint.links) {
                var srcPin = pinMap.get(sl.sourcePinId);
                var dstPin = pinMap.get(sl.targetPinId);
                if (srcPin != null && dstPin != null) {
                    dstPin.inputLink = srcPin;
                }
            }

            for (var entry : nodePositions.entrySet()) {
                int nodeId = entry.getKey();
                float[] pos = entry.getValue();
                imgui.extension.imnodes.ImNodes.setNodeGridSpacePos(nodeId, new ImVec2(pos[0], pos[1]));
            }

            if (blueprint.editorState != null) {
                imgui.extension.imnodes.ImNodes.editorContextResetPanning(
                        new ImVec2(blueprint.editorState.panX, blueprint.editorState.panY)
                );
            }

            editor.resetLastId();

        } catch (JsonSyntaxException e) {
            throw new EngineException("Failed to deserialize blueprint: " + e.getMessage(), e);
        }
    }

    /**
     * Resolves a pin type by display name from the engine's type palette.
     */
    private @Nullable NodePinType<?> resolvePinType(String typeName) {
        return switch (typeName) {
            case "Exec" -> engine.execType;
            case "Bool" -> engine.boolType;
            case "Int" -> engine.intType;
            case "Float" -> engine.floatType;
            case "String" -> engine.stringType;
            case "Object" -> engine.objectType;
            case "Any" -> engine.anyType;
            default -> null;
        };
    }

    public static class SerializedBlueprint {
        public List<SerializedNode> nodes;
        public List<SerializedPin> pins;
        public List<SerializedLink> links;
        public SerializedEditorState editorState;
    }

    public static class SerializedNode {
        public int id;
        public String name;
        public @Nullable String category;
        public float posX;
        public float posY;
        public Map<String, Object> outputValues;
    }

    public static class SerializedPin {
        public int id;
        public int nodeId;
        public String label;
        public String typeName;
        public String connectionType;
        public @Nullable Object defaultValue;
        public boolean isConnected;
    }

    public static class SerializedLink {
        public int sourcePinId;
        public int targetPinId;
    }

    public static class SerializedEditorState {
        public float panX;
        public float panY;
        // TODO: Add zoom level when ImNodes supports it
    }

    private record BlueprintDeserializer(BlueprintEngine engine) implements JsonDeserializer<SerializedBlueprint> {
        @Override
        public SerializedBlueprint deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            SerializedBlueprint blueprint = new SerializedBlueprint();
            blueprint.nodes = context.deserialize(obj.get("nodes"),
                    new TypeToken<List<SerializedNode>>() {
                    }.getType()
            );
            blueprint.pins = context.deserialize(obj.get("pins"),
                    new TypeToken<List<SerializedPin>>() {
                    }.getType()
            );
            blueprint.links = context.deserialize(obj.get("links"),
                    new TypeToken<List<SerializedLink>>() {
                    }.getType()
            );

            if (obj.has("editorState")) {
                blueprint.editorState = context.deserialize(obj.get("editorState"), SerializedEditorState.class);
            } else {
                blueprint.editorState = new SerializedEditorState();
            }

            return blueprint;
        }
    }
}