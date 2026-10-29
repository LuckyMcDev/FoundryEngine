package de.luckymcdev.foundryengine.editor.client;

import de.luckymcdev.foundryengine.EngineClientMod;
import de.luckymcdev.foundryengine.Client;
import de.luckymcdev.foundryengine.editor.client.config.PanelCategory;
import de.luckymcdev.foundryengine.editor.client.menu.MenuSection;
import de.luckymcdev.foundryengine.editor.client.menu.ShortcutHandler;
import de.luckymcdev.foundryengine.editor.client.menu.builtin.CategoryMenuSection;
import de.luckymcdev.foundryengine.imgui.client.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.imgui.client.icon.ImIcons;
import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.registry.common.GenericRegistry;
import imgui.ImGui;
import imgui.flag.ImGuiStyleVar;

/**
 * The Main Menu implementation. Manages the top info bar and coordinates menu sections.
 * Uses lazy initialization to handle potential timing issues with Client initialization.
 */
public class MainMenu {
	private final GenericRegistry<String, MenuSection> menuSections = new GenericRegistry<>();
	private ShortcutHandler shortcutHandler;

	public void register() {
		var editor = Client.getEditorManager();
		this.shortcutHandler = new ShortcutHandler(editor);

		for (PanelCategory category : PanelCategory.values()) {
			if (isSubCategory(category)) {
				continue;
			}

			this.register(category.name().toLowerCase(),
				new CategoryMenuSection(editor, category, category.getMenuLabel()));
		}

		this.register("scriptreload", new MenuSection() {
			@Override
			public void render() {
				if (ImGui.menuItem("Reload Scripts " + ImIcons.ARROW_ROTATE_LEFT)) {
					Common.getBundleManager().reload();
				}
			}
		});
	}

	private boolean isSubCategory(PanelCategory category) {
		return category.name().contains("_");
	}

	public void register(String name, MenuSection section) {
		this.menuSections.register(name, section);
	}

	public void remove(String name) {
		this.menuSections.remove(name);
	}

	public void render() {
		var g = Client.getImGraphics();

		g.pushStack();
		g.setStyleVar(ImGuiStyleVar.FramePadding, 8, 7);
		if (ImGui.beginMainMenuBar()) {

			g.drawImage(ImGraphicsExtractor.getTexture(EngineClientMod.LOGO_TRANSPARENT), 32, 32);

			g.pushStack();
			menuSections.forEach(MenuSection::render);
			g.popStack();

			ImGui.endMainMenuBar();
		}
		g.popStack();
	}

	public void handleShortcuts() {
		shortcutHandler.handleShortcuts();
		Client.getHotKeyManager().update();
	}

	public void handleRender() {
		handleShortcuts();
		render();
	}
}