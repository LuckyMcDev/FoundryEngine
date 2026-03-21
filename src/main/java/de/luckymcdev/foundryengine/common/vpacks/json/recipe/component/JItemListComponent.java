package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.luckymcdev.foundryengine.common.vpacks.json.recipe.JResult;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JItemListComponent extends AbstractJComponent {
    // JResult happens to have the same fields that this field takes, so we're just going to use it
    // until it differs.
    private final List<JResult> contents = new ArrayList<>();

    public JItemListComponent content(JResult content) {
        this.contents.add(content);
        return this;
    }

    public static class Serializer implements JsonSerializer<JItemListComponent> {
        @Override
        public JsonElement serialize(JItemListComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(src.contents);
        }
    }
}
