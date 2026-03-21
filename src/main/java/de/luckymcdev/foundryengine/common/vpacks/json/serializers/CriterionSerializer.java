package de.luckymcdev.foundryengine.common.vpacks.json.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;

import java.lang.reflect.Type;

public class CriterionSerializer implements JsonSerializer<Criterion<?>> {
    @Override
    public JsonElement serialize(Criterion<?> src, Type typeOfSrc, JsonSerializationContext context) {
        RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE,
                RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        return Criterion.CODEC.encodeStart(registryOps, src).getOrThrow();
    }
}
