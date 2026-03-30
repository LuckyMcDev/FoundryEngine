package de.luckymcdev.foundryengine.client.editor.builtin.explorer;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.files.CodeEditor;
import de.luckymcdev.foundryengine.client.editor.builtin.files.TextureViewerPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Browse all Minecraft resources from the resource manager.
 * Supports all standard Minecraft resource types and custom mod resources.
 * Resources open in read-only mode.
 */
public class ResourceExplorerPanel extends AbstractExplorerPanel {
    public static final ResourceExplorerPanel INSTANCE = new ResourceExplorerPanel();
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String[] SCAN_FOLDERS = {
            "textures", "models", "blockstates", "lang", "font", "particles",
            "shaders", "texts", "sounds", "atlases", "recipes", "loot_tables",
            "advancements", "tags", "worldgen", "functions", "structures", "dimension",
            "biomes", "noise_settings", "dimension_type", "carver", "configured_feature",
            "placed_feature", "damage_type", "painting", "enchantment", "painting",
            "item_modifiers", "predicate", "schema", "javamodels", "multimodels"
    };
    private final Map<String, ExplorerNode.ResourceExplorerNode> namespaces = new TreeMap<>();

    public ResourceExplorerPanel() {
        super(Common.id("resource_browser"), "Resource Browser");
        this.category = PanelCategory.EDITOR_EXPLORER;
    }

    @Override
    protected void refresh() {
        namespaces.clear();
        ResourceManager rm = Client.getResourceManager();

        for (String namespace : rm.getNamespaces()) {
            ExplorerNode.ResourceExplorerNode nsNode = new ExplorerNode.ResourceExplorerNode(namespace);

            for (String folder : SCAN_FOLDERS) {
                try {
                    rm.listResources(folder, (id) -> id.getNamespace().equals(namespace))
                            .forEach((id, res) -> {
                                addResourceToNode(nsNode, id);
                            });
                } catch (Exception e) {
                    LOGGER.debug("Could not scan folder '{}' in namespace '{}': {}", folder, namespace, e.getMessage());
                }
            }

            if (!nsNode.isEmpty()) {
                namespaces.put(namespace, nsNode);
            }
        }
        initialized = true;
    }

    private void addResourceToNode(ExplorerNode.ResourceExplorerNode root, Identifier id) {
        String[] parts = id.getPath().split("/");
        ExplorerNode current = root;

        for (int i = 0; i < parts.length - 1; i++) {
            String folderName = parts[i];
            ExplorerNode child = current.getChild(folderName);

            if (child == null) {
                child = new ExplorerNode.ResourceExplorerNode(folderName);
                current.addChild(folderName, child);
            }
            current = child;
        }

        if (current instanceof ExplorerNode.ResourceExplorerNode folder) {
            folder.resources.add(id);
        }
    }

    @Override
    protected void renderBrowser() {
        if (ImGui.button(EngineImGuiUtils.icon(ImIcons.FA.FA_ARROW_ROTATE_RIGHT) + " Refresh")) {
            refresh();
        }

        ImGui.separator();

        if (ImGui.beginChild("##resTree")) {
            for (ExplorerNode.ResourceExplorerNode ns : namespaces.values()) {
                renderNamespaceNode(ns);
            }
        }
        ImGui.endChild();
    }

    @Override
    protected void renderResourceFile(Identifier id) {
        String fileName = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
        String label = EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_CODE) + " " + fileName;
        String fileId = "##file_" + id;

        // Render the node with the label included so it is clickable
        ImGui.treeNodeEx(
                fileId,
                ImGuiTreeNodeFlags.Leaf
                        | ImGuiTreeNodeFlags.NoTreePushOnOpen
                        | ImGuiTreeNodeFlags.SpanAvailWidth,
                label
        );

        // Handle Left Click
        if (ImGui.isItemClicked(ImGuiMouseButton.Left)) {
            openResource(id);
        }

        // Handle Right Click Context Menu
        renderResourceContextMenu(id, fileId + "_ctx");
    }

    private void renderResourceContextMenu(Identifier id, String popupId) {
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

        // Images
        if (path.endsWith(".png") || path.endsWith(".jpg")) {
            Identifier viewerId = generateEditorId("res_tex", id.toString());

            if (Client.getEditorManager().getPanels().get(viewerId) instanceof TextureViewerPanel viewer) {
                viewer.open();
                return;
            }

            TextureViewerPanel viewer = new TextureViewerPanel(viewerId, "Texture: " + id.getPath(), id);
            Client.getEditorManager().register(viewer);
            viewer.open();
            return;
        }

        // Everything else :D
        Identifier editorId = generateEditorId("res", id.toString().replace(":", "_").replace("/", "_"));

        if (getExistingEditor(editorId) != null) {
            return;
        }

        Client.getResourceManager().getResource(id).ifPresent(res -> {
            try (InputStream in = res.open()) {
                String content = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))
                        .lines()
                        .collect(Collectors.joining("\n"));

                CodeEditor editor = new CodeEditor(editorId, id.getPath(), content);

                editor.getTextEditor().setReadOnly(true);
                editor.forceReadOnly = true;

                Client.getEditorManager().register(editor);
                editor.open();
            } catch (Exception e) {
                LOGGER.error("Failed to open resource: {}", id, e);
            }
        });
    }
}