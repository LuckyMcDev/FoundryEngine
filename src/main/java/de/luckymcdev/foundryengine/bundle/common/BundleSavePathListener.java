package de.luckymcdev.foundryengine.bundle.common;

import de.luckymcdev.foundryengine.world.common.StorageSourceManager;

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
