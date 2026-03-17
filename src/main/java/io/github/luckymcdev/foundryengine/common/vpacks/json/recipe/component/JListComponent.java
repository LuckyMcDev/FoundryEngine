package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JListComponent<T> extends AbstractJComponent {
    protected final List<T> list = new ArrayList<>();

    public void addItem(T item) {
        this.list.add(item);
    }

    public static class Serializer implements JsonSerializer<JListComponent<?>> {
        @Override
        public JsonElement serialize(JListComponent<?> src, Type type, JsonSerializationContext context) {
            return context.serialize(src.list);
        }
    }
}
