package io.github.luckymcdev.foundryengine.client.editor.builtin.editor;

import com.mojang.logging.LogUtils;
import imgui.ImGui;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiTreeNodeFlags;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.editor.builtin.editor.code.CodeEditor;
import io.github.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import io.github.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import io.github.luckymcdev.foundryengine.client.util.key.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A File explorer Panel to open the {@link CodeEditor} for editing any kind of file during
 * Runtime. Is disabled on anything other than Singleplayer
 */
public class FileExplorerPanel extends EditorPanel {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final FileExplorerPanel INSTANCE = new FileExplorerPanel();
    private final File rootDir = Common.DIRECTORY.toFile();

    public FileExplorerPanel() {
        super(Common.id("file_explorer"), "File Explorer", Shortcut.ctrl(ImGuiKey.F));
    }

    @Override
    public void content() {
        if (!Client.getMinecraft().isSingleplayer()) {
            ImGui.text("Not in singleplayer.");
            return;
        }
        ImGui.textDisabled("Project Root: " + rootDir.getName());
        ImGui.separator();

        if (ImGui.beginChild("ExplorerTree", 0, 0, false)) {
            render(rootDir);
            ImGui.endChild();
        }
    }

    private void render(File dir) {
        File[] files = dir.listFiles();
        if (files == null) return;

        // Sort files by dir, then files by alphabet
        Arrays.sort(files, Comparator.comparing(File::isFile).thenComparing(File::getName));

        for (File file : files) {
            if (file.isFile()) renderFile(file);
            if (file.isDirectory()) renderDirectory(file);
        }
    }

    private void renderFile(File file) {
        int flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth;
        String fileName = file.getName();
        String id = "##" + file.getPath();

        String fileIcon = getFileIcon(fileName);

        ImGui.treeNodeEx(id, flags, fileIcon + " " + fileName);

        if (ImGui.isItemClicked()) {
            openFileInEditor(file);
        }

    }

    private void renderDirectory(File directory) {
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth;
        String fileName = directory.getName();
        String id = "##" + directory.getPath();

        boolean isOpen = ImGui.treeNodeEx(id, flags, "");

        ImGui.sameLine();
        String folderIcon =
                isOpen ? EngineImGuiUtils.icon(ImIcons.FA.FA_FOLDER_OPEN) :
                        EngineImGuiUtils.icon(ImIcons.FA.FA_FOLDER);

        ImGui.textUnformatted(folderIcon + " " + fileName);

        if (isOpen) {
            render(directory);
            ImGui.treePop();
        }
    }

    private String getFileIcon(String fileName) {
        String name = fileName.toLowerCase();

        if (name.endsWith(".java") || name.endsWith(".groovy") || name.endsWith(".glsl") ||
                name.endsWith(".vert") || name.endsWith(".frag") || name.endsWith(".js")) {
            return EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_CODE);
        }

        if (name.endsWith(".json") || name.endsWith(".toml") || name.endsWith(".yaml") || name.endsWith(".yml")) {
            return EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_IMPORT);
        }

        if (name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".tga")) {
            return EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_IMAGE);
        }
        if (name.endsWith(".ogg") || name.endsWith(".wav") || name.endsWith(".mp3")) {
            return EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_AUDIO);
        }

        if (name.endsWith(".zip") || name.endsWith(".jar") || name.endsWith(".tar") || name.endsWith(".gz")) {
            return EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_ZIPPER);
        }

        if (name.endsWith(".txt") || name.endsWith(".log")) {
            return EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_TEXT);
        }
        if (name.endsWith(".md")) {
            return EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_PEN);
        }

        return EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_O);
    }

    private void openFileInEditor(File file) {
        try {
            String content = Files.readString(file.toPath());
            Identifier editorId = Common.id("editor_" + file.getName().toLowerCase().replaceAll("[^a-z0-9]", "_"));

            Panel existing = Client.getEditorManager().getPanels().get(editorId);
            if (existing instanceof CodeEditor editor) {
                editor.open();
                return;
            }

            CodeEditor newEditor = new CodeEditor(editorId, file.getName(), content);
            newEditor.setSaveCallback(newContent -> {
                try {
                    Files.writeString(file.toPath(), newContent);
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            });

            Client.getEditorManager().register(newEditor);
            newEditor.open();

        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }
}