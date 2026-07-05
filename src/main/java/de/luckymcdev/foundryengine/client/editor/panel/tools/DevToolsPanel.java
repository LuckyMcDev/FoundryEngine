package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.world.StorageSourceManager;
import de.luckymcdev.foundryengine.config.CommonConfig;
import imgui.ImGui;

public class DevToolsPanel extends Panel {
    public static final DevToolsPanel INSTANCE = new DevToolsPanel();

    private DevToolsPanel() {
        super(new Builder(Common.id("dev_tools"))
                .icon(ImIcons.FLASK)
                .category(PanelCategory.TOOLS));
    }

    @Override
    public void content(ImGraphicsExtractor g) {
        g.cardBegin("##pack_mode_card");
        ImGui.text(ImIcons.CUBES + "  Pack Mode");

        String current = CommonConfig.PACK_MODE.get();
        boolean isDev = current.equalsIgnoreCase("dev");

        ImGui.spacing();
        if (ImGui.checkbox("Dev Mode (edit original saves directly)", isDev)) {
            CommonConfig.PACK_MODE.set(!isDev ? "dev" : "");
            CommonConfig.PACK_MODE.save();
        }
        g.helpTooltip("When enabled, bundle saves are loaded directly without instancing. Changes will modify the original files.");

        ImGui.spacing();
        ImGui.text("Current mode: ");
        ImGui.sameLine();
        if (isDev) {
            ImGui.textColored(0xFFFFAA00, "Dev");
            g.helpTooltip("Changes will be written to the original bundle saves!");
        } else {
            ImGui.textColored(0xFF4CAF50, "Safe");
            g.helpTooltip("Each save is instanced. Original files are never modified.");
        }
        g.cardEnd();

        ImGui.spacing();

        g.cardBegin("##instancing_card");
        ImGui.text(ImIcons.DATABASE + "  Instancing");

        ImGui.spacing();
        if (ImGui.button(ImIcons.TRASH + " Clear Instances Cache", -1, 0)) {
            StorageSourceManager.clearInstanced();
        }
        g.helpTooltip("Clears the cache of instanced world copies. New copies will be created on next load.");
        g.cardEnd();
    }
}
