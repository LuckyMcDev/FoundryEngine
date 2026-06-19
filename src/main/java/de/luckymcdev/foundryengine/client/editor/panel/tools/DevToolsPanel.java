package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.imgui.ImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.world.StorageSourceManager;
import de.luckymcdev.foundryengine.config.CommonConfig;
import imgui.ImGui;

public class DevToolsPanel extends Panel {
    public static final DevToolsPanel INSTANCE = new DevToolsPanel();

    private DevToolsPanel() {
        super(new Builder(Common.id("dev_tools"), "Dev Tools")
                .icon(ImIcons.FA.FA_FLASK)
                .category(PanelCategory.TOOLS));
    }

    @Override
    public void content() {
        ImGuiUtils.section("Pack Mode");

        String current = CommonConfig.PACK_MODE.get();
        boolean isDev = current.equalsIgnoreCase("dev");

        if (ImGui.checkbox("Dev Mode (edit original saves directly)", isDev)) {
            CommonConfig.PACK_MODE.set(!isDev ? "dev" : "");
            CommonConfig.PACK_MODE.save();
        }
        ImGuiUtils.helpTooltip("When enabled, bundle saves are loaded directly without instancing. Changes will modify the original files.");

        ImGui.text("Current pack mode: " + (current.isEmpty() ? "(none)" : current));

        if (isDev) {
            ImGui.textColored(0xFFFFAA00, "Warning: Changes will be written to the original bundle saves!");
        }

        ImGui.dummy(0, 8);
        ImGuiUtils.section("Instancing");

        if (ImGui.button("Clear Instances Cache")) {
            StorageSourceManager.clearInstanced();
        }
        ImGuiUtils.helpTooltip("Clears the cache of instanced world copies. New copies will be created on next load.");
    }
}
