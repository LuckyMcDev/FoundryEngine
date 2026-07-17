package de.luckymcdev.foundryengine.common.bundle.modcompat;

import de.luckymcdev.foundryengine.common.bundle.Bundle;
import net.neoforged.bus.EventBusErrorMessage;
import net.neoforged.bus.api.BusBuilder;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListener;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ConfigTracker;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

public class BundleModContainer extends ModContainer {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Bundle bundle;
	private final IEventBus eventBus;
	private ModConfigSpec configSpec;

	public BundleModContainer(BundleModInfo modInfo, Bundle bundle) {
		super(modInfo);
		this.bundle = bundle;
		this.eventBus = BusBuilder.builder()
			.setExceptionHandler(this::onEventFailed)
			.markerType(IModBusEvent.class)
			.allowPerPhasePost()
			.build();
		this.configSpec = null;
	}

	@Override
	public @Nullable IEventBus getEventBus() {
		return eventBus;
	}

	public Bundle getBundle() {
		return bundle;
	}

	private void onEventFailed(IEventBus iEventBus, Event event, EventListener[] iEventListeners, int i, Throwable throwable) {
		LOGGER.error(new EventBusErrorMessage(event, i, iEventListeners, throwable));
	}

	public @Nullable ModConfigSpec getConfigSpec() {
		return configSpec;
	}

	public void setConfigSpec(ModConfigSpec modConfigSpec) {
		this.configSpec = modConfigSpec;
	}

	@Override
	public void registerConfig(ModConfig.Type type, IConfigSpec configSpec) {
		this.configSpec = (ModConfigSpec) configSpec;
		if (configSpec.isEmpty()) {
			LOGGER.debug("Attempted to register an empty config for type {} on mod {}", type, modId);
			return;
		}
		ConfigTracker.INSTANCE.registerConfig(type, configSpec, this);
	}
}
