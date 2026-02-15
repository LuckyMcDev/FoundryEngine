package io.github.luckymcdev.foundryengine.common.registry;

import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ResourceRegistry implements Registry<String, Identifier> {
    private final Map<String, Identifier> registryMap = new HashMap<>();

    @Override
    public void register(String id, Identifier resource) {
        registryMap.put(id, resource);
    }

    @Override
    public Identifier get(String id) {
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
