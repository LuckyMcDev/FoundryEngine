package de.luckymcdev.foundryengine.client.editor.builtin.browser;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.TextureViewerPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.code.CodeEditor;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import de.luckymcdev.foundryengine.common.util.FileEndings;
import imgui.ImGui;
import imgui.ImVec4;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;

/**
 * A file-explorer panel that opens the {@link CodeEditor} for editing any file at runtime.
 * Disabled outside singleplayer.
 */
public class FileExplorerPanel extends AbstractExplorerPanel {
    public static final FileExplorerPanel INSTANCE = new FileExplorerPanel(Common.DIRECTORY.toFile());
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm").withZone(ZoneId.systemDefault());

    private final File rootDir;
    private final ImString searchFilter = new ImString(256);
    private @Nullable String lastError = null;
    private boolean rootReadable = true;
    private ExplorerNode.FileExplorerNode rootNode;

    public FileExplorerPanel(File rootDir) {
        super(Common.id("file_explorer"), "File Explorer", Shortcut.ctrl(ImGuiKey.F2));
        this.rootDir = rootDir;
        this.category = PanelCategory.EDITOR_EXPLORER;
    }

    private static String handleSizeTooltip(File file) {
        try {
            var attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            long bytes = attrs.size();

            if (bytes < 1024) {
                return bytes + " B";
            }

            if (bytes < 1024 * 1024) {
                return String.format("%.1f KB", bytes / 1024.0);
            } else {
                return String.format("%.1f MB", bytes / (1024.0 * 1024));
            }

        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    private static Identifier fileToEditorId(File file) {
        String sanitised = file.getAbsolutePath()
                .toLowerCase()
                .replaceAll("[^a-z0-9]", "_");
        return Common.id("editor_" + sanitised);
    }

    private static File uniqueFile(File dir, String baseName, @Nullable String extension) {
        String suffix = extension != null ? "." + extension : "";
        File candidate = new File(dir, baseName + suffix);
        int n = 1;
        while (candidate.exists()) {
            candidate = new File(dir, baseName + "_" + n + suffix);
            n++;
        }
        return candidate;
    }

    @Override
    protected void refresh() {
        rootNode = new ExplorerNode.FileExplorerNode("root", rootDir);
        buildFileTree(rootDir, rootNode);
        rootReadable = true;
        initialized = true;
    }

    /**
     * Recursively build the file tree structure.
     */
    private void buildFileTree(File dir, ExplorerNode.FileExplorerNode parentNode) {
        File[] files = dir.listFiles();
        if (files == null) {
            rootReadable = dir != rootDir && rootReadable;
            return;
        }

        // Sort directories first, then files — each group alphabetically
        Arrays.sort(files, Comparator
                .comparing(File::isFile)
                .thenComparing(f -> f.getName().toLowerCase()));

        for (File file : files) {
            if (file.isDirectory()) {
                ExplorerNode.FileExplorerNode dirNode = new ExplorerNode.FileExplorerNode(file.getName(), file);
                parentNode.addChild(file.getName(), dirNode);
                buildFileTree(file, dirNode);
            } else {
                parentNode.files.add(file);
            }
        }
    }

    @Override
    protected void renderBrowser() {
        if (!Client.getMinecraft().isSingleplayer()) {
            ImGui.textColored(new ImVec4(1.0f, 0.5f, 0.0f, 1.0f),
                    "File Explorer is unavailable in multiplayer to prevent unauthorized file access.");
            return;
        }

        renderToolbar();

        // Inline error banner
        if (lastError != null) {
            ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.35f, 0.35f, 1.0f);
            ImGui.textWrapped(ImIcons.FA.FA_EXCLAMATION_TRIANGLE + " " + lastError);
            ImGui.popStyleColor();
            ImGui.sameLine();
            if (ImGui.smallButton("×")) lastError = null;
        }

        if (!rootReadable) {
            ImGui.textDisabled("Root directory is not accessible.");
            return;
        }

        ImGui.separator();

        // Keyboard shortcut: Ctrl+R → refresh
        if (ImGui.isWindowFocused() && ImGui.getIO().getKeyCtrl()
                && ImGui.isKeyPressed(ImGuiKey.R)) {
            clearError();
        }

        // Main scrollable tree
        if (ImGui.beginChild("##explorerTree", 0, 0, false)) {
            boolean filtering = !searchFilter.get().isBlank();
            if (filtering) {
                renderFilteredFiles(rootDir, searchFilter.get().trim().toLowerCase());
            } else if (rootNode != null) {
                renderFileNode(rootNode);
            }
        }
        ImGui.endChild();
    }

    private void renderToolbar() {
        // Project root label
        ImGui.textDisabled(rootDir.getName() + "/");
        ImGui.sameLine();

        // Push the buttons to the right
        float rightEdge = ImGui.getContentRegionAvailX();
        float buttonWidth = ImGui.getFrameHeight(); // square icon buttons
        float spacing = ImGui.getStyle().getItemSpacingX();

        // Search field — takes the remaining width minus the two icon buttons
        float searchWidth = rightEdge - (buttonWidth + spacing) * 2 - spacing;
        ImGui.setNextItemWidth(Math.max(searchWidth, 60.0f));
        if (ImGui.inputTextWithHint("##search", ImIcons.FA.FA_SEARCH + " Filter…", searchFilter)) {
            // filter updates live — no action needed
        }

        // Clear search with Escape when the field is focused
        if (ImGui.isItemFocused() && ImGui.isKeyPressed(ImGuiKey.Escape)) {
            searchFilter.set("");
        }

        if (!searchFilter.get().isEmpty()) {
            ImGui.sameLine();
            if (ImGui.smallButton("×##clearSearch")) searchFilter.set("");
        }

        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_ARROW_ROTATE_RIGHT + "##refresh", buttonWidth, 0)) {
            refresh();
            clearError();
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Refresh  (Ctrl+R)");
    }

