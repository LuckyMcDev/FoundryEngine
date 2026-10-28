package de.luckymcdev.foundryengine.script.common;

import org.codehaus.groovy.control.CompilerConfiguration;

import java.util.Collection;
import java.util.List;

/**
 * Sandbox-independent compiler configuration. Sandbox customizers are added per compile by
 * {@link de.luckymcdev.foundryengine.script.common.runtime.ScriptCompiler}.
 */
public final class ScriptConfig {

	private ScriptConfig() {
	}

	public static Collection<String> fileExtensions() {
		return List.of("groovy", "gvy");
	}

	public static CompilerConfiguration createBaseCompilerConfig() {
		CompilerConfiguration config = new CompilerConfiguration();
		config.addCompilationCustomizers(LenientClosureTransformer.customizer());
		return config;
	}
}