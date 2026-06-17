package de.luckymcdev.foundryengine.common.graph.serial;

import com.google.gson.*;
import de.luckymcdev.foundryengine.common.graph.model.GraphModel;
import de.luckymcdev.foundryengine.common.graph.model.LinkModel;
import de.luckymcdev.foundryengine.common.graph.model.NodeModel;
import de.luckymcdev.foundryengine.common.graph.model.PinDirection;
import de.luckymcdev.foundryengine.common.graph.model.PinModel;
import de.luckymcdev.foundryengine.common.graph.type.NodePinShapeRef;
import de.luckymcdev.foundryengine.common.graph.type.PinType;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;
import java.util.*;

/**
 * Gson-based serializer for {@link GraphModel} to/from {@code .fgraph} JSON.
 */
public final class GraphSerializer {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(GraphModel.class, new GraphModelAdapter())
            .registerTypeAdapter(NodeModel.class, new NodeModelAdapter())
            .registerTypeAdapter(PinModel.class, new PinModelAdapter())
            .registerTypeAdapter(LinkModel.class, new LinkModelAdapter())
            .registerTypeAdapter(PinType.class, new PinTypeAdapter())
            .setPrettyPrinting()
            .create();

    public static String toJson(GraphModel graph) {
        return GSON.toJson(graph);
    }

    public static GraphModel fromJson(String json) {
        return GSON.fromJson(json, GraphModel.class);
    }

    public static JsonObject toJsonTree(GraphModel graph) {
        return GSON.toJsonTree(graph).getAsJsonObject();
    }

    private static final class GraphModelAdapter implements JsonSerializer<GraphModel>, JsonDeserializer<GraphModel> {
        @Override
        public JsonElement serialize(GraphModel src, Type typeOfSrc, JsonSerializationContext ctx) {
            var obj = new JsonObject();
            obj.addProperty("id", src.id().toString());
            obj.addProperty("domain", src.domain().toString());
            var nodes = new JsonArray();
            for (var node : src.nodes().values()) {
                nodes.add(ctx.serialize(node));
            }
            obj.add("nodes", nodes);
            var links = new JsonArray();
            for (var link : src.links().values()) {
                links.add(ctx.serialize(link));
            }
            obj.add("links", links);
            return obj;
        }

