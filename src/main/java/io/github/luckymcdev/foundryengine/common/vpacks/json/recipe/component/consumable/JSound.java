package io.github.luckymcdev.foundryengine.common.vpacks.json.recipe.component.consumable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.resources.Identifier;

import java.lang.reflect.Type;

public class JSound {
    private final Identifier sound;
    private Integer range;

    public JSound(Identifier sound) {
        this.sound = sound;
    }

    public JSound range(int range) {
        this.range = range;
        return this;
    }

    public static class Serializer implements JsonSerializer<JSound> {
        @Override
        public JsonElement serialize(JSound src, Type type, JsonSerializationContext context) {
            if (src.range == null) {
                return context.serialize(src.sound);
            } else {
                JsonObject jsonObject = new JsonObject();
                jsonObject.add("sound_id", context.serialize(src.sound));
                jsonObject.add("range", context.serialize(src.range));
                return jsonObject;
            }
        }
    }
}
