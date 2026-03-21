package de.luckymcdev.foundryengine.common.vpacks.json;

import com.google.gson.*;
import de.luckymcdev.foundryengine.server.packs.VirtualPackImpl;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;

public class JCondition {
    private JsonObject parameters = new JsonObject();

    public JCondition() {
    }

    public JCondition(String condition) {
        if (condition != null) {
            this.condition(condition);
        }
    }

    public JCondition condition(String condition) {
        this.parameters.addProperty("condition", condition);
        return this;
    }

    public JCondition set(JsonObject parameters) {
        parameters.addProperty("condition", this.parameters.get("condition").getAsString());
        this.parameters = parameters;
        return this;
    }

    public JCondition parameter(String key, Number value) {
        return this.parameter(key, new JsonPrimitive(value));
    }

    public JCondition parameter(String key, JsonElement value) {
        this.parameters.add(key, value);
        return this;
    }

    public JCondition parameter(String key, boolean value) {
        return this.parameter(key, new JsonPrimitive(value));
    }

    public JCondition parameter(String key, char value) {
        return this.parameter(key, new JsonPrimitive(value));
    }

    public JCondition parameter(String key, Identifier value) {
        return this.parameter(key, value.toString());
    }

    public JCondition parameter(String key, String value) {
        return this.parameter(key, new JsonPrimitive(value));
    }

    public JCondition alternative(JCondition... conditions) {
        JsonArray jsonArray = new JsonArray();
        for (JCondition condition : conditions) {
            jsonArray.add(VirtualPackImpl.GSON.toJsonTree(condition));
        }
        this.parameters.add("terms", jsonArray);
        return this;
    }

    public static class Serializer implements JsonSerializer<JCondition> {
        @Override
        public JsonElement serialize(JCondition src, Type typeOfSrc, JsonSerializationContext context) {
            return src.parameters;
        }
    }
}
