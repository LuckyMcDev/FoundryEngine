package io.github.luckymcdev.common.registry;

import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ResourceRegistry implements Registry<String, ResourceLocation> {
    private final Map<String, ResourceLocation> registryMap = new HashMap<>();

    @Override
    public void register(String id, ResourceLocation resource) {
        registryMap.put(id, resource);
    }

    @Override
    public ResourceLocation get(String id) {
        return registryMap.get(id);
    }

    @Override
    public boolean contains(String id) {
        return registryMap.containsKey(id);
    }

    @Override
    public void remove(String id) {
        registryMap.remove(id);
    }
}
