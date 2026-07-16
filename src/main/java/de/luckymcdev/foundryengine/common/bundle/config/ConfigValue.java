package de.luckymcdev.foundryengine.common.bundle.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ConfigValue<T> {
	private final ModConfigSpec.ConfigValue<T> neoValue;

	ConfigValue(ModConfigSpec.ConfigValue<T> neoValue) {
		this.neoValue = neoValue;
	}

	public T get() {
		return neoValue.get();
	}

	public ModConfigSpec.ConfigValue<T> unwrap() {
		return neoValue;
	}
}
