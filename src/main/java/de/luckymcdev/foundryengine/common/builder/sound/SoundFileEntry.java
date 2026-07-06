package de.luckymcdev.foundryengine.common.builder.sound;

import net.minecraft.resources.Identifier;

public record SoundFileEntry(
	Identifier location,
	float volume,
	float pitch,
	int weight,
	boolean stream,
	int attenuationDistance,
	boolean preload
) {
	public SoundFileEntry(Identifier location) {
		this(location, 1.0f, 1.0f, 1, false, 16, false);
	}
}
