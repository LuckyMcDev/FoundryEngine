package de.luckymcdev.foundryengine.client.editor.menu.builtin;

import de.luckymcdev.foundryengine.client.editor.EditorManager;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.PanelMenuItemRenderer;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
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