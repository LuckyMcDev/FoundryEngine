package de.luckymcdev.foundryengine.common.vpacks.json.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.lang.reflect.Type;

public class CodecSerializer<T> implements JsonSerializer<T> {
    private final Codec<T> codec;

    public CodecSerializer(Codec<T> codec) {
        this.codec = codec;
    }

    @Override
    public JsonElement serialize(T src, Type typeOfSrc, JsonSerializationContext context) {
        return codec.encodeStart(JsonOps.INSTANCE, src).getOrThrow();
    }
}
