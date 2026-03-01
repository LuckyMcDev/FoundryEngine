package io.github.luckymcdev.foundryengine.common.bundle.registry;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.stream.Collectors;

public class BundleRegistry {
    private final String bundleId;

    public BundleRegistry(String bundleId) {
        this.bundleId = bundleId;
    }

    /**
     * Gets all blocks registered under this bundle's namespace.
     */
    public List<Block> getBundleBlocks() {
        return BuiltInRegistries.BLOCK.stream()
                .filter(block -> BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(bundleId))
                .collect(Collectors.toList());
    }

    /**
     * Gets all items registered under this bundle's namespace.
     */
    public List<Item> getBundleItems() {
        return BuiltInRegistries.ITEM.stream()
                .filter(item -> BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(bundleId))
                .collect(Collectors.toList());
    }
}