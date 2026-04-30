package de.luckymcdev.foundryengine.client.cutscene;

import de.luckymcdev.foundryengine.common.cutscene.model.Cutscene;
import org.jetbrains.annotations.Nullable;

/**
 * Lightweight shared UI state between the Cutscene panels.
 */
public final class CutsceneUiState {
    private static @Nullable String selectedCutsceneName = null;

    private CutsceneUiState() {
    }

    public static void setSelected(@Nullable Cutscene cutscene) {
        selectedCutsceneName = cutscene == null ? null : cutscene.getName();
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
}

