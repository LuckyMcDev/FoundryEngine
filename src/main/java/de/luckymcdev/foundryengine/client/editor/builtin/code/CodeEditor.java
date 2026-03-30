package de.luckymcdev.foundryengine.client.editor.builtin.code;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.builtin.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.extension.texteditor.TextEditor;
import imgui.extension.texteditor.TextEditorCoordinates;
import imgui.extension.texteditor.TextEditorLanguageDefinition;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.Identifier;

import java.util.Collections;

/**
 * InGame Code editor. May be replaced with MonacoFx at some point IDK yet.
 */
public class CodeEditor extends EditorPanel {
    private static final String POPUP_SAVE_CONFIRM = "Unsaved Changes?###save_confirm";
    private static final String POPUP_GOTO_LINE = "Go to Line###goto_line";
    private static final float BUTTON_WIDTH = 120.0f;
    private static final float FONT_SCALE_MIN = 0.1f;
    private static final float FONT_SCALE_MAX = 3.0f;
    private static final float FONT_SCALE_STEP = 0.1f;
    private final TextEditor textEditor;
    private final ImString findText = new ImString(256);
    private final ImString replaceText = new ImString(256);
    private final ImBoolean matchCase = new ImBoolean(false);
    private final ImBoolean wholeWord = new ImBoolean(false);
    public boolean customLangOverride;
    public boolean forceReadOnly = false;
    private String fileName;
    private String oldSource;
    private SaveCallback saveCallback;
    private boolean showFind = false;
    private boolean showReplace = false;
    private int gotoLineTarget = 1;
    private float fontScale = 1.0f;

    /**
     * Creates a new CodeEditor panel.
     *
     * @param id       Unique resource identifier for the panel.
     * @param fileName Display name shown in the title bar and status bar.
     * @param source   Initial source text to populate the editor.
     */
    public CodeEditor(Identifier id, String fileName, String source) {
        super(id, ImIcons.FA.FA_EDIT + " Editor: " + fileName);
        this.menuBar = true;
        this.fileName = fileName;
        this.oldSource = source;
        this.saveCallback = (_, _) -> { /* default no-op */ };

        this.textEditor = new TextEditor();
        this.textEditor.setShowWhitespaces(false);
        this.textEditor.setText(source);
        this.textEditor.setPalette(textEditor.getDarkPalette());

        this.category = PanelCategory.EDITOR_FILES;

        if (!customLangOverride) {
            this.textEditor.setLanguageDefinition(TextEditorLanguageDefinition.GLSL());
        }
    }

    /**
     * Replaces the current file content with new data and resets editor state.
     * Error markers and unsaved-changes flag are cleared.
     *
     * @param fileName New display name.
     * @param source   New source text.
     */
    public void load(String fileName, String source) {
        this.fileName = fileName;
        this.oldSource = source;
        this.textEditor.setText(source);
        this.textEditor.setErrorMarkers(Collections.emptyMap());
        this.unsaved = false;
        this.open();
    }

    /**
     * Returns {@code true} if the editor content differs from the last saved state.
     */
    public boolean isDirty() {
        return !this.oldSource.equals(this.textEditor.getText());
    }

    /**
     * Triggers the {@link SaveCallback}, applies any returned error markers, and
     * — when no errors were reported — promotes the current text to the clean
     * baseline.
     */
    private void save() {
        Int2ObjectMap<String> errors = new Int2ObjectArrayMap<>();
        if (this.saveCallback != null) {
            this.saveCallback.save(this.textEditor.getText(), errors);
        }

        if (errors.isEmpty()) {
            this.oldSource = this.textEditor.getText();
            this.unsaved = false;
        }

        this.textEditor.setErrorMarkers(errors);
    }

    @Override
    public void content() {
        if (!Client.getMinecraft().isSingleplayer() && !forceReadOnly && !textEditor.isReadOnly()) {
            ImGui.text("Editing is disabled in multiplayer.");
            return;
        }

        this.unsaved = isDirty() && !forceReadOnly;

        renderMenuBar();
        handleShortcuts();
        renderFindBar();

        // Reserve space at the bottom for the status bar
        float footerHeight = ImGui.getTextLineHeightWithSpacing()
                + ImGui.getStyle().getItemSpacingY() + 5.0f;
        float editorHeight = ImGui.getContentRegionAvailY() - footerHeight;

        // Apply font scale around the editor widget
        ImGui.setWindowFontScale(fontScale);
        textEditor.render("##source", ImGui.getContentRegionAvailX(), editorHeight);
        ImGui.setWindowFontScale(1.0f);

        renderStatusBar();
        renderSavePopup();
        renderGotoLinePopup();
    }

