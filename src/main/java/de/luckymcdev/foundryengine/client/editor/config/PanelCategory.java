package de.luckymcdev.foundryengine.client.editor.config;

import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import org.jspecify.annotations.Nullable;

public enum PanelCategory {
    OPEN("Open", ImIcons.FA.FA_BOX_OPEN),
    EDITOR("Editor", ImIcons.FA.FA_EDIT),
    TOOLS("Tools", ImIcons.FAE.FAE_TOOLS),
    VIEW("View", ImIcons.FA.FA_EYE),

    EDITOR_EXPLORER("Explorer", ImIcons.FA.FA_FOLDER, EDITOR),
    EDITOR_FILES("Files", ImIcons.FA.FA_FILE, EDITOR),
    EDITOR_BLUEPRINTS("Blueprints", ImIcons.FA.FA_MAP, EDITOR),
    EDITOR_CUTSCENES("Cutscenes", ImIcons.FA.FA_FILM, EDITOR),
    EDITOR_SCENE("Scene", ImIcons.FA.FA_FOLDER_TREE, EDITOR);


    public final String displayName;
    public final ImIcon icon;
    public final @Nullable PanelCategory parent;

    PanelCategory(String displayName, ImIcon icon) {
        this(displayName, icon, null);
    }

    PanelCategory(String displayName, ImIcon icon, @Nullable PanelCategory parent) {
        this.displayName = displayName;
        this.icon = icon;
        this.parent = parent;
    }

    public boolean isChildOf(PanelCategory category) {
        return this.parent == category;
    }

    public boolean isTopLevel() {
        return this.parent == null;
    }

    public String getMenuLabel() {
        return displayName + " " + ImGuiUtils.icon(this.icon);
    }
}