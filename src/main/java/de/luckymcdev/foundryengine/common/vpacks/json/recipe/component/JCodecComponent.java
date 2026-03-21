package de.luckymcdev.foundryengine.common.vpacks.json.recipe.component;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.lang.reflect.Type;

public class JCodecComponent<T> extends AbstractJComponent {
    private final T value;
    private final Codec<T> codec;

    public JCodecComponent(T value, Codec<T> codec) {
        this.value = value;
        this.codec = codec;
    }

    public static class Serializer<T> implements JsonSerializer<JCodecComponent<T>> {
        @Override
        public JsonElement serialize(JCodecComponent<T> src, Type type, JsonSerializationContext context) {
            return src.codec.encodeStart(JsonOps.INSTANCE, src.value).getOrThrow();
        }
    }
}
