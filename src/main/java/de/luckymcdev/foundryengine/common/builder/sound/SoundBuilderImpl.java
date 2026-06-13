package de.luckymcdev.foundryengine.common.builder.sound;

import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.builder.AbstractBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

public class SoundBuilderImpl extends AbstractBuilder<SoundEvent> implements SoundBuilder {
    private final List<SoundFileEntry> soundFiles = new ArrayList<>();
    private float fixedRange = -1f;
    private String subtitle = null;
    private boolean replace = false;

    public SoundBuilderImpl(Identifier id) {
        super(id);
    }

    @Override
    public SoundBuilder range(float distance) {
        this.fixedRange = distance;
        return this;
    }

    @Override
    public SoundBuilder subtitle(String key) {
        this.subtitle = key;
        return this;
    }

    @Override
    public SoundBuilder replace(boolean replace) {
        this.replace = replace;
        return this;
    }

    @Override
    public SoundBuilder addSound(Identifier loc, float vol, float pitch, int weight, boolean stream, int attn, boolean preload) {
        this.soundFiles.add(new SoundFileEntry(loc, vol, pitch, weight, stream, attn, preload));
        return this;
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

    @Override
    public SoundEvent build() {
        return fixedRange > 0f ? SoundEvent.createFixedRangeEvent(id, fixedRange)
                : SoundEvent.createVariableRangeEvent(id);
    }

    @Override
    public SoundEvent register(RegisterEvent.RegisterHelper<SoundEvent> h) {
        SoundEvent e = build();
        h.register(id, e);
        setObject(e);
        return e;
    }

    @Override
    public SoundBuilder generateData(boolean generate) {
        this.generateData = generate;
        return this;
    }
}