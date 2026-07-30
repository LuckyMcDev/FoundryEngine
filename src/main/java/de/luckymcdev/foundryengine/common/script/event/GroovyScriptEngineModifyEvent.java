package de.luckymcdev.foundryengine.common.script.event;

import de.luckymcdev.foundryengine.common.script.ScriptShell;
import net.neoforged.bus.api.Event;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Event posted once when the global {@link ScriptShell} is initialized.
 * Allows modification of the shared {@link CompilerConfiguration}
 * before any bundle scripts are compiled.
 */
public class GroovyScriptEngineModifyEvent extends Event {

	private final ScriptShell scriptShell;
	private final CompilerConfiguration compilerConfiguration;

	public GroovyScriptEngineModifyEvent(ScriptShell scriptShell,
	                                     CompilerConfiguration compilerConfiguration) {
		this.scriptShell = scriptShell;
		this.compilerConfiguration = compilerConfiguration;
	}

	public ScriptShell getScriptShell() {
		return scriptShell;
	}

	public CompilerConfiguration getCompilerConfiguration() {
		return compilerConfiguration;
	}
}
