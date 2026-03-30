package de.luckymcdev.foundryengine.client.editor.menu.builtin;

import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.PanelMenuItemRenderer;
import imgui.ImGui;

public class CategoryMenuSection implements MenuSection {
    private final EditorManager editor;
    private final PanelMenuItemRenderer menuItemRenderer;
    private final PanelCategory category;
    private final String menuLabel;

    public CategoryMenuSection(EditorManager editor, PanelCategory category, String menuLabel) {
        this.editor = editor;
        this.category = category;
        this.menuLabel = menuLabel;
        this.menuItemRenderer = new PanelMenuItemRenderer(editor);
    }

    @Override
    public void render() {
        if (ImGui.beginMenu(menuLabel)) {
            renderPanelsForCategory(this.category);

            for (PanelCategory sub : PanelCategory.values()) {
                if (sub.name().startsWith(this.category.name() + "_")) {
                    String subLabel = sub.name().replace(this.category.name() + "_", "");
                    if (ImGui.beginMenu(capitalize(subLabel))) {
                        renderPanelsForCategory(sub);
                        ImGui.endMenu();
                    }
                }
            }
            ImGui.endMenu();
        }
    }

    private void renderPanelsForCategory(PanelCategory cat) {
        editor.getPanels().stream()
                .filter(p -> p.category == cat)
                .forEach(menuItemRenderer::render);
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}