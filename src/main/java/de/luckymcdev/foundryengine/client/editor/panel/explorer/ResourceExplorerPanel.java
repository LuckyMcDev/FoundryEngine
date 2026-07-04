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
import de.luckymcdev.foundryengine.server.Server;
import imgui.ImGui;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;
import imgui.type.ImString;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Explorer panel that browses all Minecraft resources via the {@link ResourceManager}.
 * <p>
 * Supports every standard resource folder (textures, models, shaders, …) plus any
 * custom mod resources.  Files always open in <em>read-only</em> mode; images are
 * shown in the {@link TextureViewerPanel}, everything else in a {@link CodeEditor}.
 */
public class ResourceExplorerPanel extends AbstractExplorerPanel {
    public static final ResourceExplorerPanel INSTANCE = new ResourceExplorerPanel();
    private static final Logger LOGGER = LogUtils.getLogger();
    private final Map<String, ExplorerNode.ResourceExplorerNode> namespaces = new TreeMap<>();
    private final ImString searchFilter = new ImString(256);
    private volatile boolean reloadInProgress = false;

    public ResourceExplorerPanel() {
        super(new Builder(Common.id("resource_browser"))
                .icon(ImIcons.FA.FA_IMAGE)
                .shortcut(ImGuiShortcut.empty())
                .category(PanelCategory.EDITOR_EXPLORER));
    }

    @Override
    protected void refresh() {
        namespaces.clear();

        scanPacks(Client.getResourceManager(), PackType.CLIENT_RESOURCES);

        MinecraftServer server = Client.getMc().getSingleplayerServer();
        if (server != null) {
            scanPacks(server.getResourceManager(), PackType.SERVER_DATA);
        }

        initialized = true;
    }

    private void scanPacks(ResourceManager rm, PackType type) {
        rm.listPacks().forEach(pack -> {
            for (String namespace : pack.getNamespaces(type)) {
                if (namespace.isEmpty()) continue;
                try {
                    pack.listResources(type, namespace, "", (id, supplier) -> {
                        ExplorerNode.ResourceExplorerNode nsNode = namespaces.computeIfAbsent(
                                id.getNamespace(),
                                ExplorerNode.ResourceExplorerNode::new
                        );
                        addResourceToTree(nsNode, id);
                    });
                } catch (IllegalArgumentException ignored) {
                }
            }
        });
    }

    private void addResourceToTree(ExplorerNode.ResourceExplorerNode root, Identifier id) {
        String[] segments = id.getPath().split("/");
        ExplorerNode current = root;

        for (int i = 0; i < segments.length - 1; i++) {
            String segment = segments[i];
            ExplorerNode child = current.getChild(segment);
            if (child == null) {
                child = new ExplorerNode.ResourceExplorerNode(segment);
                current.addChild(segment, child);
            }
            current = child;
        }

        if (current instanceof ExplorerNode.ResourceExplorerNode folder) {
            folder.resources.add(id);
        }
    }

    @Override
    protected void renderBrowser() {
        renderToolbar();
        ImGui.separator();

        if (ImGui.beginChild("##resTree")) {
            String query = searchFilter.get().trim().toLowerCase();

            if (query.isBlank()) {
                renderTree();
            } else {
                renderSearchResults(query);
            }
        }
        ImGui.endChild();
    }

    private void renderToolbar() {
        float buttonWidth = ImGui.getFrameHeight();
        float spacing = ImGui.getStyle().getItemSpacingX();
        float searchWidth = ImGui.getContentRegionAvailX() - (buttonWidth + spacing) * 3 - spacing;
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
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Refresh resource tree");

        ImGui.sameLine();
        if (ImGui.button(ImIcons.FA.FA_ROTATE + "##reload", buttonWidth, 0)) {
            reloadResources();
        }
        if (ImGui.isItemHovered()) ImGui.setTooltip("Reload all resources");
    }

