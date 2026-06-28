package de.luckymcdev.foundryengine.client.editor.panel.explorer;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.panel.files.CodeEditor;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
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

    protected AbstractExplorerPanel(Builder builder) {
        super(builder);
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
    public final void content(ImGraphicsExtractor g) {
        if (!initialized) {
            refresh();
        }
        renderBrowser();
    }

    /**
     * Renders a collapsible section with a bold framed header.
     * The {@code body} runnable is called only when the section is open.
     *
     * @param label display label (may include an icon prefix)
     * @param id    unique ImGui ID string (prefix with {@code ##})
     * @param body  content to render inside the section
     */
    protected void renderSection(String label, String id, Runnable body) {
        int flags = ImGuiTreeNodeFlags.SpanAvailWidth
                | ImGuiTreeNodeFlags.DefaultOpen
                | ImGuiTreeNodeFlags.Framed;
        if (ImGui.treeNodeEx(id, flags, label)) {
            body.run();
            ImGui.treePop();
        }
    }

    /**
     * Renders a namespace/root resource node and recurses into its children.
     * The node is shown with a cube icon and the namespace name.
     */
    protected void renderNamespaceNode(ExplorerNode.ResourceExplorerNode ns) {
        String label = ImGraphicsExtractor.icon(ImIcons.FA.FA_CUBE) + " " + ns.name;
        boolean open = ImGui.treeNodeEx("##ns_" + ns.name, ImGuiTreeNodeFlags.SpanAvailWidth, label);
        if (open) {
            renderResourceFolder(ns);
            ImGui.treePop();
        }
    }

    /**
     * Renders the contents of a resource folder: sub-folders first, then leaf files.
     */
    protected void renderResourceFolder(ExplorerNode.ResourceExplorerNode folder) {
        for (ExplorerNode child : folder.getChildren()) {
            if (child instanceof ExplorerNode.ResourceExplorerNode sub) {
                renderResourceSubFolder(sub);
            }
        }
        for (Identifier id : folder.resources) {
            renderResourceFile(id);
        }
    }

    /**
     * Renders a single collapsible sub-folder within a resource tree.
     */
    private void renderResourceSubFolder(ExplorerNode.ResourceExplorerNode folder) {
        String openIcon = ImGraphicsExtractor.icon(ImIcons.FA.FA_FOLDER_OPEN);
        String closedIcon = ImGraphicsExtractor.icon(ImIcons.FA.FA_FOLDER);

        boolean open = ImGui.treeNodeEx("##f_" + folder.name, ImGuiTreeNodeFlags.SpanAvailWidth, "");
        ImGui.sameLine();
        ImGui.textUnformatted((open ? openIcon : closedIcon) + " " + folder.name);

        if (open) {
            renderResourceFolder(folder);
            ImGui.treePop();
        }
    }

    /**
     * Renders a single resource file leaf node.
     * Double-clicking opens the resource; subclasses may override for custom behaviour.
     */
    protected void renderResourceFile(Identifier id) {
        String fileName = id.getPath().substring(id.getPath().lastIndexOf('/') + 1);
        String label = ImGraphicsExtractor.icon(ImIcons.FA.FA_FILE_CODE) + " " + fileName;

        ImGui.treeNodeEx(
                "##file_" + id,
                ImGuiTreeNodeFlags.Leaf | ImGuiTreeNodeFlags.NoTreePushOnOpen | ImGuiTreeNodeFlags.SpanAvailWidth,
                label
        );

        if (ImGui.isItemClicked(ImGuiMouseButton.Left) && ImGui.isMouseDoubleClicked(ImGuiMouseButton.Left)) {
            openResource(id);
        }
    }

    /**
     * Builds a stable, filesystem-safe {@link Identifier} for an editor tab
     * from an arbitrary resource path string.
     *
     * @param prefix       short type hint, e.g. {@code "res"} or {@code "res_tex"}
     * @param resourcePath raw resource path (colons, slashes, etc. are sanitised)
     */
    protected Identifier generateEditorId(String prefix, String resourcePath) {
        String sanitized = resourcePath.toLowerCase().replaceAll("[^a-z0-9]", "_");
        return Common.id(prefix + "_" + sanitized);
    }

    /**
     * Returns {@code true} if an editor for the given ID is already registered and open.
     */
    protected boolean isResourceOpen(Identifier editorId) {
        return Client.getEditorManager().getPanel(editorId) instanceof CodeEditor;
    }

    /**
     * If a {@link CodeEditor} for {@code editorId} already exists, focuses it and returns it.
     * Returns {@code null} if no such editor exists yet.
     */
    protected @Nullable CodeEditor getExistingEditor(Identifier editorId) {
        Panel panel = Client.getEditorManager().getPanel(editorId);
        if (panel instanceof CodeEditor editor) {
            editor.open();
            return editor;
        }
        return null;
    }
}