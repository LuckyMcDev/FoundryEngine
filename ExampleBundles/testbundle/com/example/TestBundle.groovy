package com.example


import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.bundle.config.BundleConfigSpec
import de.luckymcdev.foundryengine.common.bundle.config.ConfigValue
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.minecraft.resources.Identifier
import net.neoforged.bus.api.IEventBus
import com.example.TestBundleGameEvents
import com.example.TestBundleBundleEvents

/**
 * This is the main entrypoint of the bundle.
 * This handles things like the event bus.
 *
 * Every bundle can have multiple of these!
 */
class TestBundle extends BundleEntrypoint {

    public static final String BUNDLEID = "testbundle"
    public static ConfigValue<Boolean> coolFeature
    public static ConfigValue<Integer> spawnRate

    /**
     * Constructor for the bundle entrypoint. You can do basic setup here, but you should avoid doing anything too complex until onLoad.
     */
    TestBundle(IEventBus bundleBus, IEventBus eventBus, BundleConfig bundleConfig) {
        super(bundleBus, eventBus, bundleConfig)
    }

    /**
     * Helper method for creating identifiers with the bundle namespace.
     * You need to write this yourself.
     */
    static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(BUNDLEID, path)
    }

    /**
     * Method called when the bundle is loaded.
     */
    @Override
    void onLoad() {
        eventBus.register(TestBundleGameEvents)
        bundleBus.register(TestBundleBundleEvents)

        /*
        * Example of defining config values. These will be automatically synced to clients if changed on the server.
         */
        def spec = new BundleConfigSpec(bundleConfig)
        coolFeature = spec.defineBoolean("coolFeature", false, "Enables a cool feature.")
        spawnRate = spec.defineInt("spawnRate", 10, "Determines the spawn rate of something.")
        spec.build()
    }

    /**
     * Method called when the bundle is unloaded. You NEED to unregister any event handlers here.
     */
    @Override
    void onUnload() {
        eventBus.unregister(TestBundleGameEvents)
        bundleBus.unregister(TestBundleBundleEvents)
    }

}


