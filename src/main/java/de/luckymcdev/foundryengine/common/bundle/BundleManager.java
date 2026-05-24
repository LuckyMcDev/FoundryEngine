package de.luckymcdev.foundryengine.common.bundle;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.api.event.*;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import de.luckymcdev.foundryengine.common.script.BundleScriptLoader;
import de.luckymcdev.foundryengine.server.Server;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
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
            try {
                bundle.loadClient(scriptLoader);
            } catch (Exception e) {
                LOGGER.error("Failed to load client scripts for bundle '{}'", bundle.info().id(), e);
                ModLoadingIssue issue = ModLoadingIssue.error(String.format(
                        "Failed to load client scripts for bundle '%s': %s", bundle.info().id(), e.getMessage()));
                ModLoader.addLoadingIssue(issue);
                if (Server.getServer() != null) {
                    Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] Bundle '" + bundle.info().id() + "' client scripts: " + e), false);
                }
            }
        }
    }

    /**
     * Loads server-side scripts for all registered bundles.
     * Call this from {@code FoundryEngineModServer} during server setup.
     */
    public void loadServerScripts() {
        for (Bundle bundle : bundles.values()) {
            try {
                bundle.loadServer(scriptLoader);
            } catch (Exception e) {
                LOGGER.error("Failed to load server scripts for bundle '{}'", bundle.info().id(), e);
                ModLoadingIssue issue = ModLoadingIssue.error(String.format(
                        "Failed to load server scripts for bundle '%s': %s", bundle.info().id(), e.getMessage()));
                ModLoader.addLoadingIssue(issue);
                if (Server.getServer() != null) {
                    Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] Bundle '" + bundle.info().id() + "' server scripts: " + e), false);
                }
            }
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
     * Reloads all bundles by clearing all script event callbacks, unloading current
     * bundles, rediscovering them from disk, and re-loading common + server scripts.
     */
    public void reload() {
        LOGGER.info("Reloading FoundryEngine Bundles...");

        AreaEvents.Internal.clear();
        BlockEvents.Internal.clear();
        BundleEvents.Internal.clear();
        ClientEvents.Internal.clear();
        CommandEvents.Internal.clear();
        EntityEvents.Internal.clear();
        ItemEvents.Internal.clear();
        LevelEvents.Internal.clear();
        NetworkEvents.Internal.clear();
        PlayerEvents.Internal.clear();
        RecipeEvents.Internal.clear();
        ServerEvents.Internal.clear();

        unloadAllBundles();
        bundles.clear();

        try {
            discover(Common.BUNDLES);
        } catch (IOException e) {
            LOGGER.error("Failed to reload bundles", e);
            ModLoadingIssue issue = ModLoadingIssue.error("Failed to reload bundles: " + e.getMessage());
            ModLoader.addLoadingIssue(issue);
            if (Server.getServer() != null) {
                Server.getServer().getPlayerList().broadcastSystemMessage(Component.literal("§c[Script Error] Bundle reload: " + e), false);
            }
        }

        loadServerScripts();
    }

    private void unloadAllBundles() {
        for (Bundle bundle : bundles.values()) {
            unloadBundle(bundle);
            Common.getBlueprintManager().unloadBlueprintsForBundle(bundle);
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