package de.luckymcdev.foundryengine.common.vpacks.json;

import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a sounds.json file for a resource pack.
 * Each entry maps a sound event ID to one or more .ogg sound files.
 *
 * <p>Usage:
 * <pre>{@code
 * JSounds.sounds()
 *     .add(MY_SOUND, Identifier.fromNamespaceAndPath("yourmod", "sounds/my_sound"))
 *     .add(MY_SOUND, Identifier.fromNamespaceAndPath("yourmod", "sounds/ambient"), true)
 * }</pre>
 */
public class JSounds implements Cloneable {

    private final Map<String, SoundEventEntry> sounds = new LinkedHashMap<>();

    public static JSounds sounds() {
        return new JSounds();
    }

    /**
     * Registers a sound event with a single .ogg file at default volume and pitch.
     *
     * @param event     The sound event to register
     * @param soundPath The path to the .ogg file (without extension), e.g. "yourmod:sounds/my_sound"
     * @return This instance for chaining
     */
    public JSounds add(SoundEvent event, Identifier soundPath) {
        return add(event.location(), soundPath, false, 1.0f, 1.0f);
    }

    /**
     * Registers a sound event with streaming support.
     * Use streaming for long audio like music or ambient tracks.
     *
     * @param event     The sound event to register
     * @param soundPath The path to the .ogg file (without extension)
     * @param stream    Whether to stream this sound (true for long tracks)
     * @return This instance for chaining
     */
    public JSounds add(SoundEvent event, Identifier soundPath, boolean stream) {
        return add(event.location(), soundPath, stream, 1.0f, 1.0f);
    }

    /**
     * Registers a sound event with full control over volume, pitch, and streaming.
     *
     * @param event     The sound event to register
     * @param soundPath The path to the .ogg file (without extension)
     * @param stream    Whether to stream this sound
     * @param volume    Base volume (1.0 = normal)
     * @param pitch     Base pitch (1.0 = normal)
     * @return This instance for chaining
     */
    public JSounds add(SoundEvent event, Identifier soundPath, boolean stream, float volume, float pitch) {
        return add(event.location(), soundPath, stream, volume, pitch);
    }

    /**
     * Registers a sound event by raw identifier, with full control.
     *
     * @param eventId   The sound event ID
     * @param soundPath The path to the .ogg file (without extension)
     * @param stream    Whether to stream this sound
     * @param volume    Base volume (1.0 = normal)
     * @param pitch     Base pitch (1.0 = normal)
     * @return This instance for chaining
     */
    public JSounds add(Identifier eventId, Identifier soundPath, boolean stream, float volume, float pitch) {
        SoundEventEntry entry = sounds.computeIfAbsent(eventId.getPath(), k -> new SoundEventEntry());
        entry.sounds.add(new SoundEntry(soundPath.toString(), stream, volume, pitch));
        return this;
    }

    /**
     * Registers a sound event with a subtitle (shown in Minecraft's subtitles system).
     * Note: the subtitle translation key must also be added to your lang file via
     * {@link JLang#addSoundTranslation(Identifier, String)}.
     *
     * @param event     The sound event to register
     * @param soundPath The path to the .ogg file (without extension)
     * @param subtitle  The subtitle translation key
     * @return This instance for chaining
     */
    public JSounds addWithSubtitle(SoundEvent event, Identifier soundPath, String subtitle) {
        SoundEventEntry entry = sounds.computeIfAbsent(event.location().getPath(), k -> new SoundEventEntry());
        entry.subtitle = subtitle;
        entry.sounds.add(new SoundEntry(soundPath.toString(), false, 1.0f, 1.0f));
        return this;
    }

    /**
     * Marks an existing sound event entry as replace=true, meaning it will
     * override any sounds.json entries from other resource packs for that event.
     *
     * @param event The sound event to mark as replacing
     * @return This instance for chaining
     */
    public JSounds replace(SoundEvent event) {
        SoundEventEntry entry = sounds.get(event.location().getPath());
        if (entry != null) entry.replace = true;
        return this;
    }

    /**
     * @return The raw sounds map, keyed by sound event path.
     * Used for JSON serialization.
     */
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

    public record SoundEntry(String name, boolean stream, float volume, float pitch) {
    }
}