package de.luckymcdev.foundryengine.client.editor;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.ShortcutHandler;
import de.luckymcdev.foundryengine.client.editor.menu.builtin.CategoryMenuSection;
import de.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
import de.luckymcdev.foundryengine.common.registry.GenericRegistry;
import imgui.ImGui;

/**
 * The Main Menu implementation. Manages the top info bar and coordinates menu sections.
 * Uses lazy initialization to handle potential timing issues with Client initialization.
 */
public class MainMenu {
    private final GenericRegistry<String, MenuSection> menuSections = new GenericRegistry<>();
    private ImGuiGraphicsStack graphicsStack;
    private ShortcutHandler shortcutHandler;

    public void register() {
        var editor = Client.getEditorManager();
        this.graphicsStack = Client.getImGuiManager().getGraphicsStack();
        this.shortcutHandler = new ShortcutHandler(editor);

        for (PanelCategory category : PanelCategory.values()) {
            if (isSubCategory(category)) continue;

            String label = category.getMenuLabel();
            this.register(category.name().toLowerCase(),
                    new CategoryMenuSection(editor, category, label));
        }
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
        if (ImGui.beginMainMenuBar()) {
            graphicsStack.push();
            menuSections.forEach(MenuSection::render);
            graphicsStack.pop();

            ImGui.endMainMenuBar();
        }
    }

    public void handleShortcuts() {
        shortcutHandler.handleShortcuts();
    }

    public void handleRender() {
        handleShortcuts();
        render();
    }
}