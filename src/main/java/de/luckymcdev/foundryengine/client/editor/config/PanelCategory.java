package de.luckymcdev.foundryengine.client.editor.config;

import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;

public enum PanelCategory {
    OPEN("Open", ImIcons.FA.FA_BOX_OPEN),
    EDITOR("Editor", ImIcons.FA.FA_EDIT),
    TOOLS("Tools", ImIcons.FAE.FAE_TOOLS),
    VIEW("View", ImIcons.FA.FA_EYE),

    EDITOR_VISUALS("Visuals", ImIcons.FA.FA_CAMERA),
    EDITOR_EXPLORER("Explorer", ImIcons.FA.FA_FOLDER),
    EDITOR_FILES("Files", ImIcons.FA.FA_FILE),
    EDITOR_CONSOLE("Console", ImIcons.FA.FA_TERMINAL),
    EDITOR_TOOLS("Tools", ImIcons.FA.FA_TOOLBOX);

    public final String displayName;
    public final ImIcon icon;

    PanelCategory(String displayName, ImIcon icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getMenuLabel() {
        return displayName + " " + ImGuiUtils.icon(this.icon);
    }
}
