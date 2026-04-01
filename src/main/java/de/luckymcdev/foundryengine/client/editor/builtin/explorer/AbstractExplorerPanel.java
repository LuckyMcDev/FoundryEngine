package de.luckymcdev.foundryengine.client.editor.builtin.explorer;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.Panel;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.builtin.files.CodeEditor;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.EngineImGuiUtils;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcon;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.util.key.Shortcut;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiMouseButton;
import imgui.flag.ImGuiTreeNodeFlags;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Abstract base for browser panels (ResourceBrowserPanel, FileExplorerPanel).
 * Provides common rendering logic for tree-based resource browsing.
 */
public abstract class AbstractExplorerPanel extends EditorPanel {
    protected static final Logger LOGGER = LogUtils.getLogger();
    protected boolean initialized = false;

    protected AbstractExplorerPanel(Identifier id, String displayName, ImIcon icon, Shortcut shortcut) {
        super(id, displayName, icon, shortcut);
        this.category = PanelCategory.EDITOR_EXPLORER;
    }

    /**
     * Refresh the browser tree structure. Implement to populate nodes.
     */
    protected abstract void refresh();

    /**
     * Render the main browsing tree. Override for custom layout.
     */
    protected abstract void renderBrowser();

    /**
     * Handle opening/editing a resource identified by an Identifier.
     */
    protected abstract void openResource(Identifier id);

    @Override
    public final void content() {
        if (!initialized) {
            refresh();
        }
        renderBrowser();
    }

    /**
     * Render a resource folder (for Minecraft resources).
     */
    protected void renderFolder(ExplorerNode.ResourceExplorerNode folder) {
        // Render subfolders
        for (ExplorerNode child : folder.getChildren()) {
            if (child instanceof ExplorerNode.ResourceExplorerNode subFolder) {
                boolean open = ImGui.treeNodeEx(
                        "##f_" + subFolder.name,
                        ImGuiTreeNodeFlags.SpanAvailWidth,
                        ""
                );
                ImGui.sameLine();
                ImGui.textUnformatted(
                        EngineImGuiUtils.icon(open ? ImIcons.FA.FA_FOLDER_OPEN : ImIcons.FA.FA_FOLDER)
                                + " " + subFolder.name
                );
                if (open) {
                    renderFolder(subFolder);
                    ImGui.treePop();
                }
            }
        }

        // Render resource files
        for (Identifier id : folder.resources) {
            renderResourceFile(id);
        }
    }

    /**
     * Render a single resource file entry.
     */
    protected void renderResourceFile(Identifier id) {
        String fileName = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
        ImGui.treeNodeEx(
                "##file_" + id,
                ImGuiTreeNodeFlags.Leaf
                        | ImGuiTreeNodeFlags.NoTreePushOnOpen
                        | ImGuiTreeNodeFlags.SpanAvailWidth,
                ""
        );
        ImGui.sameLine();
        ImGui.textUnformatted(EngineImGuiUtils.icon(ImIcons.FA.FA_FILE_CODE) + " " + fileName);

        if (ImGui.isItemClicked(ImGuiMouseButton.Left) && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
            openResource(id);
        }
    }

    /**
     * Render a namespace/root node with children.
     */
    protected void renderNamespaceNode(ExplorerNode.ResourceExplorerNode ns) {
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth;
        if (ImGui.treeNodeEx("##ns_" + ns.name, flags, "")) {
            ImGui.sameLine();
            ImGui.textUnformatted(EngineImGuiUtils.icon(ImIcons.FA.FA_CUBE) + " " + ns.name);
            renderFolder(ns);
            ImGui.treePop();
        } else {
            ImGui.sameLine();
            ImGui.textUnformatted(EngineImGuiUtils.icon(ImIcons.FA.FA_CUBE) + " " + ns.name);
        }
    }

    /**
     * Generate a unique editor ID for a resource.
     */
    protected Identifier generateEditorId(String prefix, String resourcePath) {
        String sanitized = resourcePath.toLowerCase().replaceAll("[^a-z0-9]", "_");
        return Common.id(prefix + "_" + sanitized);
    }

    /**
     * Check if a file/resource is already open in the editor.
     */
    protected boolean isResourceOpen(Identifier editorId) {
        return Client.getEditorManager().getPanels().get(editorId) instanceof CodeEditor;
    }

    /**
     * Get or reuse an existing CodeEditor.
     */
    protected @Nullable CodeEditor getExistingEditor(Identifier editorId) {
        Panel panel = Client.getEditorManager().getPanels().get(editorId);
        if (panel instanceof CodeEditor editor) {
            editor.open();
            return editor;
        }
        return null;
    }
}