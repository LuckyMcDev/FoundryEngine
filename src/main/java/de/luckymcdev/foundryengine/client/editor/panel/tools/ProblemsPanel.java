package de.luckymcdev.foundryengine.client.editor.panel.tools;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.editor.panel.files.CodeEditor;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiSelectableFlags;
import imgui.flag.ImGuiTreeNodeFlags;
import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.resources.Identifier;

import java.util.Comparator;

public class ProblemsPanel extends EditorPanel {
	public static final ProblemsPanel INSTANCE = new ProblemsPanel();

	private ProblemsPanel() {
		super(new Builder(Common.id("problems"))
			.icon(ImIcons.EXCLAMATION_TRIANGLE)
			.category(PanelCategory.TOOLS)
			.menuBar(true));
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		renderMenuBar();

		var buffers = Client.getWorkspaceState().getAllBuffers();
		boolean anyErrors = false;

		for (var bufEntry : buffers.entrySet()) {
			Identifier bufferId = bufEntry.getKey();
			String fileName = bufEntry.getValue().filePath();
			Int2ObjectMap<String> errors = Client.getWorkspaceState().getBufferErrors(bufferId);
			if (errors.isEmpty()) {
				continue;
			}
			anyErrors = true;

			int treeFlags = ImGuiTreeNodeFlags.SpanAvailWidth;
			boolean open = ImGui.treeNodeEx("##file_" + bufferId, treeFlags, ImIcons.FILE_CODE + " " + fileName);
			ImGui.sameLine();
			ImGui.textDisabled("(" + errors.size() + ")");

			if (open) {
				var entries = errors.int2ObjectEntrySet().stream()
					.sorted(Comparator.comparingInt(e -> e.getIntKey()))
					.toList();
				for (var errEntry : entries) {
					int line = errEntry.getIntKey();
					String message = errEntry.getValue();
					ImGui.text("  " + line);
					ImGui.sameLine();
					ImGui.pushStyleColor(ImGuiCol.Text, 0.8f, 0.2f, 0.2f, 1.0f);
					if (ImGui.selectable(message + "##" + bufferId + "_" + line, false, ImGuiSelectableFlags.SpanAllColumns)) {
						navigateToLine(bufferId, line);
					}
					ImGui.popStyleColor();
				}
				ImGui.treePop();
			}
		}

		if (!anyErrors) {
			ImGui.textColored(0.5f, 0.5f, 0.5f, 1.0f, "No problems detected");
		}
	}

	private void renderMenuBar() {
		menuBar(() -> {
			if (ImGui.menuItem("Clear All")) {
				var buffers = Client.getWorkspaceState().getAllBuffers();
				for (var bufEntry : buffers.entrySet()) {
					Client.getWorkspaceState().setErrors(bufEntry.getKey(), new Int2ObjectArrayMap<>());
				}
			}
		});
	}

	private void navigateToLine(Identifier bufferId, int line) {
		var panel = Client.getEditorManager().getPanel(bufferId);
		if (panel instanceof CodeEditor editor) {
			editor.getTextEditor().setCursor(line - 1, 0);
			editor.open();
		}
	}
}