package io.github.luckymcdev.foundryengine.client.editor;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.menu.MenuSection;
import io.github.luckymcdev.foundryengine.client.editor.menu.ShortcutHandler;
import io.github.luckymcdev.foundryengine.client.editor.menu.builtin.EditorMenuSection;
import io.github.luckymcdev.foundryengine.client.editor.menu.builtin.OpenMenuSection;
import io.github.luckymcdev.foundryengine.client.editor.menu.builtin.ToolsMenuSection;
import io.github.luckymcdev.foundryengine.client.editor.menu.builtin.ViewMenuSection;
import io.github.luckymcdev.foundryengine.client.imgui.graphics.ImGuiGraphicsStack;
import io.github.luckymcdev.foundryengine.common.registry.GenericRegistry;

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