package de.luckymcdev.foundryengine.client.editor.panel.files;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGuiTexture;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import imgui.ImGui;
import net.minecraft.resources.Identifier;

import java.io.File;

public class TextureViewerPanel extends EditorPanel {
    private final ImGuiTexture image;
    private final String sourcePath;

    public TextureViewerPanel(Identifier id, String title, Identifier identifier) {
        super(new Builder(id, title)
                .icon(ImIcons.FA.FA_IMAGES)
                .category(PanelCategory.EDITOR_FILES));
        this.image = ImGuiUtils.getTexture(identifier);
        this.sourcePath = identifier.toString();
    }

    public TextureViewerPanel(Identifier id, String title, File file) {
        super(new Builder(id, title)
                .icon(ImIcons.FA.FA_IMAGES)
                .category(PanelCategory.EDITOR_FILES));
        this.image = ImGuiUtils.getTexture(file);
        this.sourcePath = file.getAbsolutePath();
    }

    @Override
    public void content() {
        if (image.isMissing()) {
            ImGui.text("Failed to load texture: " + sourcePath);
            return;
        }

        float availWidth = ImGui.getContentRegionAvailX();
        float displayHeight = (availWidth / image.width()) * image.height();

        ImGuiUtils.drawImage(image, availWidth, displayHeight);

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("Resolution: " + image.width() + "x" + image.height());
            ImGui.text("Path: " + sourcePath);
            ImGui.endTooltip();
        }
    }
}