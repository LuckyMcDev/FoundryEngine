package de.luckymcdev.foundryengine.config;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Configuration shared by feature modules. A disabled module does not wire its
 * managers, listeners, packets, or registrations.
 */
public final class ModuleConfig {
	public final ModConfigSpec SPEC;
	public final ModConfigSpec.BooleanValue ENABLED;

	public ModuleConfig(String moduleName) {
		var builder = new ModConfigSpec.Builder();
		ENABLED = builder
			.comment("Whether the " + moduleName + " module is enabled.")
			.translation("foundryengine.configuration.enabled")
			.define("enabled", true);
		SPEC = builder.build();
	}

	public boolean registerAndCheck(ModContainer container) {
		container.registerConfig(
			ModConfig.Type.STARTUP,
			SPEC,
			Config.fileNameForMod(container.getModId())
		);
		if (FMLEnvironment.getDist().isClient()) {
			container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
		}
		return ENABLED.get();
	}
}
