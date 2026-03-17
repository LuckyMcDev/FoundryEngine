package io.github.luckymcdev.foundryengine.common.vpacks.json.state;

import com.google.gson.*;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.block.state.properties.Property;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JWhen implements Cloneable {
    private final List<List<Tuple<String, String[]>>> state = new ArrayList<>();

    @SafeVarargs
    public final <T extends Comparable<T>> JWhen add(Property<T> property, T... values) {
        String[] states = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            states[i] = property.getName(values[i]);
        }
        return this.add(property.getName(), states);
    }

    public JWhen add(String condition, String... states) {
        this.state.add(List.of(new Tuple<>(condition, states)));
        return this;
    }

    public JWhen clone() {
        try {
            return (JWhen) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class StateBuilder implements Cloneable {
        final List<Tuple<String, String[]>> state = new ArrayList<>();

        @SafeVarargs
        public final <T extends Comparable<T>> StateBuilder add(Property<T> property, T... values) {
            String[] states = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                states[i] = property.getName(values[i]);
            }
            return this.add(property.getName(), states);
        }

        public StateBuilder add(String condition, String... states) {
            this.state.add(new Tuple<>(condition, states));
            return this;
        }

        @Override
        public StateBuilder clone() {
            try {
                return (StateBuilder) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e);
            }
        }
    }

    public static class Serializer implements JsonSerializer<JWhen> {
        @Override
        public JsonElement serialize(JWhen src, Type typeOfSrc, JsonSerializationContext context) {
            if (src.state.size() == 1) {
                JsonObject jsonObject = new JsonObject();
                for (Tuple<String, String[]> stringTuple : src.state.getFirst()) {
                    jsonObject.addProperty(stringTuple.getA(), String.join("|", stringTuple.getB()));
                }
                return jsonObject;
            } else {
                JsonObject mainJson = new JsonObject();
                JsonArray array = new JsonArray();
                for (List<Tuple<String, String[]>> tuples : src.state) {
                    JsonObject jsonObject = new JsonObject();
                    for (Tuple<String, String[]> tuple : tuples) {
                        jsonObject.addProperty(tuple.getA(), String.join("|", tuple.getB()));
                    }
                    array.add(jsonObject);
                }
                mainJson.add("OR", array);
                return mainJson;
            }
        }
    }
}
