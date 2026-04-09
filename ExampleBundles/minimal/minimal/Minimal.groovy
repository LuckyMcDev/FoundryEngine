package minimal

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