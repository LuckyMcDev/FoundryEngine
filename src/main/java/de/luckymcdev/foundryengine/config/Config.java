package de.luckymcdev.foundryengine.config;

import de.luckymcdev.foundryengine.Common;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Registers the four NeoForge config types (client, server, common, startup).
 */
public final class Config {

	public static void registerClient(ModContainer container) {
		container.registerConfig(ModConfig.Type.CLIENT, ClientConfig.SPEC, fileName("client"));
		container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
	}

	public static void registerServer(ModContainer container) {
		container.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC, fileName("server"));
	}

	public static void registerCommon(ModContainer container) {
		container.registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC, fileName("common"));
	}

	public static void registerStartup(ModContainer container) {
		container.registerConfig(ModConfig.Type.STARTUP, StartupConfig.SPEC, fileName("startup"));
	}

	public static String fileName(String name) {
		return Common.MODID + "/" + name + ".toml";
	}

	public static String fileNameForMod(String modId) {
		String prefix = Common.MODID + "_";
		String name = modId.startsWith(prefix) ? modId.substring(prefix.length()) : modId;
		return fileName(name);
	}

	public static void push(ModConfigSpec.Builder builder, String path, String comment) {
		builder
			.comment(comment)
			.translation("foundryengine.configuration." + path)
			.push(path);
	}

	public static void pop(ModConfigSpec.Builder builder) {
		builder.pop();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfigEvent.Loading event) {
	}

	@SubscribeEvent
	public static void onReload(final ModConfigEvent.Reloading event) {
	}
}