package io.github.luckymcdev.foundryengine.common.vpacks.json.state;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JMultipart implements Cloneable {
    private final List<JBlockModel> apply = new ArrayList<>();
    private JWhen when;

    public JMultipart() {
    }

    public JMultipart(JBlockModel... models) {
        new JMultipart().addAllModels(models);
    }

    public JMultipart when(JWhen when) {
        this.when = when;
        return this;
    }

    public JMultipart addModel(JBlockModel model) {
        this.apply.add(model);
        return this;
    }

    public JMultipart addAllModels(JBlockModel... models) {
        this.apply.addAll(List.of(models));
        return this;
    }

    @Override
    public JMultipart clone() {
        try {
            return (JMultipart) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class Serializer implements JsonSerializer<JMultipart> {
        @Override
        public JsonElement serialize(JMultipart src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            if (src.apply.size() == 1) {
                jsonObject.add("apply", context.serialize(src.apply.getFirst()));
            } else {
                jsonObject.add("apply", context.serialize(src.apply));
            }
            jsonObject.add("when", context.serialize(src.when));
            return jsonObject;
        }
    }
}