    @Override
    public void onClosed() {
        if (isDirty() && !forceReadOnly) {
            this.open();
            ImGui.openPopup(POPUP_SAVE_CONFIRM);
        }
    }

    private void renderMenuBar() {
        if (!ImGui.beginMenuBar()) return;

        // --- File ---
        if (ImGui.beginMenu("File")) {
            if (ImGui.menuItem("Save", "Ctrl+S", false, isDirty() && !forceReadOnly)) save();
            ImGui.separator();
            if (ImGui.menuItem("Close")) this.close();
            ImGui.endMenu();
        }

        // --- Edit ---
        if (ImGui.beginMenu("Edit")) {
            boolean ro = textEditor.isReadOnly();

            // Disable read-only toggle if forceReadOnly is true
            if (ImGui.menuItem("Read-only mode", "", ro, !forceReadOnly)) {
                textEditor.setReadOnly(!ro);
            }
            if (ImGui.menuItem("Show Whitespace", "", textEditor.isShowingWhitespaces()))
                textEditor.setShowWhitespaces(!textEditor.isShowingWhitespaces());

            ImGui.separator();

            ImGui.beginDisabled(ro);
            if (ImGui.menuItem("Undo", "Ctrl+Z", false, textEditor.canUndo())) textEditor.undo();
            if (ImGui.menuItem("Redo", "Ctrl+Y", false, textEditor.canRedo())) textEditor.redo();
            ImGui.endDisabled();

            ImGui.separator();

            if (ImGui.menuItem("Copy", "Ctrl+C", false, textEditor.hasSelection())) textEditor.copy();
            ImGui.beginDisabled(ro);
            if (ImGui.menuItem("Cut", "Ctrl+X", false, textEditor.hasSelection())) textEditor.cut();
            if (ImGui.menuItem("Paste", "Ctrl+V", false, ImGui.getClipboardText() != null)) textEditor.paste();
            if (ImGui.menuItem("Delete", "Del", false, textEditor.hasSelection())) textEditor.delete();
            if (ImGui.menuItem("Select All", "Ctrl+A", false, textEditor.getTotalLines() > 0))
                textEditor.setSelection(0, 0, textEditor.getTotalLines(), 0);
            ImGui.endDisabled();

            ImGui.endMenu();
        }

        // --- View ---
        if (ImGui.beginMenu("View")) {
            if (ImGui.menuItem("Dark Palette")) textEditor.setPalette(textEditor.getDarkPalette());
            if (ImGui.menuItem("Light Palette")) textEditor.setPalette(textEditor.getLightPalette());
            if (ImGui.menuItem("Retro Blue Palette")) textEditor.setPalette(textEditor.getRetroBluePalette());

            ImGui.separator();

            if (ImGui.menuItem("Zoom In", "Ctrl+Mup")) adjustFontScale(+FONT_SCALE_STEP);
            if (ImGui.menuItem("Zoom Out", "Ctrl+MDown")) adjustFontScale(-FONT_SCALE_STEP);
            if (ImGui.menuItem("Reset Zoom")) fontScale = 1.0f;

            ImGui.endMenu();
        }

        // --- Search ---
        if (ImGui.beginMenu("Search")) {
            if (ImGui.menuItem("Find", "Ctrl+F")) toggleFind(false);
            if (ImGui.menuItem("Find/Replace", "Ctrl+H")) toggleFind(true);
            if (ImGui.menuItem("Go to Line…", "Ctrl+G")) openGotoLine();
            ImGui.endMenu();
        }

        // File name hint on the right side of the menu bar
        ImGui.separator();
        ImGui.textDisabled(fileName);

        ImGui.endMenuBar();
    }

