package de.luckymcdev.foundryengine.common.bundle.modcompat;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import org.jetbrains.annotations.Nullable;

public class BundleModContainer extends ModContainer {

	private final Bundle bundle;

	public BundleModContainer(BundleModInfo modInfo, Bundle bundle) {
		super(modInfo);
		this.bundle = bundle;
	}

	@Override
	public @Nullable IEventBus getEventBus() {
		return null;
	}

	public Bundle getBundle() {
		return bundle;
	}
}
