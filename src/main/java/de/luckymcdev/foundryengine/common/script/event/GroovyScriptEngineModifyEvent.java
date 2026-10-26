package de.luckymcdev.foundryengine.common.script.event;

import net.neoforged.bus.api.Event;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Event posted once when the script compiler is created. Allows modification of the shared
 * template {@link CompilerConfiguration} before any bundle is compiled.
 * <p>
 * Compilation customizers added here are kept. Sandbox customizers are added afterwards and
 * cannot be removed through this event.
 */
public class GroovyScriptEngineModifyEvent extends Event {

	private final CompilerConfiguration compilerConfiguration;

	public GroovyScriptEngineModifyEvent(CompilerConfiguration compilerConfiguration) {
		this.compilerConfiguration = compilerConfiguration;
	}

	public CompilerConfiguration getCompilerConfiguration() {
		return compilerConfiguration;
	}
}