    /**
     * Renders the inline find / replace bar just below the menu bar when active.
     * Pressing Escape or clicking the × button dismisses it.
     */
    private void renderFindBar() {
        if (!showFind) return;

        ImGui.pushStyleColor(imgui.flag.ImGuiCol.ChildBg,
                ImGui.getStyle().getColor(imgui.flag.ImGuiCol.FrameBg));

        float barHeight = showReplace
                ? ImGui.getFrameHeightWithSpacing() * 2 + ImGui.getStyle().getItemSpacingY() * 2 + 6
                : ImGui.getFrameHeightWithSpacing() + ImGui.getStyle().getItemSpacingY() + 4;

        if (ImGui.beginChild("##findBar", ImGui.getContentRegionAvailX(), barHeight,
                false, ImGuiWindowFlags.None)) {

            float inputWidth = 220.0f;

            // ---- Row 1: Find ----
            ImGui.text("Find:");
            ImGui.sameLine();
            ImGui.setNextItemWidth(inputWidth);
            ImGui.inputText("##find", findText, ImGuiInputTextFlags.None);

            ImGui.sameLine();
            if (ImGui.smallButton("▲")) findPrev();
            ImGui.sameLine();
            if (ImGui.smallButton("▼")) findNext();

            ImGui.sameLine();
            ImGui.checkbox("Match case", matchCase);
            ImGui.sameLine();
            ImGui.checkbox("Whole word", wholeWord);

            ImGui.sameLine();
            if (ImGui.smallButton("×")) {
                showFind = false;
                showReplace = false;
            }

            // ---- Row 2: Replace (conditional) ----
            if (showReplace) {
                ImGui.text("Replace:");
                ImGui.sameLine();
                ImGui.setNextItemWidth(inputWidth);
                ImGui.inputText("##replace", replaceText, ImGuiInputTextFlags.None);

                ImGui.sameLine();
                if (ImGui.smallButton("Replace")) replaceNext();
                ImGui.sameLine();
                if (ImGui.smallButton("Replace All")) replaceAll();
            }
        }
        ImGui.endChild();
        ImGui.popStyleColor();
    }

    /**
     * Opens or toggles the find bar.
     *
     * @param withReplace {@code true} to also show the replace row.
     */
    private void toggleFind(boolean withReplace) {
        if (!showFind) {
            showFind = true;
            showReplace = withReplace;
        } else if (withReplace && !showReplace) {
            showReplace = true;
        } else {
            showFind = false;
            showReplace = false;
        }
    }

    /**
     * Searches forward for the next occurrence of the find term starting from the
     * current cursor position. Wraps around to the beginning of the file.
     */
    private void findNext() {
        String query = findText.get();
        if (query.isEmpty()) return;

        String text = textEditor.getText();
        int fromPos = getAbsoluteCursorPos(text);

        int idx = search(text, query, fromPos + 1);
        if (idx < 0) idx = search(text, query, 0); // wrap
        if (idx >= 0) selectRange(text, idx, query.length());
    }

    /**
     * Searches backward for the previous occurrence of the find term starting from
     * the current cursor position. Wraps around to the end of the file.
     */
    private void findPrev() {
        String query = findText.get();
        if (query.isEmpty()) return;

        String text = textEditor.getText();
        int fromPos = getAbsoluteCursorPos(text);

        int idx = searchReverse(text, query, fromPos - 1);
        if (idx < 0) idx = searchReverse(text, query, text.length()); // wrap
        if (idx >= 0) selectRange(text, idx, query.length());
    }

    /**
     * Replaces the currently selected occurrence (if it matches the find term)
     * then advances to the next match.
     */
    private void replaceNext() {
        String query = findText.get();
        if (query.isEmpty()) return;

        if (textEditor.hasSelection()) {
            String sel = textEditor.getSelectedText();
            boolean matches = matchCase.get()
                    ? sel.equals(query)
                    : sel.equalsIgnoreCase(query);
            if (matches) {
                textEditor.delete();
                textEditor.insertText(replaceText.get());
            }
        }
        findNext();
    }

    /**
     * Replaces every occurrence of the find term in the entire buffer with the
     * replacement string.
     */
    private void replaceAll() {
        String query = findText.get();
        if (query.isEmpty()) return;

        String text = textEditor.getText();
        String newText = matchCase.get()
                ? text.replace(query, replaceText.get())
                : text.replaceAll("(?i)" + java.util.regex.Pattern.quote(query),
                java.util.regex.Matcher.quoteReplacement(replaceText.get()));
        textEditor.setText(newText);
    }

