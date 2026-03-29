/*
 * Copyright (c) 2025 LuckyMcDev
 * All Rights Reserved.
 *
 * Last modified: 2025-11-30 18:10:34
 */

package de.luckymcdev.foundryengine.client.editor.styles.theme;

import imgui.ImGui;
import imgui.ImGuiStyle;

public interface ImTheme {
    /**
     * Returns the name of this theme.
     *
     * @return the name of this theme
     */
    String getName();

    /**
     * Applies the theme to the given ImGui style.
     *
     * @param style the ImGui style to apply the theme to
     */
    void applyTheme(ImGuiStyle style);

    default void apply(ImGuiStyle style) {
        ImGui.styleColorsDark();
        applyTheme(style);
    }
}
