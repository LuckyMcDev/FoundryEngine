package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JMultitypeComponent extends AbstractJComponent {
    private final List<JMultitypeComponent> listValue = new ArrayList<>();
    private final HashMap<String, JMultitypeComponent> structValue = new HashMap<>();
    private Type type;
    private Boolean booleanValue;
    private Double doubleValue;
    private String stringValue;

    public JMultitypeComponent() {
    }

    public JMultitypeComponent(boolean booleanValue) {
        this.type = Type.Boolean;
        this.booleanValue = booleanValue;
    }

    public JMultitypeComponent(double doubleValue) {
        this.type = Type.Double;
        this.doubleValue = doubleValue;
    }

    public JMultitypeComponent(String stringValue) {
        this.type = Type.String;
        this.stringValue = stringValue;
    }

    public JMultitypeComponent addListValue(JMultitypeComponent value) {
        this.checkAndSetType(Type.List);
        this.listValue.add(value);
        return this;
    }

    public JMultitypeComponent addStructValue(String key, JMultitypeComponent value) {
        this.checkAndSetType(Type.Struct);
        this.structValue.put(key, value);
        return this;
    }

    private void checkAndSetType(Type type) {
        if (this.type == null) {
            this.type = type;
        } else if (this.type != type) {
            throw new IllegalStateException(
                    "A multitype component cannot have varying types within it. You are currently trying to use the" + type + "type, but the current type is " + this.type
            );
        }
    }

    private enum Type {
        Boolean,
        Double,
        String,
        List,
        Struct
    }

    public static class Serializer implements JsonSerializer<JMultitypeComponent> {
        @Override
        public JsonElement serialize(JMultitypeComponent src, java.lang.reflect.Type type, JsonSerializationContext context) {
            return switch (src.type) {
                case Boolean -> context.serialize(src.booleanValue);
                case Double -> context.serialize(src.doubleValue);
                case String -> context.serialize(src.stringValue);
                case List -> context.serialize(src.listValue);
                case Struct -> context.serialize(src.structValue);
            };
        }
    }
}
