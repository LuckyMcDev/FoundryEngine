package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class JSimpleComponent<T> extends AbstractJComponent {
    private final T value;

    public JSimpleComponent(T value) {
        this.value = value;
    }

    public static class Serializer implements JsonSerializer<JSimpleComponent<?>> {
        @Override
        public JsonElement serialize(JSimpleComponent<?> src, Type type, JsonSerializationContext context) {
            return context.serialize(src.value);
        }
    }
}
