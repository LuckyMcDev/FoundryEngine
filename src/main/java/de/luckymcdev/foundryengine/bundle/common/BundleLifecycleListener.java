package de.luckymcdev.foundryengine.bundle.common;

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
