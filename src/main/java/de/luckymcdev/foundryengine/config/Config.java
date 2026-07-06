package de.luckymcdev.foundryengine.config;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/**
 * Registers the four NeoForge config types (client, server, common, startup).
 */
public final class Config {

	public static void registerClient(ModContainer container) {
		container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC);
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}

	public static void registerServer(ModContainer container) {
		container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
	}

	public static void registerCommon(ModContainer container) {
		container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
	}

	public static void registerStartup(ModContainer container) {
		container.registerConfig(ModConfig.Type.STARTUP, StartupConfig.SPEC);
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading event) {
	}

	@SubscribeEvent
	public static void onReload(final ModConfigEvent.Reloading event) {
	}
}