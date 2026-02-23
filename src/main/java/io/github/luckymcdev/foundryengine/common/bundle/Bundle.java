package io.github.luckymcdev.foundryengine.common.bundle;

import groovy.util.GroovyScriptEngine;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleFiles;
import io.github.luckymcdev.foundryengine.common.bundle.info.BundleInfo;
import net.neoforged.bus.api.IEventBus;
import org.jspecify.annotations.Nullable;

import java.nio.file.FileSystem;

/**
 * Represents a loaded bundle.
 *
 * @param zipFileSystem The ZIP FileSystem backing this bundle's paths, or {@code null} for
 *                      folder-based bundles. Owned by this bundle — callers must close it
 *                      via BundleManager.remove() when the bundle is unloaded.
 */
public record Bundle(
        BundleInfo info,
        BundleFiles bundleFiles,
        GroovyScriptEngine scriptEngine,
        IEventBus eventBus,
        @Nullable FileSystem zipFileSystem
) {
}