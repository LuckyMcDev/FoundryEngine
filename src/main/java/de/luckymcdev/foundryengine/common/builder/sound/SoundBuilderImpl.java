package de.luckymcdev.foundryengine.common.builder.sound;

import de.luckymcdev.foundryengine.api.builder.sound.SoundBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class SoundBuilderImpl implements SoundBuilder {
    private final Identifier id;
    private final List<SoundFileEntry> soundFiles = new ArrayList<>();
    private float fixedRange = -1f;
    private String subtitle = null;
    private boolean replace = false;
    private @Nullable SoundEvent object;

    public SoundBuilderImpl(Identifier id) {
        this.id = id;
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

    @Override
    public Identifier getId() {
        return id;
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
    public SoundEvent get() {
        if (object == null) {
            throw new IllegalStateException("Sound " + id + " has not been registered yet");
        }
        return object;
    }

    @Override
    public SoundEvent getOrCreate() {
        if (object == null) {
            object = build();
        }
        return object;
    }

    @Override
    public Identifier newID(String pre, String post) {
        if (pre.isEmpty() && post.isEmpty()) {
            return id;
        }
        return id.withPath(pre + id.getPath() + post);
    }

    @Override
    public SoundEvent register(RegisterEvent.RegisterHelper<SoundEvent> h) {
        SoundEvent e = build();
        h.register(id, e);
        this.object = e;
        return e;
    }
}