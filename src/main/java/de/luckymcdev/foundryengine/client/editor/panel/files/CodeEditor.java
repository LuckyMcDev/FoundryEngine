package de.luckymcdev.foundryengine.client.editor.panel.files;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.ImGuiShortcut;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.imgui.text.ImGuiCoreTextEditor;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorTheme;
import de.luckymcdev.foundryengine.client.imgui.text.preset.glsl.GLSLAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.preset.glsl.GLSLColorizer;
import de.luckymcdev.foundryengine.client.imgui.text.preset.groovy.GroovyAutocompleteProvider;
import de.luckymcdev.foundryengine.client.imgui.text.preset.groovy.GroovyColorizer;
import imgui.ImGui;
import imgui.flag.ImGuiFocusedFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeEditor extends EditorPanel {
	private static final String POPUP_SAVE_CONFIRM = "Unsaved Changes?###save_confirm";
	private static final String POPUP_GOTO_LINE = "Go to Line###goto_line";
	private static final float BUTTON_WIDTH = 120.0f;
	private static final float FONT_SCALE_MIN = 0.1f;
	private static final float FONT_SCALE_MAX = 3.0f;
	private static final float FONT_SCALE_STEP = 0.1f;

	private final ImGuiCoreTextEditor textEditor;
	private final ImString findText = new ImString(256);
	private final ImString replaceText = new ImString(256);
	private final ImBoolean matchCase = new ImBoolean(false);
	private final ImBoolean wholeWord = new ImBoolean(false);
	public boolean forceReadOnly = false;
	private String fileName;
	private String oldSource;
	private SaveCallback saveCallback;
	private boolean showFind = false;
	private boolean showReplace = false;
	private int gotoLineTarget = 1;
	private float fontScale = 1.0f;

	public CodeEditor(Identifier id, Component label, String source) {
		super(new Builder(id, label)
			.icon(ImIcons.FA.FA_EDIT)
			.shortcut(ImGuiShortcut.empty())
			.category(PanelCategory.EDITOR_FILES)
			.menuBar(true));
		this.fileName = label.getString();
		this.oldSource = source;
		this.saveCallback = (_, _) -> {};
		this.textEditor = new ImGuiCoreTextEditor(null, null, EditorTheme.dark().build());
		this.textEditor.setText(source);
	}

	private static String extensionFrom(String fileName) {
		if (fileName == null || !fileName.contains(".")) return "";
		return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
	}

	public void load(String fileName, String source) {
		this.fileName = fileName;
		this.oldSource = source;
		this.textEditor.setText(source);
		this.setUnsaved(false);
		this.open();
	}

	public boolean isDirty() {
		return !this.oldSource.equals(this.textEditor.getText());
	}

	public void applyLanguage(String fileName) {
		String ext = extensionFrom(fileName);
		switch (ext) {
			case "groovy": {
				GroovyColorizer c = new GroovyColorizer();
				this.textEditor.setColorizer(c);
				this.textEditor.setProvider(new GroovyAutocompleteProvider());
				this.textEditor.getColorizer().invalidateAll();
				break;
			}
			case "fsh", "vsh", "glsl", "hlsl": {
				GLSLColorizer c = new GLSLColorizer();
				this.textEditor.setColorizer(c);
				this.textEditor.setProvider(new GLSLAutocompleteProvider(c));
				this.textEditor.getColorizer().invalidateAll();
				break;
			}
		}
	}

	private void save() {
		Int2ObjectMap<String> errors = new Int2ObjectArrayMap<>();
		if (this.saveCallback != null) {
			this.saveCallback.save(this.textEditor.getText(), errors);
		}
		if (errors.isEmpty()) {
			this.oldSource = this.textEditor.getText();
			this.setUnsaved(false);
		}
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		if (!requireLevelOnServer(PermissionLevel.OWNERS)) return;
		this.setUnsaved(isDirty() && !forceReadOnly);

		renderMenuBar();
		handleShortcuts();
		renderFindBar();

		float footerHeight = ImGui.getTextLineHeightWithSpacing()
			+ ImGui.getStyle().getItemSpacingY() + 5.0f;
		float editorHeight = ImGui.getContentRegionAvailY() - footerHeight;

		ImGui.setWindowFontScale(fontScale);
		textEditor.render("##source", ImGui.getContentRegionAvailX(), editorHeight, false);
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
		menuBar(() -> {
			if (ImGui.beginMenu("File")) {
				if (ImGui.menuItem("Save", "Ctrl+S", false, isDirty() && !forceReadOnly)) save();
				ImGui.separator();
				if (ImGui.menuItem("Close")) this.close();
				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Edit")) {
				boolean ro = textEditor.isReadOnly();

				if (ImGui.menuItem("Read-only mode", "", ro, !forceReadOnly)) {
					textEditor.setReadOnly(!ro);
				}

				ImGui.beginDisabled(ro);
				if (ImGui.menuItem("Undo", "Ctrl+Z", false, textEditor.canUndo())) textEditor.undo();
				if (ImGui.menuItem("Redo", "Ctrl+Y", false, textEditor.canRedo())) textEditor.redo();
				ImGui.endDisabled();

				ImGui.separator();

				if (ImGui.menuItem("Copy", "Ctrl+C", false, textEditor.hasSelection())) textEditor.copy();
				ImGui.beginDisabled(ro);
				if (ImGui.menuItem("Cut", "Ctrl+X", false, textEditor.hasSelection())) textEditor.cut();
				if (ImGui.menuItem("Paste", "Ctrl+V", false, ImGui.getClipboardText() != null)) textEditor.paste();
				if (ImGui.menuItem("Select All", "Ctrl+A")) textEditor.selectAll();
				ImGui.endDisabled();

				ImGui.endMenu();
			}

			if (ImGui.beginMenu("View")) {
				if (ImGui.menuItem("Zoom In", "Ctrl+Mup")) adjustFontScale(+FONT_SCALE_STEP);
				if (ImGui.menuItem("Zoom Out", "Ctrl+MDown")) adjustFontScale(-FONT_SCALE_STEP);
				if (ImGui.menuItem("Reset Zoom")) fontScale = 1.0f;
				ImGui.endMenu();
			}

			if (ImGui.beginMenu("Search")) {
				if (ImGui.menuItem("Find", "Ctrl+F")) toggleFind(false);
				if (ImGui.menuItem("Find/Replace", "Ctrl+H")) toggleFind(true);
				if (ImGui.menuItem("Go to Line\u2026", "Ctrl+G")) openGotoLine();
				ImGui.endMenu();
			}

			ImGui.separator();
			ImGui.textDisabled(fileName);
		});
	}

	private void renderFindBar() {
		if (!showFind) return;

		float barHeight = showReplace
			? ImGui.getFrameHeightWithSpacing() * 2 + ImGui.getStyle().getItemSpacingY() * 2 + 6
			: ImGui.getFrameHeightWithSpacing() + ImGui.getStyle().getItemSpacingY() + 4;

		if (ImGui.beginChild("##findBar", ImGui.getContentRegionAvailX(), barHeight,
			false, ImGuiWindowFlags.None)) {

			float inputWidth = 220.0f;

			ImGui.text("Find:");
			ImGui.sameLine();
			ImGui.setNextItemWidth(inputWidth);
			ImGui.inputText("##find", findText, ImGuiInputTextFlags.None);

			ImGui.sameLine();
			if (ImGui.smallButton("\u25B2")) findPrev();
			ImGui.sameLine();
			if (ImGui.smallButton("\u25BC")) findNext();

			ImGui.sameLine();
			ImGui.checkbox("Match case", matchCase);
			ImGui.sameLine();
			ImGui.checkbox("Whole word", wholeWord);

			ImGui.sameLine();
			if (ImGui.smallButton("\u00D7")) {
				showFind = false;
				showReplace = false;
			}

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
	}

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

	private void findNext() {
		String query = findText.get();
		if (query.isEmpty()) return;
		String text = textEditor.getText();
		int cursorPos = getCursorOffset(text);
		int idx = search(text, query, cursorPos + 1);
		if (idx < 0) idx = search(text, query, 0);
		if (idx >= 0) selectRange(text, idx, query.length());
	}

	private void findPrev() {
		String query = findText.get();
		if (query.isEmpty()) return;
		String text = textEditor.getText();
		int cursorPos = getCursorOffset(text);
		int idx = searchReverse(text, query, cursorPos - 1);
		if (idx < 0) idx = searchReverse(text, query, text.length());
		if (idx >= 0) selectRange(text, idx, query.length());
	}

	private void replaceNext() {
		String query = findText.get();
		if (query.isEmpty()) return;
		if (textEditor.hasSelection()) {
			String sel = textEditor.getSelectedText();
			boolean matches = matchCase.get()
				? sel.equals(query)
				: sel.equalsIgnoreCase(query);
			if (matches) {
				textEditor.replaceSelection(replaceText.get());
			}
		}
		findNext();
	}

	private void replaceAll() {
		String query = findText.get();
		if (query.isEmpty()) return;
		String text = textEditor.getText();
		String newText = matchCase.get()
			? text.replace(query, replaceText.get())
			: text.replaceAll("(?i)" + Pattern.quote(query),
			Matcher.quoteReplacement(replaceText.get()));
		textEditor.setText(newText);
	}

	private int search(String text, String query, int from) {
		if (from < 0 || from >= text.length()) return -1;
		String src = matchCase.get() ? text : text.toLowerCase(Locale.ROOT);
		String q = matchCase.get() ? query : query.toLowerCase(Locale.ROOT);
		if (!wholeWord.get()) return src.indexOf(q, from);
		int idx = src.indexOf(q, from);
		while (idx >= 0) {
			if (isWordBoundary(text, idx, query.length())) return idx;
			idx = src.indexOf(q, idx + 1);
		}
		return -1;
	}

	private int searchReverse(String text, String query, int from) {
		if (from < 0) return -1;
		from = Math.min(from, text.length());
		String src = matchCase.get() ? text : text.toLowerCase(Locale.ROOT);
		String q = matchCase.get() ? query : query.toLowerCase(Locale.ROOT);
		if (!wholeWord.get()) return src.lastIndexOf(q, from);
		int idx = src.lastIndexOf(q, from);
		while (idx >= 0) {
			if (isWordBoundary(text, idx, query.length())) return idx;
			if (idx == 0) return -1;
			idx = src.lastIndexOf(q, idx - 1);
		}
		return -1;
	}

	private boolean isWordBoundary(String text, int idx, int len) {
		boolean leftBound = idx == 0 || !Character.isLetterOrDigit(text.charAt(idx - 1));
		int end = idx + len;
		boolean rightBound = end >= text.length() || !Character.isLetterOrDigit(text.charAt(end));
		return leftBound && rightBound;
	}

	private int getCursorOffset(String text) {
		int line = textEditor.getCursorLine();
		int col = textEditor.getCursorColumn();
		int curLine = 0, curCol = 0;
		for (int i = 0; i < text.length(); i++) {
			if (curLine == line && curCol == col) return i;
			if (text.charAt(i) == '\n') { curLine++; curCol = 0; }
			else curCol++;
		}
		return text.length();
	}

	private void selectRange(String text, int start, int length) {
		int startLine = 0, startCol = 0, endLine = 0, endCol = 0;
		for (int i = 0; i < start && i < text.length(); i++) {
			if (text.charAt(i) == '\n') { startLine++; startCol = 0; }
			else startCol++;
		}
		endLine = startLine;
		endCol = startCol;
		for (int i = start; i < start + length && i < text.length(); i++) {
			if (text.charAt(i) == '\n') { endLine++; endCol = 0; }
			else endCol++;
		}
		textEditor.selectRegion(startLine, startCol, endLine, endCol);
	}

	private void openGotoLine() {
		gotoLineTarget = textEditor.getCursorLine() + 1;
		ImGui.openPopup(POPUP_GOTO_LINE);
	}

	private void renderGotoLinePopup() {
		if (!ImGui.beginPopupModal(POPUP_GOTO_LINE, ImGuiWindowFlags.AlwaysAutoResize)) return;

		ImGui.text("Enter line number (1 \u2013 " + textEditor.getTotalLines() + "):");

		ImInt buf = new ImInt(gotoLineTarget);
		if (ImGui.inputInt("##gotoLine", buf)) {
			gotoLineTarget = Math.clamp(buf.get(), 1, textEditor.getTotalLines());
		}

		if (ImGui.button("Go", BUTTON_WIDTH, 0)) {
			int line = Math.max(0, gotoLineTarget - 1);
			textEditor.setCursor(line, 0);
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

		int line = textEditor.getCursorLine() + 1;
		int col = textEditor.getCursorColumn() + 1;
		int totalLines = textEditor.getTotalLines();
		String zoomLabel = fontScale != 1.0f
			? String.format(" | Zoom: %d%%", Math.round(fontScale * 100))
			: "";
		String roLabel = forceReadOnly ? " | [READ-ONLY]" : "";

		ImGui.text(String.format("Ln %d, Col %d | Lines: %d%s%s",
			line, col, totalLines, zoomLabel, roLabel));

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

		float wheel = ImGui.getIO().getMouseWheel();
		if (ctrl && wheel != 0) {
			adjustFontScale(wheel > 0 ? +FONT_SCALE_STEP : -FONT_SCALE_STEP);
		}

		if (showFind && ImGui.isKeyPressed(ImGuiKey.Escape)) {
			showFind = false;
			showReplace = false;
		}

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
			this.setUnsaved(false);
			ImGui.closeCurrentPopup();
			super.close();
		}
		ImGui.sameLine();
		if (ImGui.button("Cancel", BUTTON_WIDTH, 0)) {
			ImGui.closeCurrentPopup();
		}

		ImGui.endPopup();
	}

	private void adjustFontScale(float delta) {
		fontScale = Math.clamp(fontScale + delta, FONT_SCALE_MIN, FONT_SCALE_MAX);
	}

	public void setSaveCallback(SaveCallback saveCallback) {
		this.saveCallback = saveCallback;
	}

	public ImGuiCoreTextEditor getTextEditor() {
		return textEditor;
	}

	public float getFontScale() {
		return fontScale;
	}

	public void setFontScale(float fontScale) {
		this.fontScale = Math.clamp(fontScale, FONT_SCALE_MIN, FONT_SCALE_MAX);
	}

	@FunctionalInterface
	public interface SaveCallback {
		void save(String source, Int2ObjectMap<String> errors);
	}
}
