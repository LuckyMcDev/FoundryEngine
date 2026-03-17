package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.HashMap;

public class JCustomDataComponent extends AbstractJComponent {
    private final HashMap<String, JMultitypeComponent> data = new HashMap<>();

    public JCustomDataComponent data(String key, JMultitypeComponent data) {
        this.data.put(key, data);
        return this;
    }

    public static class Serializer implements JsonSerializer<JCustomDataComponent> {
        @Override
        public JsonElement serialize(JCustomDataComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(src.data);
        }
    }
}
