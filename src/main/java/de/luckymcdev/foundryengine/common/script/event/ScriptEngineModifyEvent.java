package de.luckymcdev.foundryengine.common.script.event;

import de.luckymcdev.foundryengine.common.script.BundleScriptEngine;
import net.neoforged.bus.api.Event;

/**
 * An Event to modify the Script Engine.
 */
public class ScriptEngineModifyEvent extends Event {
	private final BundleScriptEngine scriptEngine;

	public ScriptEngineModifyEvent(BundleScriptEngine scriptEngine) {
		this.scriptEngine = scriptEngine;
	}

	public BundleScriptEngine getScriptEngine() {
		return scriptEngine;
	}
}
