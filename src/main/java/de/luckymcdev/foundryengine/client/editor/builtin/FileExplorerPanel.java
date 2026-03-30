package de.luckymcdev.foundryengine.client.editor.builtin;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.Panel;
import de.luckymcdev.foundryengine.client.editor.builtin.code.CodeEditor;
import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.FileEndings;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiTreeNodeFlags;
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
        super(Common.id("file_explorer"), "File Explorer", Shortcut.ctrl(ImGuiKey.F2));
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

    private static CodeEditor getCodeEditor(File file, Identifier editorId, String content) {
        CodeEditor newEditor = new CodeEditor(editorId, file.getName(), content);

        TextEditorLanguageDefinition lang = FileEndings.getLanguageDefinitionByFileName(file.getName());

        newEditor.getTextEditor().setLanguageDefinition(lang);
        newEditor.customLangOverride = true;

        newEditor.setSaveCallback((source, errors) -> {
            if (errors.isEmpty()) {
                try {
                    Files.writeString(file.toPath(), source);
                } catch (IOException e) {
                    LOGGER.error(e.getLocalizedMessage());
                }
            } else {
                LOGGER.error(errors.toString());
            }
        });

        return newEditor;
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

    private void renderFile(File file) {
        int flags = ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth;
        String fileName = file.getName();
        String id = "##" + file.getPath();

        String fileIcon = FileEndings.getFileIcon(fileName);

        ImGui.treeNodeEx(id, flags, fileIcon + " " + fileName);

        if (ImGui.isItemClicked()) {
            openFileInEditor(file);
        }

    }

    private void openFileInEditor(File file) {
        try {
            String content = Files.readString(file.toPath());
            String uniquePathId = file.getAbsolutePath().toLowerCase().replaceAll("[^a-z0-9]", "_");
            Identifier editorId = Common.id("editor_" + uniquePathId);

            Panel existing = Client.getEditorManager().getPanels().get(editorId);
            if (existing instanceof CodeEditor editor) {
                editor.open();
                return;
            }

            CodeEditor newEditor = getCodeEditor(file, editorId, content);

            Client.getEditorManager().register(newEditor);
            newEditor.open();

        } catch (IOException e) {
            LOGGER.error(e.getLocalizedMessage());
        }
    }
}