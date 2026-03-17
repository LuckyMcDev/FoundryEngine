package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import io.github.luckymcdev.foundryengine.common.vpacks.json.PresetColor;

import java.lang.reflect.Type;

public class JColorComponent extends AbstractJComponent {
    private final PresetColor color;

    public JColorComponent(PresetColor color) {
        this.color = color;
    }

    public static class Serializer implements JsonSerializer<JColorComponent> {
        @Override
        public JsonElement serialize(JColorComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(src.color);
        }
    }
}
