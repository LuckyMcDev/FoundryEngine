package de.luckymcdev.foundryengine.common.bundle;

import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import groovy.util.GroovyScriptEngine;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;

import java.util.List;

/**
 * Represents a loaded bundle.
 */
public record Bundle(
        BundleInfo info,
        BundleFiles bundleFiles,
        GroovyScriptEngine scriptEngine,
        BundleRegistryQuery registryQuery,
        IEventBus eventBus,
        IEventBus bundleBus,
        List<BundleEntrypoint> entrypoints
) {
    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(this.info.id(), path);
    }
}