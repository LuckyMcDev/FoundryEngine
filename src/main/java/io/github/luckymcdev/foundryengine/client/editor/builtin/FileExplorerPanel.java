package io.github.luckymcdev.foundryengine.client.editor.builtin;

import imgui.ImGui;
import imgui.flag.ImGuiKey;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.editor.code.CodeEditor;
import io.github.luckymcdev.foundryengine.client.util.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.resources.Identifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileExplorerPanel extends Panel {

    public static final FileExplorerPanel INSTANCE = new FileExplorerPanel();
    private final File rootDir = new File(Common.FOUNDRY_ENGINE.toAbsolutePath().toString());

    public FileExplorerPanel() {
        super(Common.id("file_explorer"), "File Explorer", Shortcut.ctrl(ImGuiKey.F));
    }

    @Override
    public void content() {
        if (ImGui.beginChild("ExplorerTree")) {
            renderDirectory(rootDir);
            ImGui.endChild();
        }
    }

    private void renderDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isHidden()) continue;

            if (file.isDirectory()) {
                if (ImGui.treeNode(file.getName() + "/")) {
                    renderDirectory(file);
                    ImGui.treePop();
                }
            } else {
                // Selectable file entry
                if (ImGui.selectable(file.getName())) {
                    openFileInEditor(file);
                }
            }
        }
    }

    private void openFileInEditor(File file) {
        try {
            String content = Files.readString(file.toPath());

            // Create a unique ID based on the file path so we don't open the same file twice
            Identifier editorId = Common.id("editor_" + file.getName().toLowerCase().replaceAll("[^a-z0-0]", "_"));

            // Check if an editor for this file is already registered/open
            Panel existing = Client.getEditorManager().getPanels().get(editorId);
            if (existing instanceof CodeEditor editor) {
                editor.open();
                return;
            }

            // Create a new instance
            CodeEditor newEditor = new CodeEditor(editorId, file.getName(), content);

            newEditor.setSaveCallback((newContent) -> {
                try {
                    Files.writeString(file.toPath(), newContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Register and open the new panel
            Client.getEditorManager().register(newEditor);
            newEditor.open();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}