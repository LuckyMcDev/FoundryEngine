package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;

/**
 * Bundle Manager that manages Bundle Lifecylces.
 */
public class BundleManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final GenericRegistry<String, Bundle> bundles = new GenericRegistry<>();
    private final BundleDiscovery bundleDiscovery;

    public BundleManager(IEventBus modBus) {
        BundleFactory bundleFactory = new BundleFactory(modBus);
        this.bundleDiscovery = new BundleDiscovery(bundleFactory, this::register);
    }

    /**
     * Registers a bundle in the registry.
     */
    public void register(Bundle bundle) {
        bundles.register(bundle.info().id(), bundle);
        LOGGER.debug("Registered Bundle: {} with Info: {}", bundle.info().id(), bundle.info());
    }

    /**
     * Removes and cleans up a bundle, including closing its ZIP FileSystem if applicable.
     */
    public void remove(Bundle bundle) {
        bundles.remove(bundle.info().id());
        closeFileSystem(bundle);
    }

    /**
     * Discovers and loads all bundles from the specified directory.
     */
    public void discover(Path directory) throws IOException {
        bundleDiscovery.discover(directory);
    }

    /**
     * Returns all currently loaded bundles.
     */
    public Iterable<Bundle> getBundles() {
        return bundles.values();
    }

    /**
     * Retrieves a bundle by its ID.
     */
    public Bundle getBundle(String id) {
        return bundles.get(id);
    }

    /**
     * Reloads all bundles by unloading current bundles and rediscovering them.
     */
    public void reload() {
        LOGGER.info("Reloading FoundryEngine Bundles...");

        unloadAllBundles();
        bundles.clear();

        try {
            discover(Common.BUNDLES);
        } catch (IOException e) {
            LOGGER.error("Failed to reload bundles", e);
        }
    }

    /**
     * Unloads all bundles, calling their onUnload methods and closing resources.
     */
    private void unloadAllBundles() {
        for (Bundle bundle : bundles.values()) {
            unloadBundle(bundle);
        }
    }

    /**
     * Unloads a single bundle by calling onUnload on all entrypoints
     * and closing the ZIP FileSystem if present.
     */
    private void unloadBundle(Bundle bundle) {
        for (BundleEntrypoint entrypoint : bundle.entrypoints()) {
            try {
                entrypoint.onUnload();
            } catch (Exception e) {
                LOGGER.error("Error unloading script in bundle {}", bundle.info().id(), e);
            }
        }

        closeFileSystem(bundle);
    }

    /**
     * Closes the ZIP FileSystem associated with a bundle, if present.
     */
    private void closeFileSystem(Bundle bundle) {
        FileSystem fs = bundle.bundleFiles().zipFileSystem();
        if (fs != null && fs.isOpen()) {
            try {
                fs.close();
            } catch (IOException e) {
                LOGGER.warn("Failed to close ZIP FileSystem for bundle '{}': {}",
                        bundle.info().id(), e.getLocalizedMessage());
            }
        }
    }

    public BundleDiscovery getBundleDiscovery() {
        return bundleDiscovery;
    }

    @Override
    public void onResourceManagerReload(@NonNull ResourceManager resourceManager) {
        this.reload();
    }
}