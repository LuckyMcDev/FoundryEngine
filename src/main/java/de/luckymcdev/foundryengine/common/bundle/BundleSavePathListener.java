package de.luckymcdev.foundryengine.common.bundle;

import de.luckymcdev.foundryengine.common.world.StorageSourceManager;

public class BundleSavePathListener implements BundleLifecycleListener {
    @Override
    public void onBundleLoaded(Bundle bundle) {
        StorageSourceManager.addAdditionalPath(bundle.bundleFiles().saves());
    }

    @Override
    public void onBundleUnloaded(Bundle bundle) {
        StorageSourceManager.removeAdditionalPath(bundle.bundleFiles().saves());
    }
}
