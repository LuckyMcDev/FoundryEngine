package de.luckymcdev.foundryengine.client.editor;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.ShortcutHandler;
import de.luckymcdev.foundryengine.client.editor.menu.builtin.EditorMenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.builtin.OpenMenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.builtin.ToolsMenuSection;
import de.luckymcdev.foundryengine.client.editor.menu.builtin.ViewMenuSection;
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

    public MainMenu() {
        // Constructor is lightweight - actual initialization happens on first use
    }

    public void register() {
        var editor = Client.getEditorManager();
        this.graphicsStack = Client.getImGuiManager().getGraphicsStack();
        this.shortcutHandler = new ShortcutHandler(editor);

        this.register("open", new OpenMenuSection(editor));
        this.register("editor", new EditorMenuSection(editor));
        this.register("tools", new ToolsMenuSection(editor));
        this.register("view", new ViewMenuSection());
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
}