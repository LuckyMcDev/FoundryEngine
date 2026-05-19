package de.luckymcdev.foundryengine.common.builder.sound;

import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import de.luckymcdev.foundryengine.common.builder.BuilderState;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.ArrayList;
import java.util.List;

public class SoundBuilderImpl implements SoundBuilder {
    public final BuilderState<SoundEvent> state;
    private final List<SoundFileEntry> soundFiles = new ArrayList<>();
    private float fixedRange = -1f;
    private String subtitle = null;
    private boolean replace = false;

    public SoundBuilderImpl(Identifier id) {
        this.state = new BuilderState<>(id);
        this.state.registryKey = Registries.SOUND_EVENT;
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
        return fixedRange > 0f ? SoundEvent.createFixedRangeEvent(state.id, fixedRange)
                : SoundEvent.createVariableRangeEvent(state.id);
    }

    @Override
    public SoundEvent get() {
        return state.get();
    }

    @Override
    public SoundEvent getOrCreate() {
        return state.getOrCreate();
    }

    @Override
    public Identifier newID(String pre, String post) {
        return state.newID(pre, post);
    }

    @Override
    public SoundEvent register(RegisterEvent.RegisterHelper<SoundEvent> h) {
        SoundEvent e = build();
        h.register(state.id, e);
        state.setObject(e);
        return e;
    }
}