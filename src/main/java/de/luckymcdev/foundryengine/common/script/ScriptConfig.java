package de.luckymcdev.foundryengine.common.script;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

/**
 * Shared CompilerConfiguration for all Groovy scripting in FoundryEngine.
 * Imports all Minecraft, NeoForge, and FoundryEngine classes so bundle scripts
 * and the eval command have full access to the environment.
 */
public final class ScriptConfig {

	private ScriptConfig() {
	}

	public static CompilerConfiguration createCompilerConfig() {
		CompilerConfiguration config = new CompilerConfiguration();
		config.addCompilationCustomizers(createImportCustomizer());
		return config;
	}

	private static ImportCustomizer createImportCustomizer() {
		ImportCustomizer imports = new ImportCustomizer();
		imports.addStarImports(
			"net.minecraft",
			"net.minecraft.world",
			"net.minecraft.world.entity",
			"net.minecraft.world.entity.player",
			"net.minecraft.world.item",
			"net.minecraft.world.level",
			"net.minecraft.world.level.block",
			"net.minecraft.core",
			"net.minecraft.resources",
			"net.minecraft.server",
			"net.minecraft.server.level",
			"net.minecraft.network.chat",
			"net.neoforged.neoforge",
			"net.neoforged.bus.api",
			"de.luckymcdev.foundryengine",
			"de.luckymcdev.foundryengine.common",
			"de.luckymcdev.foundryengine.api"
		);
		return imports;
	}
}