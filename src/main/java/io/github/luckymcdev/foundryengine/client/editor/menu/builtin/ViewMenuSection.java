package io.github.luckymcdev.foundryengine.client.editor.menu.builtin;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.menu.MenuSection;

public class ViewMenuSection implements MenuSection {

    @Override
    public void render() {
        if (ImGui.beginMenu("View")) {
            renderResetLayout();
            ImGui.endMenu();
        }
    }

    private void renderResetLayout() {
        if (ImGui.menuItem("Reset Docking")) {
            resetDocking();
        }
    }

    private void resetDocking() {
        int dockspaceId = Client.getImGuiManager().getDockId();
        imgui.internal.ImGui.dockBuilderRemoveNodeChildNodes(dockspaceId);
        imgui.internal.ImGui.dockBuilderRemoveNodeDockedWindows(dockspaceId);
    }
}