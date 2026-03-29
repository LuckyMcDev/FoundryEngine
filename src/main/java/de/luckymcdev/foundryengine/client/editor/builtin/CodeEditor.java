package de.luckymcdev.foundryengine.client.editor.builtin;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.minecraft.resources.Identifier;

import java.util.function.Consumer;

/**
 * An Ingame Code Editor.
 * Does NOT have syntax highlighting and only exists due to
 * ImGui java not having the ColorTextEditor in it from version 1.89 anymore.
 */
public class CodeEditor extends EditorPanel {
    private final ImString codeBuffer;
    private static final int BUFFER_MUL = 100;
    private String originalSource;
    private String fileName;
    private Consumer<String> saveCallback;

    public CodeEditor(Identifier id, String fileName, String source) {
        super(id, ImIcons.FA.FA_EDIT + " Editor: " + fileName);
        this.menuBar = true;
        this.fileName = fileName;
        this.originalSource = source;
        this.codeBuffer = new ImString(source.length() * BUFFER_MUL);
        this.codeBuffer.set(source);
    }

    public void load(String fileName, String source) {
        this.fileName = fileName;
        this.originalSource = source;
        this.codeBuffer.set(source);
        this.unsaved = false;
        this.open();
    }

    public boolean isDirty() {
        return !this.codeBuffer.get().equals(originalSource);
    }

    private void save() {
        if (saveCallback != null) {
            saveCallback.accept(this.codeBuffer.get());
        }
        ImGui.setClipboardText(this.codeBuffer.get());
        this.originalSource = this.codeBuffer.get();
        this.unsaved = false;
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

        float footerHeight = ImGui.getTextLineHeightWithSpacing() + ImGui.getStyle().getItemSpacingY() + 5;
        float editorHeight = ImGui.getContentRegionAvailY() - footerHeight;

        ImGui.beginChild("LineNumbers", 45, editorHeight, false, ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        String[] lines = codeBuffer.get().split("\n", -1);
        for (int i = 1; i <= lines.length; i++) {
            ImGui.textColored(0.5f, 0.5f, 0.5f, 1.0f, String.format("%3d", i));
        }
        ImGui.endChild();

        ImGui.sameLine();

        if (ImGui.beginChild("EditorArea", 0, editorHeight, false, ImGuiWindowFlags.HorizontalScrollbar)) {
            int flags = ImGuiInputTextFlags.AllowTabInput;
            ImGui.inputTextMultiline("##source", codeBuffer, -1f, -1f, flags);
            ImGui.endChild();
        }

        renderStatusBar(lines);
        renderSavePopup();
    }

    private void renderMenuBar() {
        if (ImGui.beginMenuBar()) {
            if (ImGui.beginMenu("File")) {
                if (ImGui.menuItem("Save", "Ctrl+S", false, isDirty())) {
                    save();
                }
                if (ImGui.menuItem("Open")) {
                    Client.getEditorManager().openPanel(FileExplorerPanel.INSTANCE);
                }
                if (ImGui.menuItem("Close")) {
                    this.close();
                }
                ImGui.endMenu();
            }

            ImGui.separator();
            ImGui.textDisabled(fileName);

            ImGui.endMenuBar();
        }
    }

    private void renderStatusBar(String[] lines) {
        ImGui.separator();

        ImGui.text("Encoding: UTF-8");

        String statusText = "Lines: " + lines.length;
        float textWidth = ImGui.calcTextSize(statusText).x;
        float padding = ImGui.getStyle().getWindowPaddingX();

        ImGui.sameLine(ImGui.getWindowWidth() - textWidth - (padding * 2));
        ImGui.text(statusText);
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
            ImGui.separator();

            if (ImGui.button("Save", 120, 0)) {
                save();
                ImGui.closeCurrentPopup();
                super.close();
            }
            ImGui.sameLine();
            if (ImGui.button("Discard", 120, 0)) {
                this.originalSource = this.codeBuffer.get();
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

    public void setSaveCallback(Consumer<String> saveCallback) {
        this.saveCallback = saveCallback;
    }
}