package de.luckymcdev.foundryengine.client.editor.config;

import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import org.jspecify.annotations.Nullable;

public enum PanelCategory {
    OPEN("Open", ImIcons.BOX_OPEN),
    EDITOR("Editor", ImIcons.EDIT),
    TOOLS("Tools", ImIcons.TOOLS),
    VIEW("View", ImIcons.EYE),

    EDITOR_EXPLORER("Explorer", ImIcons.FOLDER, EDITOR),
    EDITOR_FILES("Files", ImIcons.FILE, EDITOR),
    EDITOR_CUTSCENES("Cutscenes", ImIcons.FILM, EDITOR);


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

    public String getDisplayName() {
        return displayName;
    }

    public String getMenuLabel() {
        return getDisplayName() + " " + ImGraphicsExtractor.icon(this.icon);
    }
}