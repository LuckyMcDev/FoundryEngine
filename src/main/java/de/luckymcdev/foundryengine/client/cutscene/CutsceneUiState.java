package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import de.luckymcdev.foundryengine.common.cutscene.util.LerpType;
import org.jetbrains.annotations.Nullable;

/**
 * Shared UI state between the Cutscene panels.
 * Centralizes playback settings so both CutscenePanel and CutsceneTimelinePanel stay in sync.
 */
public final class CutsceneUiState {
    private static @Nullable String selectedCutsceneName = null;

    // Shared playback settings - kept in sync between panels
    private static int playbackLength = 60;
    private static int playbackHoldStart = 0;
    private static int playbackHoldEnd = 0;
    private static int playbackEasingIndex = 0;

    // Last known cutscene to detect changes
    private static @Nullable String lastKnownCutsceneName = null;

    private CutsceneUiState() {
    }

    public static void setSelected(@Nullable Cutscene cutscene) {
        selectedCutsceneName = cutscene == null ? null : cutscene.getName();
        if (cutscene != null) {
            syncFromCutscene(cutscene);
        }
    }

    public static @Nullable String getSelectedName() {
        return selectedCutsceneName;
    }

    public static void setSelectedName(@Nullable String name) {
        selectedCutsceneName = (name == null || name.isBlank()) ? null : name;
    }

    public static @Nullable Cutscene getSelectedCutscene() {
        if (selectedCutsceneName == null) return null;
        return CutsceneRenderer.findByName(selectedCutsceneName);
    }

    /**
     * Sync UI state from cutscene defaults. Call when selecting a different cutscene.
     */
    public static void syncFromCutscene(Cutscene cutscene) {
        if (cutscene == null) return;
        String name = cutscene.getName();
        if (name.equals(lastKnownCutsceneName)) return;

        playbackLength = cutscene.getDefaultLength();
        playbackHoldStart = cutscene.getDefaultHoldStart();
        playbackHoldEnd = cutscene.getDefaultHoldEnd();

        String easing = cutscene.getDefaultEasing();
        playbackEasingIndex = 0;
        var easingValues = LerpType.values();
        for (int i = 0; i < easingValues.length; i++) {
            if (easingValues[i].name().equalsIgnoreCase(easing)) {
                playbackEasingIndex = i;
                break;
            }
        }

        lastKnownCutsceneName = name;
    }

    /**
     * Update cutscene defaults from current UI state. Call when UI values change.
     */
    public static void syncToCutscene(Cutscene cutscene) {
        if (cutscene == null) return;
        cutscene.setDefaultLength(playbackLength);
        cutscene.setDefaultHoldStart(playbackHoldStart);
        cutscene.setDefaultHoldEnd(playbackHoldEnd);
        var easingValues = LerpType.values();
        if (playbackEasingIndex >= 0 && playbackEasingIndex < easingValues.length) {
            cutscene.setDefaultEasing(easingValues[playbackEasingIndex].name());
        }
    }

    // Getters and setters for panels
    public static int getPlaybackLength() {
        return playbackLength;
    }

    public static void setPlaybackLength(int v) {
        playbackLength = Math.max(1, v);
    }

    public static int getPlaybackHoldStart() {
        return playbackHoldStart;
    }

    public static void setPlaybackHoldStart(int v) {
        playbackHoldStart = Math.max(0, v);
    }

    public static int getPlaybackHoldEnd() {
        return playbackHoldEnd;
    }

    public static void setPlaybackHoldEnd(int v) {
        playbackHoldEnd = Math.max(0, v);
    }

    public static int getPlaybackEasingIndex() {
        return playbackEasingIndex;
    }

    public static void setPlaybackEasingIndex(int v) {
        playbackEasingIndex = v;
    }
}

