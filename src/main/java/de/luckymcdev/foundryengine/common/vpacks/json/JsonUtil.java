package de.luckymcdev.foundryengine.common.vpacks.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;

import java.util.List;

public class JsonUtil {
    public static <T> JsonArray serializeCodecList(List<T> list, Codec<T> codec) {
        JsonArray array = new JsonArray(list.size());
        for (T item : list) {
            array.add(codec.encodeStart(JsonOps.INSTANCE, item).getOrThrow());
        }
        return array;
    }

    public static <T> JsonElement serializeCodec(T value, Codec<T> codec) {
        return codec.encodeStart(JsonOps.INSTANCE, value).getOrThrow();
    }
}
