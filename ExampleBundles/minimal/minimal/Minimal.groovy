package minimal

import de.luckymcdev.foundryengine.common.bundle.config.BundleConfig
import de.luckymcdev.foundryengine.common.script.BundleEntrypoint
import net.neoforged.bus.api.IEventBus

class Minimal extends BundleEntrypoint {

    Minimal(IEventBus eventBus, BundleConfig bundleConfig) {
        super(eventBus, bundleConfig)
    }

    @Override
    void onLoad() {
    }

    @Override
    void onUnload() {
    }
}