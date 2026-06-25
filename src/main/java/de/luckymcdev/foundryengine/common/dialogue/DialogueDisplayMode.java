package de.luckymcdev.foundryengine.common.dialogue;

/**
 * Controls how dialogue is presented on the client.
 * {@link #SCREEN} opens a full-screen widget overlay.
 * {@link #CHAT} prints dialogue to the chat window.
 */
public enum DialogueDisplayMode {
    SCREEN, CHAT;

    /**
     * @param ordinal 0-based enum index
     * @return the matching enum constant, or {@link #SCREEN} if out of range
     */
    public static DialogueDisplayMode fromOrdinal(int ordinal) {
        var values = values();
        if (ordinal < 0 || ordinal >= values.length) return SCREEN;
        return values[ordinal];
    }
}
