package de.luckymcdev.foundryengine.client.editor.builtin.files;

import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import imgui.ImGui;
import net.minecraft.resources.Identifier;

import java.io.File;

public class TextureViewerPanel extends EditorPanel {
    private final ImGuiUtils.Image image;
    private final String sourcePath;

    public TextureViewerPanel(Identifier id, String title, Identifier identifier) {
        super(id, title, ImIcons.FA.FA_IMAGES, Shortcut.empty());
        this.image = ImGuiUtils.getTexture(identifier);
        this.sourcePath = identifier.toString();
        this.category = PanelCategory.EDITOR_FILES;
    }

    public TextureViewerPanel(Identifier id, String title, File file) {
        super(id, title, ImIcons.FA.FA_IMAGES, Shortcut.empty());
        this.image = ImGuiUtils.getTexture(file);
        this.sourcePath = file.getAbsolutePath();
        this.category = PanelCategory.EDITOR_FILES;
    }

    @Override
    public void content() {
        if (image.glId() == -1) {
            ImGui.text("Failed to load texture: " + sourcePath);
            return;
        }

        float availWidth = ImGui.getContentRegionAvailX();
        float displayHeight = (availWidth / image.width()) * image.height();

        ImGuiUtils.drawImage(image.glId(), availWidth, displayHeight);

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text("Resolution: " + image.width() + "x" + image.height());
            ImGui.text("Path: " + sourcePath);
            ImGui.endTooltip();
        }
    }
}