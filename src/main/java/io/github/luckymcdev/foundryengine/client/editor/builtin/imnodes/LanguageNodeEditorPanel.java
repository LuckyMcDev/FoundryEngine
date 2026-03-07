package io.github.luckymcdev.foundryengine.client.editor.builtin.imnodes;

import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.Client;
import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.client.imgui.imnodes.NodeEditorInstance;
import io.github.luckymcdev.foundryengine.client.imgui.imnodes.lang.NodeLanguageDefinition;
import io.github.luckymcdev.foundryengine.client.util.Shortcut;
import net.minecraft.resources.Identifier;

/**
 * A generic visual scripting panel driven by a {@link NodeLanguageDefinition}.
 *
 * <p>All language-specific behaviour (pin types, node catalog, context menu,
 * node body widgets, and code generation) is fully delegated to the definition.
 * This panel only handles layout and the ImGui child window split.
 *
 * <p>To create a panel for a new language, extend this class and pass in your
 * {@link NodeLanguageDefinition} implementation:
 *
 * <pre>{@code
 * public class GroovyEditorPanel extends LanguageNodeEditorPanel {
 *     public static final GroovyEditorPanel INSTANCE = new GroovyEditorPanel();
 *
 *     private GroovyEditorPanel() {
 *         super(
 *             Common.id("groovy_editor"),
 *             Shortcut.ctrl(ImGuiKey.G),
 *             new GroovyLanguageDefinition()
 *         );
 *     }
 * }
 * }</pre>
 */
public abstract class LanguageNodeEditorPanel extends Panel {

    /**
     * The language definition driving this panel's behaviour.
     */
    protected final NodeLanguageDefinition definition;

    /**
     * The live node graph editor instance.
     */
    protected final NodeEditorInstance<?> nodeEditor;

    /**
     * Fraction of the available horizontal width allocated to the node graph area.
     * The remaining space is given to the code preview. Default: {@code 0.65F}.
     */
    protected float graphWidthFraction = 0.65F;

    /**
     * Minimum pixel width of the node graph child window. Default: {@code 300F}.
     */
    protected float graphMinWidth = 300F;

    /**
     * Creates a new language-driven node editor panel.
     *
     * @param id         The unique resource-location identifier for this panel.
     * @param shortcut   The keyboard shortcut used to toggle the panel.
     * @param definition The {@link NodeLanguageDefinition} that drives this panel.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected LanguageNodeEditorPanel(Identifier id, Shortcut shortcut, NodeLanguageDefinition definition) {
        super(id, definition.languageName() + " Editor", shortcut);
        this.definition = definition;
        this.definition.ensureInitialised();
        // Delegate editor construction to the definition so it can control
        // the root node's pins (e.g. exec-out for Groovy vs data-in for GLSL).
        this.nodeEditor = definition.createEditor();
    }

    @Override
    public final void content() {
        if (!Client.getMinecraft().isSingleplayer()) {
            ImGui.text("Not in singleplayer.");
            return;
        }
        float fullWidth = ImGui.getContentRegionAvailX();
        float leftWidth = Math.max(graphMinWidth, fullWidth * graphWidthFraction);

        // Left side: node graph
        ImGui.beginChild("###lang-node-editor-" + getId(), leftWidth, 0, true);
        nodeEditor.render(
                node -> definition.buildContextMenu(nodeEditor),
                node -> definition.renderNodeBody(node, nodeEditor)
        );
        ImGui.endChild();

        ImGui.sameLine();

        // Right side: generated code preview
        ImGui.beginChild("###lang-code-preview-" + getId(), 0, 0, true);
        renderCodePreview();
        ImGui.endChild();
    }

    /**
     * Renders the right-hand code preview pane.
     *
     * <p>Override to customise the preview — for example to add a "Copy" button,
     * syntax highlighting, or a live execution button.
     * Call {@code super.renderCodePreview()} to include the default code listing.
     */
    protected void renderCodePreview() {
        ImGui.textUnformatted("Generated " + definition.languageName());
        ImGui.separator();

        String code = definition.generateCode(nodeEditor);

        for (String line : code.split("\n", -1)) {
            ImGui.textUnformatted(line);
        }
    }
}