    private void reloadResources() {
        if (reloadInProgress) return;
        reloadInProgress = true;

        Server.reloadResources()
                .thenRun(() -> {
                    reloadInProgress = false;
                    refresh();
                })
                .exceptionally(e -> {
                    reloadInProgress = false;
                    LOGGER.error("Resource reload failed", e);
                    return null;
                });
    }

    /**
     * Render the full namespace hierarchy.
     */
    private void renderTree() {
        for (ExplorerNode.ResourceExplorerNode ns : namespaces.values()) {
            renderNamespaceNode(ns);
        }
    }

    /**
     * Render a flat, filtered list of all resources whose path contains {@code query}.
     * Mirrors Veil's search behaviour: no tree structure, just matching file rows.
     */
    private void renderSearchResults(String query) {
        for (ExplorerNode.ResourceExplorerNode ns : namespaces.values()) {
            collectAndRenderMatches(ns, query);
        }
    }

    private void collectAndRenderMatches(ExplorerNode.ResourceExplorerNode folder, String query) {
        for (Identifier id : folder.resources) {
            if (id.toString().toLowerCase().contains(query)) {
                renderResourceFile(id);
            }
        }
        for (ExplorerNode child : folder.getChildren()) {
            if (child instanceof ExplorerNode.ResourceExplorerNode sub) {
                collectAndRenderMatches(sub, query);
            }
        }
    }

    @Override
    protected void renderResourceFile(Identifier id) {
        String fileName = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
        String label = ImGraphicsExtractor.icon(ImIcons.FA.FA_FILE_CODE) + " " + fileName;
        String nodeId = "##file_" + id;

        ImGui.treeNodeEx(
                nodeId,
                ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth,
                label
        );

        if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
            openResource(id);
        }

        renderFileContextMenu(id, nodeId + "_ctx");
    }

    private void renderFileContextMenu(Identifier id, String popupId) {
        if (ImGui.beginPopupContextItem(popupId)) {
            if (ImGui.menuItem(ImIcons.FA.FA_EDIT + "  Open")) {
                openResource(id);
            }
            ImGui.separator();
            if (ImGui.menuItem(ImIcons.FA.FA_COPY + "  Copy Identifier")) {
                ImGui.setClipboardText(id.toString());
            }
            ImGui.endPopup();
        }
    }

    @Override
    protected void openResource(Identifier id) {
        String path = id.getPath().toLowerCase();

        if (path.endsWith(".png") || path.endsWith(".jpg")) {
            openTextureViewer(id);
        } else {
            openInCodeEditor(id);
        }
    }

    private void openTextureViewer(Identifier id) {
        Identifier viewerId = generateEditorId("res_tex", id.toString());

        // Re-focus if already open.
        if (Client.getEditorManager().getPanel(viewerId) instanceof TextureViewerPanel viewer) {
            viewer.open();
            return;
        }

        TextureViewerPanel viewer = new TextureViewerPanel(viewerId, Component.literal("Texture: " + id.getPath()), id);
        Client.getEditorManager().register(viewer);
        viewer.open();
    }

    private void openInCodeEditor(Identifier id) {
        Identifier editorId = generateEditorId("res", id.toString().replace(":", "_").replace("/", "_"));

        if (getExistingEditor(editorId) != null) return;

        Optional<Resource> resource = Client.getResourceManager().getResource(id);

        if (resource.isEmpty()) {
            MinecraftServer server = Client.getMc().getSingleplayerServer();
            if (server != null) {
                resource = server.getResourceManager().getResource(id);
            }
        }

        resource.ifPresent(res -> {
            try (InputStream in = res.open()) {
                String content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                CodeEditor editor = new CodeEditor(editorId, Component.literal(" Editor: " + id.getPath()), content);
                editor.getTextEditor().setReadOnly(true);
                editor.forceReadOnly = true;
                editor.applyLanguage(id.getPath());

                Client.getEditorManager().register(editor);
                editor.open();
            } catch (IOException e) {
                LOGGER.error("Failed to open resource '{}': {}", id, e);
            }
        });
    }
}