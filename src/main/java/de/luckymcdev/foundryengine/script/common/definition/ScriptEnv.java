package de.luckymcdev.foundryengine.script.common.definition;

public enum ScriptEnv {
	COMMON("common"),
	CLIENT("client"),
	SERVER("server");

	private final String name;

	ScriptEnv(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}