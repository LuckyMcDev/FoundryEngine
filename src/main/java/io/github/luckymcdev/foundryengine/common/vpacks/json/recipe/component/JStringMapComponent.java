package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class JStringMapComponent extends AbstractJComponent {
    private final Map<String, String> states = new HashMap<>();

    public JStringMapComponent state(String state, String value) {
        this.states.put(state, value);
        return this;
    }

    public static class Serializer implements JsonSerializer<JStringMapComponent> {
        @Override
        public JsonElement serialize(JStringMapComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(src.states);
        }
    }
}
