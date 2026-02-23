package io.github.luckymcdev.foundryengine.client.editor.builtin.node;

import com.mojang.blaze3d.platform.InputConstants;
import imgui.ImGui;
import io.github.luckymcdev.foundryengine.client.imgui.node.lang.GroovyLanguageDefinition;
import io.github.luckymcdev.foundryengine.client.imgui.node.lang.NodeLanguageDefinition;
import io.github.luckymcdev.foundryengine.client.util.Shortcut;
import io.github.luckymcdev.foundryengine.common.Common;

/**
 * Singleton panel for the Groovy visual script editor.
 *
 * <p>Wires together {@link LanguageNodeEditorPanel} and {@link GroovyLanguageDefinition}.
 * Open with <b>Ctrl+G</b>.
 *
 * <p>Adding support for another language is as simple as creating a new
 * {@link NodeLanguageDefinition} subclass and a matching panel like this one.
 */
public class GroovyEditorPanel extends LanguageNodeEditorPanel {

    public static final GroovyEditorPanel INSTANCE = new GroovyEditorPanel();

    private GroovyEditorPanel() {
        super(
                Common.id("groovy_editor"),
                Shortcut.ctrl(InputConstants.KEY_E),
                new GroovyLanguageDefinition()
        );
    }

    /**
     * Extends the default code preview with a toolbar above the generated code.
     */
    @Override
    protected void renderCodePreview() {
        // Toolbar
        if (ImGui.button("Copy##groovy-copy")) {
            ImGui.setClipboardText(definition.generateCode(nodeEditor));
        }
        ImGui.sameLine();
        if (ImGui.button("Clear##groovy-clear")) {
            nodeEditor.clear();
        }
        ImGui.separator();

        // Default code listing from the base class
        super.renderCodePreview();
    }
}