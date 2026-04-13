package de.luckymcdev.foundryengine.common.bundle;

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig;
import de.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import de.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleCreativeModeTab;
import de.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import de.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import groovy.util.GroovyScriptEngine;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class Bundle {
    private final BundleInfo info;
    private final BundleFiles bundleFiles;
    private final GroovyScriptEngine scriptEngine;
    private final BundleRegistryQuery registryQuery;
    private final BundleCreativeModeTab creativeModeTab;
    private final BundleConfig bundleConfig;
    private final List<BundleEntrypoint> commonEntrypoints = new ArrayList<>();
    private final List<BundleEntrypoint> clientEntrypoints = new ArrayList<>();
    private final List<BundleEntrypoint> serverEntrypoints = new ArrayList<>();

    public Bundle(BundleInfo info, BundleFiles bundleFiles, GroovyScriptEngine scriptEngine,
                  BundleRegistryQuery registryQuery, IEventBus eventBus,
                  BundleCreativeModeTab creativeModeTab, BundleConfig bundleConfig) {
        this.info = info;
        this.bundleFiles = bundleFiles;
        this.scriptEngine = scriptEngine;
        this.registryQuery = registryQuery;
        this.creativeModeTab = creativeModeTab;
        this.bundleConfig = bundleConfig;
    }

    public void loadCommon(BundleScriptLoader loader) {
        commonEntrypoints.addAll(loader.loadCommon(bundleFiles, scriptEngine));
    }

    public void loadClient(BundleScriptLoader loader) {
        clientEntrypoints.addAll(loader.loadClient(bundleFiles, scriptEngine));
    }

    public void loadServer(BundleScriptLoader loader) {
        serverEntrypoints.addAll(loader.loadServer(bundleFiles, scriptEngine));
    }

    /**
     * Returns all loaded entrypoints across all environments.
     * Used by BundleManager during unload.
     */
    public List<BundleEntrypoint> entrypoints() {
        return Stream.of(commonEntrypoints, clientEntrypoints, serverEntrypoints)
                .flatMap(List::stream)
                .toList();
    }

    public List<BundleEntrypoint> commonEntrypoints() {
        return Collections.unmodifiableList(commonEntrypoints);
    }

    public List<BundleEntrypoint> clientEntrypoints() {
        return Collections.unmodifiableList(clientEntrypoints);
    }

    public List<BundleEntrypoint> serverEntrypoints() {
        return Collections.unmodifiableList(serverEntrypoints);
    }

    public void unload() {
        entrypoints().forEach(BundleEntrypoint::onUnload);
        commonEntrypoints.clear();
        clientEntrypoints.clear();
        serverEntrypoints.clear();
    }


    public BundleInfo info() {
        return info;
    }

    public BundleFiles bundleFiles() {
        return bundleFiles;
    }

    public BundleConfig bundleConfig() {
        return bundleConfig;
    }

    public GroovyScriptEngine scriptEngine() {
        return scriptEngine;
    }

    public BundleRegistryQuery registryQuery() {
        return registryQuery;
    }

    public BundleCreativeModeTab creativeModeTab() {
        return creativeModeTab;
    }


    public Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(info.id(), path);
    }
}