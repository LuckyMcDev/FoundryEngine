package de.luckymcdev.foundryengine.client.editor.panel.explorer;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.files.CodeEditor;
import de.luckymcdev.foundryengine.client.editor.panel.files.TextureViewerPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.ImGuiShortcut;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import de.luckymcdev.foundryengine.common.exceptions.EngineException;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ClientBoundFileListPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ServerBoundRequestFileContentPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ServerBoundRequestFileListPacket;
import de.luckymcdev.foundryengine.common.network.packets.explorer.ServerBoundSaveFilePacket;
import de.luckymcdev.foundryengine.common.util.FileEndings;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.awt.Desktop;
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
    private ExplorerNode.FileExplorerNode rootNode;
    private ExplorerNode.@Nullable RemoteExplorerNode remoteRootNode = null;
    private @Nullable String lastError = null;
    private boolean rootReadable = true;
    private boolean remoteRequested = false;
    private boolean remoteLoading = false;

    public FileExplorerPanel(File rootDir) {
        super(new Builder(Common.id("file_explorer"))
                .icon(ImIcons.FILES_O)
                .shortcut(ImGuiShortcut.empty())
                .category(PanelCategory.EDITOR_EXPLORER));
        this.rootDir = rootDir;
    }

    private static boolean isMultiplayer() {
        return Client.getMc().getCurrentServer() != null;
    }

    private static Identifier fileToEditorId(File file) {
        String sanitised = file.getAbsolutePath().toLowerCase().replaceAll("[^a-z0-9]", "_");
        return Common.id("editor_" + sanitised);
    }

    private static Identifier remoteFileEditorId(String relativePath) {
        String sanitised = ("remote_" + relativePath).toLowerCase().replaceAll("[^a-z0-9]", "_");
        return Common.id("editor_" + sanitised);
    }

    /**
     * Returns a non-conflicting file path by appending {@code _1}, {@code _2}, etc.
     */
    private static File uniqueFile(File dir, String baseName, @Nullable String extension) {
        String suffix = extension != null ? "." + extension : "";
        File candidate = new File(dir, baseName + suffix);
        int n = 1;
        while (candidate.exists()) {
            candidate = new File(dir, baseName + "_" + n++ + suffix);
        }
        return candidate;
    }

    private static String fileSizeString(File file) {
        try {
            long bytes = Files.readAttributes(file.toPath(), BasicFileAttributes.class).size();
            if (bytes < 1_024) return bytes + " B";
            if (bytes < 1_048_576) return String.format("%.1f KB", bytes / 1_024.0);
            return String.format("%.1f MB", bytes / 1_048_576.0);
        } catch (IOException e) {
            throw new EngineException(e);
        }
    }

    private static String fileNameFrom(String path) {
        return path.contains("/") ? path.substring(path.lastIndexOf('/') + 1) : path;
    }

    /**
     * Called when the server sends its file listing.
     * Rebuilds {@link #remoteRootNode} from the flat entry list.
     */
    public void receiveRemoteFileList(List<ClientBoundFileListPacket.RemoteEntry> entries) {
        ExplorerNode.RemoteExplorerNode root =
                new ExplorerNode.RemoteExplorerNode("Server (" + Common.MODNAME + ")", "", true);

        for (ClientBoundFileListPacket.RemoteEntry entry : entries) {
            String[] parts = entry.relativePath().split("/");
            ExplorerNode.RemoteExplorerNode current = root;

            for (int i = 0; i < parts.length; i++) {
                String part = parts[i];
                boolean isLastPart = (i == parts.length - 1);
                String partialPath = String.join("/", Arrays.copyOfRange(parts, 0, i + 1));

                if (isLastPart) {
                    ExplorerNode.RemoteExplorerNode node =
                            new ExplorerNode.RemoteExplorerNode(part, entry.relativePath(), entry.isDirectory());
                    if (entry.isDirectory()) {
                        current.children.putIfAbsent(part, node);
                    } else {
                        boolean alreadyAdded = current.files.stream()
                                .anyMatch(f -> f.relativePath.equals(entry.relativePath()));
                        if (!alreadyAdded) {
                            current.files.add(node);
                        }
                    }
                } else {
                    current = (ExplorerNode.RemoteExplorerNode) current.children.computeIfAbsent(
                            part, k -> new ExplorerNode.RemoteExplorerNode(k, partialPath, true));
                }
            }
        }

        remoteRootNode = root;
        remoteLoading = false;
    }

    /**
     * Called when the server sends back the contents of a requested file.
     * Opens the content in a {@link CodeEditor} whose save callback pushes changes back.
     */
    public void receiveRemoteFileContent(String relativePath, String content) {
        Identifier editorId = remoteFileEditorId(relativePath);
        if (getExistingEditor(editorId) != null) return;

        String fileName = fileNameFrom(relativePath);
        CodeEditor editor = new CodeEditor(editorId, Component.literal(" Editor: [SERVER] " + fileName), content);

		editor.applyLanguage(fileName);

        editor.setSaveCallback((source, errors) ->
                ClientPacketDistributor.sendToServer(new ServerBoundSaveFilePacket(relativePath, source)));

        Client.getEditorManager().register(editor);
        editor.open();
    }

    @Override
    protected void refresh() {
        rootNode = new ExplorerNode.FileExplorerNode("root", rootDir);
        rootReadable = buildFileTree(rootDir, rootNode);
        initialized = true;

        if (isMultiplayer()) {
            requestRemoteFileList();
        }
    }

    @Override
    protected void renderBrowser() {
        if (!requireLevelOnServer(PermissionLevel.OWNERS)) return;

        renderToolbar();
        renderErrorBanner();

        if (!rootReadable) {
            ImGui.textDisabled("Root directory is not accessible.");
            return;
        }

        ImGui.separator();

        if (ImGui.isWindowFocused() && ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(ImGuiKey.R)) {
            clearError();
        }

        if (ImGui.beginChild("##explorerTree", 0, 0, false)) {
            boolean filtering = !searchFilter.get().isBlank();
            String query = searchFilter.get().trim().toLowerCase();

            // Client section
            renderSection(ImGraphicsExtractor.icon(ImIcons.DESKTOP) + " Client", "##section_client", () -> {
                if (filtering) {
                    renderFilteredLocalFiles(rootDir, query);
                } else if (rootNode != null) {
                    renderFileNode(rootNode);
                }
            });

            // Server section
            if (isMultiplayer()) {
                ImGui.spacing();
                renderSection(ImGraphicsExtractor.icon(ImIcons.SERVER) + " Server", "##section_server", () -> {
                    if (remoteLoading) {
                        ImGui.textDisabled("Loading…");
                    } else if (remoteRootNode == null) {
                        if (!remoteRequested) requestRemoteFileList();
                        ImGui.textDisabled("Fetching file list…");
                    } else if (filtering) {
                        renderFilteredRemoteNodes(remoteRootNode, query);
                    } else {
                        renderRemoteNodeChildren(remoteRootNode);
                    }
                });
            }
        }
        ImGui.endChild();
    }

    /**
     * Not used by the file explorer – files are opened via {@link #openFileInEditor}.
     */
    @Override
    protected void openResource(Identifier id) { /* no-op */ }

    /**
     * Recursively scans {@code dir} and populates {@code parentNode}.
     * Directories are expanded; files go into {@link ExplorerNode.FileExplorerNode#files}.
     *
     * @return {@code true} if the directory was readable
     */
    private boolean buildFileTree(File dir, ExplorerNode.FileExplorerNode parentNode) {
        File[] contents = dir.listFiles();
        if (contents == null) return false;

        Arrays.sort(contents, Comparator
                .comparing(File::isFile)                          // directories first
                .thenComparing(f -> f.getName().toLowerCase()));

        for (File entry : contents) {
            if (entry.isDirectory()) {
                ExplorerNode.FileExplorerNode dirNode = new ExplorerNode.FileExplorerNode(entry.getName(), entry);
                parentNode.addChild(entry.getName(), dirNode);
                buildFileTree(entry, dirNode);
            } else {
                parentNode.files.add(entry);
            }
        }
        return true;
    }

    private void renderFileNode(ExplorerNode.FileExplorerNode node) {
        // Root node is invisible – just render its children directly.
        if ("root".equals(node.name)) {
            renderFileNodeChildren(node);
            return;
        }

        String id = "##dir_" + node.file.getPath();
        boolean isOpen = ImGui.treeNodeEx(id, ImGuiTreeNodeFlags.SpanAvailWidth, "");

        ImGui.sameLine();
        String folderIcon = isOpen
                ? ImGraphicsExtractor.icon(ImIcons.FOLDER_OPEN)
                : ImGraphicsExtractor.icon(ImIcons.FOLDER);
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
            renderLocalFileItem(file);
        }
    }

    private void renderLocalFileItem(File file) {
        String id = "##file_" + file.getPath();
        String fileName = file.getName();
        String icon = FileEndings.getFileIcon(fileName);
        boolean isOpen = isFileOpen(file);

        if (isOpen) ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getStyle().getColor(ImGuiCol.CheckMark));
        ImGui.treeNodeEx(id,
                ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth,
                icon + " " + fileName);
        if (isOpen) ImGui.popStyleColor();

        if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
            openFileInEditor(file);
        }

        renderFileContextMenu(file, id + "_ctx");
        renderFileTooltip(file, isOpen);
    }

    /**
     * Recursively finds and renders every local file whose name contains {@code query}.
     * No tree structure is shown – just a flat filtered list (matches Veil's search UX).
     */
    private boolean renderFilteredLocalFiles(File dir, String query) {
        File[] contents = dir.listFiles();
        if (contents == null) return false;

        Arrays.sort(contents, Comparator
                .comparing(File::isFile)
                .thenComparing(f -> f.getName().toLowerCase()));

        boolean anyMatch = false;
        for (File entry : contents) {
            if (entry.isDirectory()) {
                anyMatch |= renderFilteredLocalFiles(entry, query);
            } else if (entry.getName().toLowerCase().contains(query)) {
                renderLocalFileItem(entry);
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

    private void renderRemoteNode(ExplorerNode.RemoteExplorerNode node) {
        if (node.isDirectory) {
            String id = "##rdir_" + node.relativePath;
            boolean isOpen = ImGui.treeNodeEx(id, ImGuiTreeNodeFlags.SpanAvailWidth, "");

            ImGui.sameLine();
            String folderIcon = isOpen
                    ? ImGraphicsExtractor.icon(ImIcons.FOLDER_OPEN)
                    : ImGraphicsExtractor.icon(ImIcons.FOLDER);
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
            renderRemoteNode((ExplorerNode.RemoteExplorerNode) child);
        }
        for (ExplorerNode.RemoteExplorerNode file : node.files) {
            renderRemoteFileItem(file);
        }
    }

    private void renderRemoteFileItem(ExplorerNode.RemoteExplorerNode node) {
        String id = "##rfile_" + node.relativePath;
        String icon = FileEndings.getFileIcon(node.name);
        boolean isOpen = isResourceOpen(remoteFileEditorId(node.relativePath));

        if (isOpen) ImGui.pushStyleColor(ImGuiCol.Text, ImGui.getStyle().getColor(ImGuiCol.CheckMark));
        ImGui.treeNodeEx(id,
                ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth,
                icon + " " + node.name);
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
            anyMatch |= renderFilteredRemoteNodes((ExplorerNode.RemoteExplorerNode) child, query);
        }
        for (ExplorerNode.RemoteExplorerNode file : node.files) {
            if (file.name.toLowerCase().contains(query)) {
                renderRemoteFileItem(file);
                anyMatch = true;
            }
        }
        return anyMatch;
    }

    private void openRemoteFile(String relativePath) {
        Identifier editorId = remoteFileEditorId(relativePath);
        if (getExistingEditor(editorId) != null) return;
        ClientPacketDistributor.sendToServer(new ServerBoundRequestFileContentPacket(relativePath));
    }

    private void openFileInEditor(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".png") || name.endsWith(".jpg")) {
            openTextureViewer(file);
            return;
        }

        try {
            String content = Files.readString(file.toPath());
            Identifier editorId = fileToEditorId(file);

            if (getExistingEditor(editorId) != null) return;

            CodeEditor editor = buildLocalCodeEditor(file, editorId, content);
            Client.getEditorManager().register(editor);
            editor.open();
        } catch (IOException e) {
            setError("Could not open \"" + file.getName() + "\": " + e.getLocalizedMessage());
        }
    }

    private void openTextureViewer(File file) {
        Identifier viewerId = Common.id("tex_viewer_" + file.getAbsolutePath().hashCode());
        if (Client.getEditorManager().getPanel(viewerId) instanceof TextureViewerPanel viewer) {
            viewer.open();
            return;
        }
        TextureViewerPanel viewer = new TextureViewerPanel(viewerId, Component.literal("Texture: " + file.getName()), file);
        Client.getEditorManager().register(viewer);
        viewer.open();
    }

    private CodeEditor buildLocalCodeEditor(File file, Identifier editorId, String content) {
        CodeEditor editor = new CodeEditor(editorId, Component.literal(" Editor: [CLIENT] " + file.getName()), content);
		editor.applyLanguage(file.getName());
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

        float buttonWidth = ImGui.getFrameHeight();
        float spacing = ImGui.getStyle().getItemSpacingX();
        float searchWidth = ImGui.getContentRegionAvailX() - (buttonWidth + spacing) * 2 - spacing;
        ImGui.setNextItemWidth(Math.max(searchWidth, 60.0f));
        ImGui.inputTextWithHint("##search", ImIcons.SEARCH + " Filter…", searchFilter);

        // Escape clears the search field
        if (ImGui.isItemFocused() && ImGui.isKeyPressed(ImGuiKey.Escape)) {
            searchFilter.set("");
        }

        if (!searchFilter.get().isEmpty()) {
            ImGui.sameLine();
            if (ImGui.smallButton("×##clearSearch")) searchFilter.set("");
        }

        ImGui.sameLine();
        if (ImGui.button(ImIcons.ROTATE_RIGHT + "##refresh", buttonWidth, 0)) {
            clearError();
            remoteRootNode = null;
            remoteRequested = false;
            refresh();
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Refresh  (Ctrl+R)");
    }

    /**
     * Shows a dismissible error banner at the top of the panel when an error is set.
     */
    private void renderErrorBanner() {
        if (lastError == null) return;
        ImGui.pushStyleColor(ImGuiCol.Text, 1.0f, 0.35f, 0.35f, 1.0f);
        ImGui.textWrapped(ImIcons.EXCLAMATION_TRIANGLE + " " + lastError);
        ImGui.popStyleColor();
        ImGui.sameLine();
        if (ImGui.smallButton("×")) lastError = null;
    }

    private void renderFileContextMenu(File file, String popupId) {
        if (ImGui.beginPopupContextItem(popupId)) {
            if (ImGui.menuItem(ImIcons.EDIT + "  Open")) openFileInEditor(file);
            ImGui.separator();
            if (ImGui.menuItem(ImIcons.COPYRIGHT + "  Copy Path")) ImGui.setClipboardText(file.getAbsolutePath());
            if (ImGui.menuItem(ImIcons.FOLDER_OPEN + "  Reveal in Explorer"))
                revealInExplorer(file.getParentFile());
            ImGui.endPopup();
        }
    }

    private void renderDirectoryContextMenu(File dir, String popupId) {
        if (ImGui.beginPopupContextItem(popupId)) {
            if (ImGui.menuItem(ImIcons.COPY + "  Copy Path")) ImGui.setClipboardText(dir.getAbsolutePath());
            if (ImGui.menuItem(ImIcons.FOLDER_OPEN + "  Reveal in Explorer")) revealInExplorer(dir);
            ImGui.separator();
            if (ImGui.menuItem(ImIcons.FILE + "  New File…")) createNewFileIn(dir);
            if (ImGui.menuItem(ImIcons.FOLDER + "  New Folder…")) createNewFolderIn(dir);
            ImGui.endPopup();
        }
    }

    private void renderFileTooltip(File file, boolean isOpen) {
        if (!ImGui.isItemHovered()) return;
        ImGui.beginTooltip();
        ImGui.text(file.getName());
        ImGui.separator();
        ImGui.textDisabled("Size:     " + fileSizeString(file));
        ImGui.textDisabled("Modified: " + DATE_FMT.format(Instant.ofEpochMilli(file.lastModified())));
        if (isOpen) {
            ImGui.separator();
            ImGui.textDisabled("Currently open in editor");
        }
        ImGui.endTooltip();
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

    private boolean isFileOpen(File file) {
        return isResourceOpen(fileToEditorId(file));
    }

    private void setError(String message) {
        lastError = message;
        LOGGER.error(message);
    }

    private void clearError() {
        lastError = null;
        rootReadable = true;
    }
}