    /**
     * Render a file browser node and its children.
     */
    private void renderFileNode(ExplorerNode.FileExplorerNode node) {
        // Skip rendering the root's container, just render its children
        if ("root" .equals(node.name)) {
            renderFileNodeChildren(node);
            return;
        }

        int flags = ImGuiTreeNodeFlags.SpanAvailWidth;
        String id = "##dir_" + node.file.getPath();
        boolean isOpen = ImGui.treeNodeEx(id, flags, "");

        ImGui.sameLine();
        String folderIcon = isOpen
                ? EngineImGuiUtils.icon(ImIcons.FA.FA_FOLDER_OPEN)
                : EngineImGuiUtils.icon(ImIcons.FA.FA_FOLDER);
        ImGui.textUnformatted(folderIcon + " " + node.name);

        renderDirectoryContextMenu(node.file, id + "_ctx");

        if (isOpen) {
            renderFileNodeChildren(node);
            ImGui.treePop();
        }
    }

    /**
     * Render children of a file node (subdirectories and files).
     */
    private void renderFileNodeChildren(ExplorerNode.FileExplorerNode node) {
        if (node.isEmpty()) {
            ImGui.indent();
            ImGui.textDisabled("(empty)");
            ImGui.unindent();
            return;
        }

        // Render subdirectories
        for (ExplorerNode child : node.getChildren()) {
            if (child instanceof ExplorerNode.FileExplorerNode fileNode) {
                renderFileNode(fileNode);
            }
        }

        // Render files in this directory
        for (File file : node.files) {
            renderFileItem(file);
        }
    }

    /**
     * Render a file entry with context menu and tooltip.
     */
    private void renderFileItem(File file) {
        int flags = ImGuiTreeNodeFlags.Leaf
                | ImGuiTreeNodeFlags.NoTreePushOnOpen
                | ImGuiTreeNodeFlags.SpanAvailWidth;
        String id = "##file_" + file.getPath();
        String fileName = file.getName();
        String fileIcon = FileEndings.getFileIcon(fileName);

        // Highlight already-open files
        boolean isOpen = isFileOpen(file);
        if (isOpen) ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getStyle().getColor(ImGuiCol.CheckMark));

        ImGui.treeNodeEx(id, flags, fileIcon + " " + fileName);

        if (isOpen) ImGui.popStyleColor();

