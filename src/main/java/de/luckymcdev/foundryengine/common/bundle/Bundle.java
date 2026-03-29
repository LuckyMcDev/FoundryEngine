package de.luckymcdev.foundryengine.common.bundle;

import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleCreativeModeTab;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import groovy.util.GroovyScriptEngine;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;

import java.util.List;

/**
 * Represents a loaded bundle.
 * @param info the info for this bundle.
 * @param bundleFiles file info and zfs for this bundle
 * @param scriptEngine the script engine for this bundle.
 * @param registryQuery the registry query for this bundle.
 * @param eventBus the game event bus for this bundle, just mirrors NeoForge.EVENT_BUS
 * @param bundleBus the 'mod' event bus but just for this bundle.
 * @param entrypoints a list of entrypoints this bundle contains.
 */
public record Bundle(
        BundleInfo info,
        BundleFiles bundleFiles,
        GroovyScriptEngine scriptEngine,
        BundleRegistryQuery registryQuery,
        IEventBus eventBus,
        IEventBus bundleBus,
        List<BundleEntrypoint> entrypoints,
        BundleCreativeModeTab creativeModeTab
) {
    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(this.info.id(), path);
    }
}