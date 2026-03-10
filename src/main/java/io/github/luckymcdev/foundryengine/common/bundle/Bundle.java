package io.github.luckymcdev.foundryengine.common.bundle;

import groovy.util.GroovyScriptEngine;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import io.github.luckymcdev.foundryengine.common.bundle.registry.BundleRegistryQuery;
import io.github.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import net.neoforged.bus.api.IEventBus;
import org.jspecify.annotations.Nullable;

import java.nio.file.FileSystem;
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
        List<BundleEntrypoint> entrypoints,
        @Nullable FileSystem zipFileSystem
) {
}