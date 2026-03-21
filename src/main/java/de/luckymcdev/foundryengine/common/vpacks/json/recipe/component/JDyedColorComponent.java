package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class JDyedColorComponent extends AbstractJComponent {
    private final List<Integer> colorPieces = new ArrayList<>(3);
    private Integer color;

    public JDyedColorComponent(int color) {
        this.color = color;
    }

    public JDyedColorComponent(int r, int g, int b) {
        this.colorPieces.add(r);
        this.colorPieces.add(g);
        this.colorPieces.add(b);
    }

    public static class Serializer implements JsonSerializer<JDyedColorComponent> {
        @Override
        public JsonElement serialize(JDyedColorComponent src, Type type, JsonSerializationContext context) {
            return context.serialize(Objects.requireNonNullElseGet(src.color, () -> src.colorPieces));
        }
    }
}
