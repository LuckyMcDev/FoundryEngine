package de.luckymcdev.foundryengine.common.blueprint;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.common.blueprint.engine.BlueprintEngine;
import de.luckymcdev.foundryengine.common.blueprint.graph.BlueprintGraph;
import de.luckymcdev.foundryengine.common.blueprint.serial.BlueprintSerializer;
import de.luckymcdev.foundryengine.common.bundle.Bundle;
import de.luckymcdev.foundryengine.common.bundle.BundleLifecycleListener;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class BlueprintManager implements BundleLifecycleListener {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final GenericRegistry<String, BundleBlueprints> bundleBlueprints = new GenericRegistry<>();

    @Override
    public void onBundleLoaded(Bundle bundle) {
        loadBlueprintsForBundle(bundle);
    }

    @Override
    public void onBundleUnloaded(Bundle bundle) {
        unloadBlueprintsForBundle(bundle);
    }

    public void loadBlueprintsForBundle(Bundle bundle) {
        File blueprintsDir = bundle.bundleFiles().blueprints().toFile();

        if (!blueprintsDir.isDirectory()) return;

        File[] files = blueprintsDir.listFiles();
        if (files == null || files.length == 0) return;

        BlueprintEngine engine = new BlueprintEngine();
        engine.registerBuiltins();

        BlueprintGraph graph = new BlueprintGraph();
        BlueprintSerializer serializer = new BlueprintSerializer(engine);

        for (File file : files) {
            if (!file.getName().endsWith(BlueprintSerializer.EXTENSION)) continue;
            try {
                serializer.loadFromFile(file.toPath(), graph);
                LOGGER.info("[Blueprint] Loaded '{}' for bundle '{}'",
                        file.getName(), bundle.info().id());
            } catch (IOException e) {
                LOGGER.error("[Blueprint] Failed to load '{}' for bundle '{}'",
                        file.getName(), bundle.info().id(), e);
            }
        }

        bundleBlueprints.register(bundle.info().id(), new BundleBlueprints(engine, graph));
    }

    public void unloadBlueprintsForBundle(Bundle bundle) {
        bundleBlueprints.remove(bundle.info().id());
    }

    public void executeCommonEvent(String eventName) {
        bundleBlueprints.forEach(bp -> bp.engine().executeEvent(eventName, bp.graph()));
    }

    /**
     * Internal: execute an event with an additional runtime context payload.
     */
    public void executeCommonEvent(String eventName, Map<String, Object> payload) {
        bundleBlueprints.forEach(bp -> bp.engine().executeEvent(eventName, bp.graph(), payload));
    }

    public void executeEventForBundle(String bundleId, String eventName) {
        BundleBlueprints bp = bundleBlueprints.get(bundleId);
        if (bp != null) bp.engine().executeEvent(eventName, bp.graph());
    }

    public GenericRegistry<String, BundleBlueprints> getBundleBlueprints() {
        return bundleBlueprints;
    }

    public BundleBlueprints getBlueprintsFor(Bundle bundle) {
        return bundleBlueprints.get(bundle.info().id());
    }

    public record BundleBlueprints(BlueprintEngine engine, BlueprintGraph graph) {
    }
}