        @Override
        public GraphModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            var obj = json.getAsJsonObject();
            var id = UUID.fromString(obj.get("id").getAsString());
            var domain = Identifier.parse(obj.get("domain").getAsString());
            var nodes = new ArrayList<NodeModel>();
            for (var n : obj.getAsJsonArray("nodes")) {
                nodes.add(ctx.deserialize(n, NodeModel.class));
            }
            var links = new ArrayList<LinkModel>();
            for (var l : obj.getAsJsonArray("links")) {
                links.add(ctx.deserialize(l, LinkModel.class));
            }
            return new GraphModel(id, domain, nodes, links);
        }
    }

    private static final class NodeModelAdapter implements JsonSerializer<NodeModel>, JsonDeserializer<NodeModel> {
        @Override
        public JsonElement serialize(NodeModel src, Type typeOfSrc, JsonSerializationContext ctx) {
            var obj = new JsonObject();
            obj.addProperty("id", src.id().toString());
            obj.addProperty("type", src.typeRef().toString());
            obj.addProperty("x", src.x());
            obj.addProperty("y", src.y());
            var pins = new JsonArray();
            for (var pin : src.pins()) {
                pins.add(ctx.serialize(pin));
            }
            obj.add("pins", pins);
            obj.add("data", src.data());
            return obj;
        }

        @Override
        public NodeModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            var obj = json.getAsJsonObject();
            var id = UUID.fromString(obj.get("id").getAsString());
            var typeRef = Identifier.parse(obj.get("type").getAsString());
            var x = obj.get("x").getAsFloat();
            var y = obj.get("y").getAsFloat();
            var pins = new ArrayList<PinModel>();
            for (var p : obj.getAsJsonArray("pins")) {
                pins.add(ctx.deserialize(p, PinModel.class));
            }
            var data = obj.has("data") ? obj.getAsJsonObject("data") : new JsonObject();
            return new NodeModel(id, typeRef, x, y, pins, data);
        }
    }

    private static final class PinModelAdapter implements JsonSerializer<PinModel>, JsonDeserializer<PinModel> {
        @Override
        public JsonElement serialize(PinModel src, Type typeOfSrc, JsonSerializationContext ctx) {
            var obj = new JsonObject();
            obj.addProperty("id", src.id().toString());
            obj.addProperty("type", src.type().id().toString());
            obj.addProperty("label", src.label());
            obj.addProperty("direction", src.direction().name());
            obj.addProperty("index", src.index());
            return obj;
        }

        @Override
        public PinModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            var obj = json.getAsJsonObject();
            var id = UUID.fromString(obj.get("id").getAsString());
            var type = resolvePinType(Identifier.parse(obj.get("type").getAsString()));
            var label = obj.get("label").getAsString();
            var direction = PinDirection.valueOf(obj.get("direction").getAsString());
            var index = obj.get("index").getAsInt();
            return new PinModel(id, type, label, direction, index);
        }
    }

    private static final class LinkModelAdapter implements JsonSerializer<LinkModel>, JsonDeserializer<LinkModel> {
        @Override
        public JsonElement serialize(LinkModel src, Type typeOfSrc, JsonSerializationContext ctx) {
            var obj = new JsonObject();
            obj.addProperty("id", src.id().toString());
            obj.addProperty("from", src.fromPin().toString());
            obj.addProperty("to", src.toPin().toString());
            return obj;
        }

        @Override
        public LinkModel deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            var obj = json.getAsJsonObject();
            var id = UUID.fromString(obj.get("id").getAsString());
            var from = UUID.fromString(obj.get("from").getAsString());
            var to = UUID.fromString(obj.get("to").getAsString());
            return new LinkModel(id, from, to);
        }
    }

    private static final class PinTypeAdapter implements JsonSerializer<PinType>, JsonDeserializer<PinType> {
        @Override
        public JsonElement serialize(PinType src, Type typeOfSrc, JsonSerializationContext ctx) {
            return new JsonPrimitive(src.id().toString());
        }

        @Override
        public PinType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            return resolvePinType(Identifier.parse(json.getAsString()));
        }
    }

    private static PinType resolvePinType(Identifier id) {
        if (id.equals(PinType.EXEC.id())) return PinType.EXEC;
        if (id.equals(PinType.BOOL.id())) return PinType.BOOL;
        if (id.equals(PinType.INT.id())) return PinType.INT;
        if (id.equals(PinType.FLOAT.id())) return PinType.FLOAT;
        if (id.equals(PinType.STRING.id())) return PinType.STRING;
        if (id.equals(PinType.DOUBLE.id())) return PinType.DOUBLE;
        if (id.equals(PinType.VEC2.id())) return PinType.VEC2;
        if (id.equals(PinType.VEC3.id())) return PinType.VEC3;
        if (id.equals(PinType.VEC4.id())) return PinType.VEC4;
        if (id.equals(PinType.MAT4.id())) return PinType.MAT4;
        if (id.equals(PinType.SAMPLER.id())) return PinType.SAMPLER;
        if (id.equals(PinType.JSON_OBJECT.id())) return PinType.JSON_OBJECT;
        if (id.equals(PinType.JSON_ARRAY.id())) return PinType.JSON_ARRAY;
        if (id.equals(PinType.JSON_VALUE.id())) return PinType.JSON_VALUE;
        if (id.equals(PinType.ANY.id())) return PinType.ANY;
        throw new IllegalArgumentException("Unknown PinType: " + id);
    }
}
