package de.luckymcdev.foundryengine.editor.client.menu.builtin;

import de.luckymcdev.foundryengine.editor.client.EditorManager;
import de.luckymcdev.foundryengine.editor.client.config.PanelCategory;
import de.luckymcdev.foundryengine.editor.client.menu.MenuSection;
import de.luckymcdev.foundryengine.editor.client.menu.PanelMenuItemRenderer;
import de.luckymcdev.foundryengine.editor.client.panel.Panel;
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
				if (sub.isChildOf(this.category)) {
					if (ImGui.beginMenu(sub.getMenuLabel())) {
						renderPanelsForCategory(sub);
						ImGui.endMenu();
					}
				}
			}
			ImGui.endMenu();
		}
	}

	private void renderPanelsForCategory(PanelCategory cat) {
		for (Panel panel : editor.getPanels()) {
			if (panel.getCategory() == cat) {
				menuItemRenderer.render(panel);
			}
		}
	}
}