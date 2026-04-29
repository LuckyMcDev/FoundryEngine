package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.ClientEvents;
import de.luckymcdev.foundryengine.api.event.ServerEvents;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Collection;

/**
 * Bundle Manager that manages Bundle Lifecycles.
 *
 * <p>Script loading is sided:
 * <ul>
 *   <li>{@link #register(Bundle)} always loads <em>common</em> scripts.</li>
 *   <li>{@link #loadClientScripts()} must be called from the client dist entrypoint.</li>
 *   <li>{@link #loadServerScripts()} must be called from the server dist entrypoint.</li>
 * </ul>
 */
public class BundleManager implements ResourceManagerReloadListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final GenericRegistry<String, Bundle> bundles = new GenericRegistry<>();
    private final BundleDiscovery bundleDiscovery;
    private final BundleScriptLoader scriptLoader;

    public BundleManager(IEventBus modBus, Path configDirectory) {
        BundleFactory bundleFactory = new BundleFactory(modBus, configDirectory);
        this.scriptLoader = bundleFactory.getScriptLoader();
        this.bundleDiscovery = new BundleDiscovery(bundleFactory, this::register);
    }

    /**
     * Registers a bundle and immediately loads its common-side scripts.
     */
    public void register(Bundle bundle) {
        bundles.register(bundle.info().id(), bundle);
        bundle.loadCommon(scriptLoader);
        Common.getBlueprintManager().loadBlueprintsForBundle(bundle);
        LOGGER.debug("Registered Bundle: {} with Info: {}", bundle.info().id(), bundle.info());
    }

    /**
     * Loads client-side scripts for all registered bundles.
     * Call this from {@code FoundryEngineModClient} during client setup.
     */
    public void loadClientScripts() {
        for (Bundle bundle : bundles.values()) {
            bundle.loadClient(scriptLoader);
        }
    }

    /**
     * Loads server-side scripts for all registered bundles.
     * Call this from {@code FoundryEngineModServer} during server setup.
     */
    public void loadServerScripts() {
        for (Bundle bundle : bundles.values()) {
            bundle.loadServer(scriptLoader);
        }
    }

    /**
     * Removes and cleans up a bundle, including closing its ZIP FileSystem if applicable.
     */
    public void remove(Bundle bundle) {
        bundles.remove(bundle.info().id());
        unloadBundle(bundle);
        Common.getBlueprintManager().unloadBlueprintsForBundle(bundle);
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
    public Collection<Bundle> getBundles() {
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
     * Common scripts are reloaded here; dist-specific scripts will be reloaded
     * when the respective dist reload listeners fire.
     */
    public void reload() {
        LOGGER.info("Reloading FoundryEngine Bundles...");

        ClientEvents._clear();
        ServerEvents._clear();
        //BundleEvents._clear(); // I Guess this doesnt have to be cleared? bc its a registry

        unloadAllBundles();
        bundles.clear();

        try {
            discover(Common.BUNDLES);
        } catch (IOException e) {
            LOGGER.error("Failed to reload bundles", e);
        }
    }

    private void unloadAllBundles() {
        for (Bundle bundle : bundles.values()) {
            unloadBundle(bundle);
        }
    }

    /**
     * Unloads a single bundle: saves config, calls onUnload on all entrypoints,
     * and closes the ZIP FileSystem if present.
     */
    private void unloadBundle(Bundle bundle) {
        if (bundle.bundleConfig().isLoaded()) {
            bundle.bundleConfig().save();
        }

        bundle.unload();

        closeFileSystem(bundle);
    }

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
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.reload();
    }
}