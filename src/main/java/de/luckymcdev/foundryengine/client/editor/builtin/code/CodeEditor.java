package de.luckymcdev.foundryengine.client.editor.builtin.code;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorCoordinates;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.Identifier;

import java.util.Collections;

/**
 * Ported Ingame Code Editor.
 * Merges Veil's advanced error marking and state management with the Foundry Panel system.
 */
public class CodeEditor extends EditorPanel {

    private final TextEditor textEditor;
    public boolean customLangOverride;
    private String fileName;
    private String oldSource;
    private SaveCallback saveCallback;

    public CodeEditor(Identifier id, String fileName, String source) {
        super(id, ImIcons.FA.FA_EDIT + " Editor: " + fileName);
        this.menuBar = true;
        this.fileName = fileName;
        this.oldSource = source;
        this.saveCallback = (_, _) -> {
            // Default no-op save callback, can be overridden by caller
        };

        this.textEditor = new TextEditor();
        this.textEditor.setShowWhitespaces(false);
        this.textEditor.setText(source);
        this.textEditor.setPalette(getTextEditor().getDarkPalette());

        if (!customLangOverride) {
            this.textEditor.setLanguageDefinition(TextEditorLanguageDefinition.GLSL());
        }
    }

    public void load(String fileName, String source) {
        this.fileName = fileName;
        this.oldSource = source;
        this.textEditor.setText(source);
        this.textEditor.setErrorMarkers(Collections.emptyMap());
        this.unsaved = false;
        this.open();
    }

    public boolean isDirty() {
        return !this.oldSource.equals(this.textEditor.getText());
    }

    private void save() {
        Int2ObjectMap<String> errors = new Int2ObjectArrayMap<>();
        if (this.saveCallback != null) {
            this.saveCallback.save(this.textEditor.getText(), errors);
        }

        // If there are no errors, we consider this the new 'original' state
        if (errors.isEmpty()) {
            this.oldSource = this.textEditor.getText();
            this.unsaved = false;
        }

        // Update the visual error markers in the gutter
        this.textEditor.setErrorMarkers(errors);
    }

    @Override
    public void content() {
        if (!Client.getMinecraft().isSingleplayer()) {
            ImGui.text("Not in singleplayer.");
            return;
        }
        this.unsaved = isDirty();

        renderMenuBar();
        handleShortcuts();

        // Status bar area calculation
        float footerHeight = ImGui.getTextLineHeightWithSpacing() + ImGui.getStyle().getItemSpacingY() + 5;
        float editorHeight = ImGui.getContentRegionAvailY() - footerHeight;

        // Render the actual editor
        textEditor.render("##source", ImGui.getContentRegionAvailX(), editorHeight);

        renderStatusBar();
        renderSavePopup();
    }

    private void renderMenuBar() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Save", "Ctrl+S", false, isDirty())) {
                    save();
                }
                if (ImGui.menuItem("Close")) {
                    this.close();
                }
                ImGui.endMenu();
            }

            if (ImGui.beginMenu("Edit")) {
                boolean immutable = textEditor.isReadOnly();

                if (ImGui.menuItem("Read-only mode", "", immutable)) textEditor.setReadOnly(!immutable);
                if (ImGui.menuItem("Show Whitespace", "", textEditor.isShowingWhitespaces()))
                    textEditor.setShowWhitespaces(!textEditor.isShowingWhitespaces());

                ImGui.separator();

                ImGui.beginDisabled(immutable);
                if (ImGui.menuItem("Undo", "Ctrl+Z", false, textEditor.canUndo())) textEditor.undo();
                if (ImGui.menuItem("Redo", "Ctrl+Y", false, textEditor.canRedo())) textEditor.redo();
                ImGui.endDisabled();

                ImGui.separator();

                if (ImGui.menuItem("Copy", "Ctrl+C", false, textEditor.hasSelection())) textEditor.copy();

                ImGui.beginDisabled(immutable);
                if (ImGui.menuItem("Cut", "Ctrl+X", false, textEditor.hasSelection())) textEditor.cut();
                if (ImGui.menuItem("Paste", "Ctrl+V", false, ImGui.getClipboardText() != null)) textEditor.paste();
                if (ImGui.menuItem("Delete", "Del", false, textEditor.hasSelection())) textEditor.delete();
                if (ImGui.menuItem("Select All", "Ctrl+A", false, textEditor.getTotalLines() > 0))
                    textEditor.setSelection(0, 0, textEditor.getTotalLines(), 0);
                ImGui.endDisabled();

                ImGui.endMenu();
            }

            if (ImGui.beginMenu("View")) {
                if (ImGui.menuItem("Dark Palette", false, true)) {
                    textEditor.setPalette(textEditor.getDarkPalette());
                }
                if (ImGui.menuItem("Light Palette", false, true)) {
                    textEditor.setPalette(textEditor.getLightPalette());
                }
                if (ImGui.menuItem("Retro Blue Palette", false, true)) {
                    textEditor.setPalette(textEditor.getRetroBluePalette());
                }
                ImGui.endMenu();
            }

            ImGui.separator();
            ImGui.textDisabled(fileName);

            ImGui.endMenuBar();
        }
    }

    private void renderStatusBar() {
        ImGui.separator();

        TextEditorCoordinates pos = textEditor.getCursorPosition();
        String overwrite = textEditor.isOverwrite() ? "OVR" : "INS";

        // Combined status info: Line/Col, Total Lines, and Insert Mode
        ImGui.text(String.format("Ln %d, Col %d | Lines: %d | %s",
                pos.mLine + 1, pos.mColumn + 1, textEditor.getTotalLines(), overwrite));

        // Right-aligned unsaved indicator
        if (isDirty()) {
            String statusText = "Unsaved Changes*";
            float textWidth = ImGui.calcTextSize(statusText).x;
            ImGui.sameLine(ImGui.getWindowWidth() - textWidth - (ImGui.getStyle().getWindowPaddingX() * 2));
            ImGui.textColored(1.0f, 0.3f, 0.3f, 1.0f, statusText);
        }
    }

    private void handleShortcuts() {
        if (ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)) {
            if (ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(ImGuiKey.S) && isDirty()) {
                save();
            }
        }
    }

    private void renderSavePopup() {
        if (ImGui.beginPopupModal("Unsaved Changes?###save_confirm", ImGuiWindowFlags.AlwaysAutoResize)) {
            ImGui.text("Do you want to save changes to " + fileName + "?");
            ImGui.textDisabled("Unsaved progress will be lost.");
            ImGui.separator();

            if (ImGui.button("Save", 120, 0)) {
                save();
                ImGui.closeCurrentPopup();
                super.close();
            }
            ImGui.sameLine();
            if (ImGui.button("Discard", 120, 0)) {
                this.oldSource = this.textEditor.getText();
                this.unsaved = false;
                ImGui.closeCurrentPopup();
                super.close();
            }
            ImGui.sameLine();
            if (ImGui.button("Cancel", 120, 0)) {
                ImGui.closeCurrentPopup();
            }
            ImGui.endPopup();
        }
    }

    @Override
    public void onClosed() {
        if (isDirty()) {
            this.open();
            ImGui.openPopup("Unsaved Changes?###save_confirm");
        }
    }

    public void setSaveCallback(SaveCallback saveCallback) {
        this.saveCallback = saveCallback;
    }

    public TextEditor getTextEditor() {
        return textEditor;
    }

    /**
     * Ported functional interface from Veil to handle complex save logic
     */
    @FunctionalInterface
    public interface SaveCallback {
        void save(String source, Int2ObjectMap<String> errors);
    }
}