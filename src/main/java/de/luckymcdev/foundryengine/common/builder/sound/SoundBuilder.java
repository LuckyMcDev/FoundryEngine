package de.luckymcdev.foundryengine.common.builder.sound;

import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

public class SoundBuilder extends AbstractBuilder<SoundEvent> {
	private final List<SoundFileEntry> soundFiles = new ArrayList<>();
	private float fixedRange = -1.0f;
	private String subtitle = null;
	private boolean replace = false;

	public SoundBuilder(Identifier id) {
		super(id);
	}

	public static SoundBuilder create(Identifier id) {
		return new SoundBuilder(id);
	}

	public SoundBuilder range(float distance) {
		this.fixedRange = distance;
		return this;
	}

	public SoundBuilder subtitle(String key) {
		this.subtitle = key;
		return this;
	}

	public SoundBuilder replace(boolean replace) {
		this.replace = replace;
		return this;
	}

	public SoundBuilder addSound(Identifier loc, float vol, float pitch, int weight, boolean stream, int attn, boolean preload) {
		this.soundFiles.add(new SoundFileEntry(loc, vol, pitch, weight, stream, attn, preload));
		return this;
	}

	public SoundBuilder addSound(Identifier location) {
		return addSound(location, 1.0f, 1.0f, 1, false, 16, false);
	}

	public List<SoundFileEntry> getSoundFiles() {
		return soundFiles;
	}

	public String getSubtitle() {
		return subtitle;
	}

	public boolean isReplace() {
		return replace;
	}

	public SoundEvent build() {
		return fixedRange > 0.0f ? SoundEvent.createFixedRangeEvent(id, fixedRange)
			: SoundEvent.createVariableRangeEvent(id);
	}

	public SoundEvent register(RegisterEvent.RegisterHelper<SoundEvent> h) {
		SoundEvent e = build();
		h.register(id, e);
		setObject(e);
		return e;
	}

	public SoundBuilder generateData(boolean generate) {
		this.generateData = generate;
		return this;
	}

	public record SoundFileEntry(Identifier location, float volume, float pitch, int weight, boolean stream,
	                             int attenuationDistance, boolean preload) {
	}
}
