package de.luckymcdev.foundryengine.util.common;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.*;
import net.neoforged.neoforge.common.NeoForge;

/**
 * Shared wiring contract for the modular FoundryEngine entrypoints.
 * Only exposes the FML lifecycle hooks; everything else goes through {@link #bus()}.
 */
public interface EngineModPart {

	default void wire(IEventBus modBus) {
		modBus.addListener(this::onConstruct);
		modBus.addListener(this::onCommonSetup);
		modBus.addListener(this::onClientSetup);
		modBus.addListener(this::onDedicatedServerSetup);
		modBus.addListener(this::onLoadComplete);
	}

	default IEventBus bus() {
		return NeoForge.EVENT_BUS;
	}

	default void onConstruct(FMLConstructModEvent event) {
	}

	default void onCommonSetup(FMLCommonSetupEvent event) {
	}

	default void onClientSetup(FMLClientSetupEvent event) {
	}

	default void onDedicatedServerSetup(FMLDedicatedServerSetupEvent event) {
	}

	default void onLoadComplete(FMLLoadCompleteEvent event) {
	}
}