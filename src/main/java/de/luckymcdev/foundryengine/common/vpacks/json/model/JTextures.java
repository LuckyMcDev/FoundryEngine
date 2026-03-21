package de.luckymcdev.foundryengine.common.vpacks.json.model;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JTextures {
    private final Map<String, String> textures = new HashMap<>();

    public JTextures add(String name, String value) {
        this.textures.put(name, value);
        return this;
    }

    public JTextures particle(String value) {
        this.textures.put("particle", value);
        return this;
    }

    public JTextures layer0(String value) {
        this.textures.put("layer0", value);
        return this;
    }

    public JTextures layer1(String value) {
        this.textures.put("layer1", value);
        return this;
    }

    public JTextures layer2(String value) {
        this.textures.put("layer2", value);
        return this;
    }

    public JTextures layer3(String value) {
        this.textures.put("layer3", value);
        return this;
    }

    public JTextures layer4(String value) {
        this.textures.put("layer4", value);
        return this;
    }

    @Override
    public JTextures clone() {
        try {
            return (JTextures) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class Serializer implements JsonSerializer<JTextures> {
        @Override
        public JsonElement serialize(JTextures src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            src.textures.forEach(jsonObject::addProperty);
            return jsonObject;
        }
    }
}