    private int search(String text, String query, int from) {
        if (from < 0 || from >= text.length()) return -1;
        return matchCase.get()
                ? text.indexOf(query, from)
                : text.toLowerCase(java.util.Locale.ROOT)
                  .indexOf(query.toLowerCase(java.util.Locale.ROOT), from);
    }

    private int searchReverse(String text, String query, int from) {
        if (from < 0) return -1;
        from = Math.min(from, text.length() - query.length());
        return matchCase.get()
                ? text.lastIndexOf(query, from)
                : text.toLowerCase(java.util.Locale.ROOT)
                  .lastIndexOf(query.toLowerCase(java.util.Locale.ROOT), from);
    }

    /**
     * Converts the editor's current cursor line/column into an absolute character
     * offset within {@code text}.
     */
    private int getAbsoluteCursorPos(String text) {
        TextEditorCoordinates pos = textEditor.getCursorPosition();
        int line = 0, col = 0, offset = 0;
        for (int i = 0; i < text.length(); i++) {
            if (line == pos.mLine && col == pos.mColumn) return i;
            if (text.charAt(i) == '\n') {
                line++;
                col = 0;
            } else {
                col++;
            }
            offset = i + 1;
        }
        return offset;
    }

    /**
     * Selects a range in the editor given an absolute character offset.
     *
     * @param text   Full editor text (used for line/col conversion).
     * @param start  Absolute start offset.
     * @param length Length of the selection.
     */
    private void selectRange(String text, int start, int length) {
        int startLine = 0, startCol = 0, endLine = 0, endCol = 0;
        int i = 0;

        for (; i < start && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                startLine++;
                startCol = 0;
            } else {
                startCol++;
            }
        }
        endLine = startLine;
        endCol = startCol;
        for (int e = start + length; i < e && i < text.length(); i++) {
            if (text.charAt(i) == '\n') {
                endLine++;
                endCol = 0;
            } else {
                endCol++;
            }
        }