        // Left-click → open
        if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
            openFileInEditor(file);
        }

        // Right-click → context menu
        renderFileContextMenu(file, id + "_ctx");

        // Hover tooltip
        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text(fileName);
            ImGui.separator();
            appendFileMetaTooltip(file);
            if (isOpen) {
                ImGui.separator();
                ImGui.textDisabled("Currently open in editor");
            }
            ImGui.endTooltip();
        }
    }

    /**
     * Render filtered file results (search).
     */
    private boolean renderFilteredFiles(File dir, String query) {
        File[] files = dir.listFiles();
        if (files == null) return false;

        Arrays.sort(files, Comparator
                .comparing(File::isFile)
                .thenComparing(f -> f.getName().toLowerCase()));

        boolean anyMatch = false;

        for (File file : files) {
            if (file.isDirectory()) {
                boolean childMatch = renderFilteredFiles(file, query);
                anyMatch |= childMatch;
            } else {
                if (file.getName().toLowerCase().contains(query)) {
                    renderFileItem(file);
                    anyMatch = true;
                }
            }
        }

        return anyMatch;
    }

    private void renderFileContextMenu(File file, String popupId) {
        if (ImGui.beginPopupContextItem(popupId)) {
            if (ImGui.menuItem(ImIcons.FA.FA_EDIT + "  Open")) {
                openFileInEditor(file);
            }
            ImGui.separator();
            if (ImGui.menuItem(ImIcons.FA.FA_COPY + "  Copy Path")) {
                ImGui.setClipboardText(file.getAbsolutePath());
            }
            if (ImGui.menuItem(ImIcons.FA.FA_FOLDER_OPEN + "  Reveal in Explorer")) {
                revealInExplorer(file.getParentFile());
            }
            ImGui.endPopup();
        }
    }

    private void renderDirectoryContextMenu(File dir, String popupId) {
        if (ImGui.beginPopupContextItem(popupId)) {
            if (ImGui.menuItem(ImIcons.FA.FA_COPY + "  Copy Path")) {
                ImGui.setClipboardText(dir.getAbsolutePath());
            }
            if (ImGui.menuItem(ImIcons.FA.FA_FOLDER_OPEN + "  Reveal in Explorer")) {
                revealInExplorer(dir);
            }
            ImGui.separator();
            if (ImGui.menuItem(ImIcons.FA.FA_FILE + "  New File…")) {
                createNewFileIn(dir);
            }
            if (ImGui.menuItem(ImIcons.FA.FA_FOLDER + "  New Folder…")) {
                createNewFolderIn(dir);
            }
            ImGui.endPopup();
        }
    }

    @Override
    protected void openResource(Identifier id) {
        // Not used for file explorer — files are opened directly
    }

    private void openFileInEditor(File file) {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".png") || fileName.endsWith(".jpg")) {
            Identifier viewerId = Common.id("tex_viewer_" + file.getAbsolutePath().hashCode());

            // Reuse existing viewer if open
            if (Client.getEditorManager().getPanels().get(viewerId) instanceof TextureViewerPanel viewer) {
                viewer.open();
                return;
            }

            TextureViewerPanel viewer = new TextureViewerPanel(viewerId, "Texture: " + file.getName(), file);
            Client.getEditorManager().register(viewer);
            viewer.open();
            return;
        }

        try {
            String content = Files.readString(file.toPath());
            Identifier editorId = fileToEditorId(file);

            // Reuse existing editor if open
            CodeEditor existing = getExistingEditor(editorId);
            if (existing != null) {
                return;
            }

            CodeEditor newEditor = buildCodeEditor(file, editorId, content);
            Client.getEditorManager().register(newEditor);
            newEditor.open();

        } catch (IOException e) {
            setError("Could not open \"" + file.getName() + "\": " + e.getLocalizedMessage());
        }
    }

    private CodeEditor buildCodeEditor(File file, Identifier editorId, String content) {
        CodeEditor editor = new CodeEditor(editorId, file.getName(), content);

        TextEditorLanguageDefinition lang = FileEndings.getLanguageDefinitionByFileName(file.getName());
        if (lang != null) {
            editor.getTextEditor().setLanguageDefinition(lang);
            editor.customLangOverride = true;
        }

        editor.setSaveCallback((source, errors) -> {
            try {
                Files.writeString(file.toPath(), source);
            } catch (IOException e) {
                setError("Save failed for \"" + file.getName() + "\": " + e.getLocalizedMessage());
                LOGGER.error("Failed to save {}: {}", file.getAbsolutePath(), e.getLocalizedMessage());
            }
        });

        return editor;
    }

    private void createNewFileIn(File dir) {
        File newFile = uniqueFile(dir, "new_file", "txt");
        try {
            if (newFile.createNewFile()) {
                refresh(); // Refresh tree to show new file
                openFileInEditor(newFile);
            }
        } catch (IOException e) {
            setError("Could not create file: " + e.getLocalizedMessage());
        }
    }

    private void createNewFolderIn(File dir) {
        File newDir = uniqueFile(dir, "new_folder", null);
        if (!newDir.mkdir()) {
            setError("Could not create folder: " + newDir.getName());
        } else {
            refresh(); // Refresh tree to show new folder
        }
    }

    private void revealInExplorer(File dir) {
        try {
            Desktop desktop = java.awt.Desktop.getDesktop();
            desktop.open(dir);
        } catch (Exception ignored) {
            // If that doesn't work, just ignore it :)
        }
    }

    private void appendFileMetaTooltip(File file) {
        String size = handleSizeTooltip(file);
        String modified = DATE_FMT.format(Instant.ofEpochMilli(file.lastModified()));
        ImGui.textDisabled("Size:     " + size);
        ImGui.textDisabled("Modified: " + modified);
    }

    private boolean isFileOpen(File file) {
        return isResourceOpen(fileToEditorId(file));
    }

    private void setError(String message) {
        this.lastError = message;
        LOGGER.error(message);
    }

    private void clearError() {
        this.lastError = null;
        this.rootReadable = true;
    }
}