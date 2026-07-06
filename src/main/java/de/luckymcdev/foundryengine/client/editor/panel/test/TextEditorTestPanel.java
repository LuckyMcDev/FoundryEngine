package de.luckymcdev.foundryengine.client.editor.panel.test;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.client.imgui.text.ImGuiCoreTextEditor;
import de.luckymcdev.foundryengine.client.imgui.text.editor.EditorTheme;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;

public class TextEditorTestPanel extends Panel {
	public static final TextEditorTestPanel INSTANCE = new TextEditorTestPanel();

	private final ImGuiCoreTextEditor editor;

	private TextEditorTestPanel() {
		super(new Builder(Common.id("text_editor_test"))
			.icon(ImIcons.FILE_TEXT)
			.category(PanelCategory.OPEN));

		editor = new ImGuiCoreTextEditor(null, null, EditorTheme.dark().build());
		editor.setText("Hello, world!\nThis is a test.\n\nTab: indent\nCtrl+S: show text");
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		if (!requireLocal()) {
			return;
		}

		if (ImGui.button("Show Text")) {
			ImGui.setClipboardText(editor.getText());
			ImGui.openPopup("text_copied");
		}
		ImGui.sameLine();

		if (ImGui.button("Reset")) {
			editor.setText("Hello, world!\nThis is a test.\n\nTab: indent\nCtrl+S: show text");
		}

		if (ImGui.beginPopup("text_copied")) {
			ImGui.text("Text copied to clipboard!");
			ImGui.endPopup();
		}

		ImGui.separator();
		ImGui.text("Lines: " + editor.getTotalLines());

		editor.render("##editor", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY() - 20, false);
	}
}
