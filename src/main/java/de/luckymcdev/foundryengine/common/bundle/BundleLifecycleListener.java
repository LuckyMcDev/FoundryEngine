package de.luckymcdev.foundryengine.common.bundle;

public interface BundleLifecycleListener {
	default void onBundleLoaded(Bundle bundle) {
	}

	default void onBundlePreUnload(Bundle bundle) {
	}

	default void onBundleUnloaded(Bundle bundle) {
	}

	default void onBundleReloadStarted() {
	}

	default void onBundleReloadCompleted() {
	}
}