        textEditor.setSelection(startLine, startCol, endLine, endCol);
        textEditor.setCursorPosition(endLine, endCol);
    }

    /**
     * Opens the Go-to-Line popup initialized to the current cursor line.
     */
    private void openGotoLine() {
        gotoLineTarget = textEditor.getCursorPosition().mLine + 1;
        ImGui.openPopup(POPUP_GOTO_LINE);
    }

    private void renderGotoLinePopup() {
        if (!ImGui.beginPopupModal(POPUP_GOTO_LINE, ImGuiWindowFlags.AlwaysAutoResize)) return;

        ImGui.text("Enter line number (1 – " + textEditor.getTotalLines() + "):");

        ImInt buf = new ImInt(gotoLineTarget);
        if (ImGui.inputInt("##gotoLine", buf)) {
            gotoLineTarget = Math.clamp(buf.get(), 1, textEditor.getTotalLines());
        }

        if (ImGui.button("Go", BUTTON_WIDTH, 0)) {
            int line = Math.max(0, gotoLineTarget - 1);
            textEditor.setCursorPosition(line, 0);
            ImGui.closeCurrentPopup();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", BUTTON_WIDTH, 0)) {
            ImGui.closeCurrentPopup();
        }

        ImGui.endPopup();
    }

    private void renderStatusBar() {
        ImGui.separator();

        TextEditorCoordinates pos = textEditor.getCursorPosition();
        String overwrite = textEditor.isOverwrite() ? "OVR" : "INS";
        String zoomLabel = fontScale != 1.0f
                ? String.format(" | Zoom: %d%%", Math.round(fontScale * 100))
                : "";

        String roLabel = forceReadOnly ? " | [READ-ONLY]" : "";

        ImGui.text(String.format("Ln %d, Col %d | Lines: %d | %s%s%s",
                pos.mLine + 1, pos.mColumn + 1,
                textEditor.getTotalLines(),
                overwrite,
                zoomLabel,
                roLabel));

        if (isDirty()) {
            String indicator = "Unsaved Changes*";
            float textW = ImGui.calcTextSize(indicator).x;
            ImGui.sameLine(ImGui.getWindowWidth() - textW - ImGui.getStyle().getWindowPaddingX() * 2);
            ImGui.textColored(1.0f, 0.3f, 0.3f, 1.0f, indicator);
        }
    }

    private void handleShortcuts() {
        if (!ImGui.isWindowFocused(ImGuiFocusedFlags.RootAndChildWindows)) return;

        boolean ctrl = ImGui.getIO().getKeyCtrl();

        if (ctrl && ImGui.isKeyPressed(ImGuiKey.S) && isDirty() && !forceReadOnly) save();
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.F)) toggleFind(false);
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.H)) toggleFind(true);
        if (ctrl && ImGui.isKeyPressed(ImGuiKey.G)) openGotoLine();

        // Ctrl + mouse wheel scroll: up = zoom in, down = zoom out
        float wheel = ImGui.getIO().getMouseWheel();
        if (ctrl && wheel != 0) {
            adjustFontScale(wheel > 0 ? +FONT_SCALE_STEP : -FONT_SCALE_STEP);
        }

        // Escape dismisses the find bar
        if (showFind && ImGui.isKeyPressed(ImGuiKey.Escape)) {
            showFind = false;
            showReplace = false;
        }

        // Find-bar Enter key support
        if (showFind && ImGui.isKeyPressed(ImGuiKey.Enter)) {
            if (ImGui.getIO().getKeyShift()) findPrev();
            else findNext();
        }
    }

    private void renderSavePopup() {
        if (!ImGui.beginPopupModal(POPUP_SAVE_CONFIRM, ImGuiWindowFlags.AlwaysAutoResize)) return;

        ImGui.text("Do you want to save changes to " + fileName + "?");
        ImGui.textDisabled("Unsaved progress will be lost.");
        ImGui.separator();

        if (ImGui.button("Save", BUTTON_WIDTH, 0)) {
            save();
            ImGui.closeCurrentPopup();
            super.close();
        }
        ImGui.sameLine();
        if (ImGui.button("Discard", BUTTON_WIDTH, 0)) {
            this.oldSource = this.textEditor.getText();
            this.unsaved = false;
            ImGui.closeCurrentPopup();
            super.close();
        }
        ImGui.sameLine();
        if (ImGui.button("Cancel", BUTTON_WIDTH, 0)) {
            ImGui.closeCurrentPopup();
        }

        ImGui.endPopup();
    }

    /**
     * Adjusts the editor font scale by {@code delta}, clamped between
     * {@link #FONT_SCALE_MIN} and {@link #FONT_SCALE_MAX}.
     */
    private void adjustFontScale(float delta) {
        fontScale = Math.clamp(fontScale + delta, FONT_SCALE_MIN, FONT_SCALE_MAX);
    }

    /**
     * Registers a new save callback, replacing any previously set one.
     */
    public void setSaveCallback(SaveCallback saveCallback) {
        this.saveCallback = saveCallback;
    }

    /**
     * Returns the underlying {@link TextEditor} for advanced configuration.
     */
    public TextEditor getTextEditor() {
        return textEditor;
    }

    /**
     * Returns the current font-scale multiplier (default {@code 1.0f}).
     */
    public float getFontScale() {
        return fontScale;
    }

    /**
     * Directly sets the font-scale multiplier.
     *
     * @param fontScale Value clamped to [{@link #FONT_SCALE_MIN}, {@link #FONT_SCALE_MAX}].
     */
    public void setFontScale(float fontScale) {
        this.fontScale = Math.clamp(fontScale, FONT_SCALE_MIN, FONT_SCALE_MAX);
    }

    /**
     * Functional interface for delegating save operations to the caller.
     *
     * <p>The implementation should validate/compile {@code source} and populate
     * {@code errors} with any diagnostic messages keyed by 1-based line number.
     * An empty {@code errors} map signals a successful save.
     *
     * <pre>{@code
     * editor.setSaveCallback((source, errors) -> {
     *     try {
     *         MyCompiler.compile(source);
     *     } catch (CompileException e) {
     *         errors.put(e.getLine(), e.getMessage());
     *     }
     * });
     * }</pre>
     */
    @FunctionalInterface
    public interface SaveCallback {
        /**
         * @param source The current editor text to be saved/compiled.
         * @param errors Mutable map to fill with {@code lineNumber → message} pairs.
         *               Leave empty to indicate success.
         */
        void save(String source, Int2ObjectMap<String> errors);
    }
}