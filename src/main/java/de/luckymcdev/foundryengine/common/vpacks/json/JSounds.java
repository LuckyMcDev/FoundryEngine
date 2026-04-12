package de.luckymcdev.foundryengine.common.vpacks.json;


import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a sounds.json file.
 */
public class JSounds implements Cloneable {
    private final Map<String, SoundEventEntry> sounds = new LinkedHashMap<>();

    public static JSounds sounds() {
        return new JSounds();
    }

    public void addComplex(SoundEvent event, SoundEventEntry entry) {
        sounds.put(event.location().getPath(), entry);
    }

    public Map<String, SoundEventEntry> getSounds() {
        return sounds;
    }

    @Override
    public JSounds clone() {
        try {
            return (JSounds) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    public static class SoundEventEntry {
        public final List<SoundEntry> sounds = new ArrayList<>();
        public boolean replace = false;
        public String subtitle = null;
    }

    public static class SoundEntry {
        public String name;
        public float volume = 1.0f;
        public float pitch = 1.0f;
        public int weight = 1;
        public boolean stream = false;
        public int attenuation_distance = 16;
        public boolean preload = false;
        public String type = "file";

        public SoundEntry(String name) {
            this.name = name;
        }
    }
}