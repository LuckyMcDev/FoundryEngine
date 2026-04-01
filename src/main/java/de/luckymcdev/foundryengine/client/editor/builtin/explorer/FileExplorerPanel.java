package de.luckymcdev.foundryengine.client.editor.builtin.explorer;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.files.CodeEditor;
import de.luckymcdev.foundryengine.client.editor.builtin.files.TextureViewerPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import de.luckymcdev.foundryengine.common.network.packets.explorer.*;
import de.luckymcdev.foundryengine.common.util.FileEndings;
import imgui.ImGui;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
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
import java.util.List;

/**
 * A file-explorer panel showing both local client files and remote server files
 * when connected to a multiplayer server. Remote files are fetched on demand via
 * packets and opened in a {@link CodeEditor} whose save callback writes back to the server.
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
    private ExplorerNode.@Nullable RemoteExplorerNode remoteRootNode = null;
    private boolean remoteRequested = false;
    private boolean remoteLoading = false;

    public FileExplorerPanel(File rootDir) {
        super(Common.id("file_explorer"), "File Explorer", ImIcons.FA.FA_FILES_O, Shortcut.ctrl(ImGuiKey.F2));
        this.rootDir = rootDir;
        this.category = PanelCategory.EDITOR_EXPLORER;
    }

    private static Identifier remoteFileEditorId(String relativePath) {
        String sanitised = ("remote_" + relativePath).toLowerCase().replaceAll("[^a-z0-9]", "_");
        return Common.id("editor_" + sanitised);
    }

    private static boolean isMultiplayer() {
        Minecraft mc = Minecraft.getInstance();
        return mc.getCurrentServer() != null;
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

    private static String handleSizeTooltip(File file) {
        try {
            var attrs = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
            long bytes = attrs.size();
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            return String.format("%.1f MB", bytes / (1024.0 * 1024));
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    public void receiveRemoteFileList(String rootPath, List<ClientBoundFileListPacket.RemoteEntry> entries) {
        ExplorerNode.RemoteExplorerNode root = new ExplorerNode.RemoteExplorerNode("Server (" + Common.MODNAME + ")", "", true);

        for (ClientBoundFileListPacket.RemoteEntry entry : entries) {
            String[] parts = entry.relativePath().split("/");
            ExplorerNode.RemoteExplorerNode current = root;
            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                boolean isLast = (i == parts.length - 1);
                String partialPath = String.join("/", Arrays.copyOfRange(parts, 0, i + 1));

                if (isLast) {
                    ExplorerNode.RemoteExplorerNode node = new ExplorerNode.RemoteExplorerNode(part, entry.relativePath(), entry.isDirectory());
                    if (entry.isDirectory()) {
                        current.children.putIfAbsent(part, node);
                    } else {
                        // Check we haven't already added it via directory traversal
                        boolean alreadyPresent = current.files.stream()
                                .anyMatch(f -> f.relativePath.equals(entry.relativePath()));
                        if (!alreadyPresent) {
                            current.files.add(node);
                        }
                    }
                } else {
                    current = (ExplorerNode.RemoteExplorerNode) current.children.computeIfAbsent(part,
                            k -> new ExplorerNode.RemoteExplorerNode(k, partialPath, true));
                }
            }
        }

        remoteRootNode = root;
        remoteLoading = false;
    }

    /**
     * Called when {@link ClientBoundFileContentPacket} arrives.
     */
    public void receiveRemoteFileContent(String relativePath, String content) {
        String fileName = relativePath.contains("/")
                ? relativePath.substring(relativePath.lastIndexOf('/') + 1)
                : relativePath;

        Identifier editorId = remoteFileEditorId(relativePath);

        if (getExistingEditor(editorId) != null) return;

        CodeEditor editor = new CodeEditor(editorId, "[SERVER] " + fileName, content);

        TextEditorLanguageDefinition lang = FileEndings.getLanguageDefinitionByFileName(fileName);
        if (lang != null) {
            editor.getTextEditor().setLanguageDefinition(lang);
            editor.customLangOverride = true;
        }

        // Save callback sends content back to the server
        editor.setSaveCallback((source, errors) ->
                ClientPacketDistributor.sendToServer(new ServerBoundSaveFilePacket(relativePath, source))
        );

        Client.getEditorManager().register(editor);
        editor.open();
    }

    @Override
    protected void refresh() {
        rootNode = new ExplorerNode.FileExplorerNode("root", rootDir);
        buildFileTree(rootDir, rootNode);
        rootReadable = true;
        initialized = true;

        // Also refresh remote tree if we're on a server
        if (isMultiplayer()) {
            requestRemoteFileList();
        }
    }

    @Override
    protected void renderBrowser() {
        //TODO: Fix this so that we dont need a level to check permissions
        //TODO: Or i guess do it some other way so that there isnt a check going on here at all?
        //TODO: Maybe make it so that the permissions check is only active if youre in a server? <-- Tried to do this.
        if (!EngineImGuiUtils.requireFull()) return;
        renderToolbar();

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

        if (ImGui.isWindowFocused() && ImGui.getIO().getKeyCtrl()
                && ImGui.isKeyPressed(ImGuiKey.R)) {
            clearError();
        }

        if (ImGui.beginChild("##explorerTree", 0, 0, false)) {
            boolean filtering = !searchFilter.get().isBlank();

            // ---- CLIENT section ----
            renderSectionHeader(
                    EngineImGuiUtils.icon(ImIcons.FA.FA_DESKTOP) + " Client",
                    "##section_client",
                    () -> {
                        if (filtering) {
                            renderFilteredFiles(rootDir, searchFilter.get().trim().toLowerCase());
                        } else if (rootNode != null) {
                            renderFileNode(rootNode);
                        }
                    }
            );

            // ---- SERVER section (multiplayer only) ----
            if (isMultiplayer()) {
                ImGui.spacing();
                renderSectionHeader(
                        EngineImGuiUtils.icon(ImIcons.FA.FA_SERVER) + " Server",
                        "##section_server",
                        () -> {
                            if (remoteLoading) {
                                ImGui.textDisabled("Loading…");
                            } else if (remoteRootNode == null) {
                                if (!remoteRequested) {
                                    requestRemoteFileList();
                                }
                                ImGui.textDisabled("Fetching file list…");
                            } else {
                                if (filtering) {
                                    renderFilteredRemoteNodes(remoteRootNode, searchFilter.get().trim().toLowerCase());
                                } else {
                                    renderRemoteNodeChildren(remoteRootNode);
                                }
                            }
                        }
                );
            }
        }
        ImGui.endChild();
    }

    /**
     * Renders a collapsible top-level section with a bold-ish label.
     */
    private void renderSectionHeader(String label, String id, Runnable body) {
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth | ImGuiTreeNodeFlags.DefaultOpen | ImGuiTreeNodeFlags.Framed;
        if (ImGui.treeNodeEx(id, flags, label)) {
            body.run();
            ImGui.treePop();
        }
    }

    @Override
    protected void openResource(Identifier id) {
        // Not used for file explorer — files are opened directly
    }


    private void buildFileTree(File dir, ExplorerNode.FileExplorerNode parentNode) {
        File[] files = dir.listFiles();
        if (files == null) {
            rootReadable = dir != rootDir && rootReadable;
            return;
        }

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

    private void renderFileNode(ExplorerNode.FileExplorerNode node) {
        if ("root".equals(node.name)) {
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

    private void renderFileNodeChildren(ExplorerNode.FileExplorerNode node) {
        if (node.isEmpty()) {
            ImGui.indent();
            ImGui.textDisabled("(empty)");
            ImGui.unindent();
            return;
        }

        for (ExplorerNode child : node.getChildren()) {
            if (child instanceof ExplorerNode.FileExplorerNode fileNode) {
                renderFileNode(fileNode);
            }
        }

        for (File file : node.files) {
            renderFileItem(file);
        }
    }

    private void renderFileItem(File file) {
        int flags = ImGuiTreeNodeFlags.Leaf
                | ImGuiTreeNodeFlags.NoTreePushOnOpen
                | ImGuiTreeNodeFlags.SpanAvailWidth;
        String id = "##file_" + file.getPath();
        String fileName = file.getName();
        String fileIcon = FileEndings.getFileIcon(fileName);

        boolean isOpen = isFileOpen(file);
        if (isOpen) ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getStyle().getColor(ImGuiCol.CheckMark));

        ImGui.treeNodeEx(id, flags, fileIcon + " " + fileName);

        if (isOpen) ImGui.popStyleColor();

        if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
            openFileInEditor(file);
        }

        renderFileContextMenu(file, id + "_ctx");

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


    private boolean renderFilteredFiles(File dir, String query) {
        File[] files = dir.listFiles();
        if (files == null) return false;

        Arrays.sort(files, Comparator
                .comparing(File::isFile)
                .thenComparing(f -> f.getName().toLowerCase()));

        boolean anyMatch = false;
        for (File file : files) {
            if (file.isDirectory()) {
                anyMatch |= renderFilteredFiles(file, query);
            } else if (file.getName().toLowerCase().contains(query)) {
                renderFileItem(file);
                anyMatch = true;
            }
        }
        return anyMatch;
    }

    private void renderRemoteNode(ExplorerNode.RemoteExplorerNode node) {
        if (node.isDirectory) {
            int flags = ImGuiTreeNodeFlags.SpanAvailWidth;
            String id = "##rdir_" + node.relativePath;
            boolean isOpen = ImGui.treeNodeEx(id, flags, "");
            ImGui.sameLine();
            String folderIcon = isOpen
                    ? EngineImGuiUtils.icon(ImIcons.FA.FA_FOLDER_OPEN)
                    : EngineImGuiUtils.icon(ImIcons.FA.FA_FOLDER);
            ImGui.textUnformatted(folderIcon + " " + node.name);

            if (isOpen) {
                renderRemoteNodeChildren(node);
                ImGui.treePop();
            }
        } else {
            renderRemoteFileItem(node);
        }
    }

    private void renderRemoteNodeChildren(ExplorerNode.RemoteExplorerNode node) {
        if (node.isEmpty()) {
            ImGui.indent();
            ImGui.textDisabled("(empty)");
            ImGui.unindent();
            return;
        }

        for (ExplorerNode child : node.children.values()) {
            var remoteChild = (ExplorerNode.RemoteExplorerNode) child;
            renderRemoteNode(remoteChild);
        }
        for (ExplorerNode.RemoteExplorerNode file : node.files) {
            renderRemoteFileItem(file);
        }
    }

    private void renderRemoteFileItem(ExplorerNode.RemoteExplorerNode node) {
        int flags = ImGuiTreeNodeFlags.Leaf
                | ImGuiTreeNodeFlags.NoTreePushOnOpen
                | ImGuiTreeNodeFlags.SpanAvailWidth;
        String id = "##rfile_" + node.relativePath;
        String fileIcon = FileEndings.getFileIcon(node.name);

        boolean isOpen = isResourceOpen(remoteFileEditorId(node.relativePath));
        if (isOpen) ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getStyle().getColor(ImGuiCol.CheckMark));

        ImGui.treeNodeEx(id, flags, fileIcon + " " + node.name);

        if (isOpen) ImGui.popStyleColor();

        if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
            openRemoteFile(node.relativePath);
        }

        if (ImGui.isItemHovered()) {
            ImGui.beginTooltip();
            ImGui.text(node.name);
            ImGui.separator();
            ImGui.textDisabled("Remote: " + node.relativePath);
            if (isOpen) {
                ImGui.separator();
                ImGui.textDisabled("Currently open in editor");
            }
            ImGui.endTooltip();
        }
    }

    private boolean renderFilteredRemoteNodes(ExplorerNode.RemoteExplorerNode node, String query) {
        boolean anyMatch = false;
        for (ExplorerNode child : node.children.values()) {
            var remoteChild = (ExplorerNode.RemoteExplorerNode) child;
            anyMatch |= renderFilteredRemoteNodes(remoteChild, query);
        }
        for (ExplorerNode.RemoteExplorerNode file : node.files) {
            if (file.name.toLowerCase().contains(query)) {
                renderRemoteFileItem(file);
                anyMatch = true;
            }
        }
        return anyMatch;
    }

    private void requestRemoteFileList() {
        remoteRequested = true;
        remoteLoading = true;
        ClientPacketDistributor.sendToServer(new ServerBoundRequestFileListPacket(""));
    }

    private void openRemoteFile(String relativePath) {
        Identifier editorId = remoteFileEditorId(relativePath);
        if (getExistingEditor(editorId) != null) return;
        ClientPacketDistributor.sendToServer(new ServerBoundRequestFileContentPacket(relativePath));
    }

    private void openFileInEditor(File file) {
        String fileName = file.getName().toLowerCase();

        if (fileName.endsWith(".png") || fileName.endsWith(".jpg")) {
            Identifier viewerId = Common.id("tex_viewer_" + file.getAbsolutePath().hashCode());
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

            CodeEditor existing = getExistingEditor(editorId);
            if (existing != null) return;

            CodeEditor newEditor = buildCodeEditor(file, editorId, content);
            Client.getEditorManager().register(newEditor);
            newEditor.open();

        } catch (IOException e) {
            setError("Could not open \"" + file.getName() + "\": " + e.getLocalizedMessage());
        }
    }

    private CodeEditor buildCodeEditor(File file, Identifier editorId, String content) {
        CodeEditor editor = new CodeEditor(editorId, "[CLIENT] " + file.getName(), content);

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

    private void renderToolbar() {
        ImGui.textDisabled(rootDir.getName() + "/");
        ImGui.sameLine();

        float rightEdge = ImGui.getContentRegionAvailX();
        float buttonWidth = ImGui.getFrameHeight();
        float spacing = ImGui.getStyle().getItemSpacingX();

        float searchWidth = rightEdge - (buttonWidth + spacing) * 2 - spacing;
        ImGui.setNextItemWidth(Math.max(searchWidth, 60.0f));
        ImGui.inputTextWithHint("##search", ImIcons.FA.FA_SEARCH + " Filter…", searchFilter);

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
            // Reset remote state so tree is re-fetched
            remoteRootNode = null;
            remoteRequested = false;
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Refresh  (Ctrl+R)");
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

    private void createNewFileIn(File dir) {
        File newFile = uniqueFile(dir, "new_file", "txt");
        try {
            if (newFile.createNewFile()) {
                refresh();
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
            refresh();
        }
    }

    private void revealInExplorer(File dir) {
        try {
            Desktop.getDesktop().open(dir);
        } catch (Exception ignored) {
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