package de.luckymcdev.foundryengine.common.cutscene.model;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

/**
 * Base class for all cutscene attachments.
 * Attachments are timeline-based objects that can be attached to a cutscene
 * at a specific normalized time position (0..1).
 * <p>
 * This system makes it easy to add new attachment types in the future
 * by simply extending this class and implementing the required methods.
 */
public abstract class CutsceneAttachment {
    /**
     * Normalized time (0..1) along the cutscene playback where this attachment triggers.
     */
    protected float at;

    /**
     * A human-readable name/type identifier for this attachment.
     * Used for serialization and UI display.
     */
    protected String type;

    protected CutsceneAttachment(float at, String type) {
        this.at = Mth.clamp(at, 0f, 1f);
        this.type = type == null ? "unknown" : type;
    }

    public float getAt() {
        return at;
    }

    public void setAt(float at) {
        this.at = Mth.clamp(at, 0f, 1f);
    }

    public String getType() {
        return type;
    }

    /**
     * Serializes this attachment to NBT for saving.
     * Subclasses should override this to add their specific data.
     */
    public CompoundTag toNbt() {
        CompoundTag tag = new CompoundTag();
        tag.putFloat("At", this.at);
        tag.putString("Type", this.type);
        return tag;
    }

    /**
     * Creates a copy of this attachment.
     * Subclasses must implement this properly.
     */
    public abstract CutsceneAttachment copy();

    /**
     * Returns a display name for this attachment (for UI).
     */
    public abstract String getDisplayName();

    /**
     * Returns the total normalized duration of this attachment (0..1),
     * or 0 if it's instantaneous.
     */
    public abstract float getDuration();
}
