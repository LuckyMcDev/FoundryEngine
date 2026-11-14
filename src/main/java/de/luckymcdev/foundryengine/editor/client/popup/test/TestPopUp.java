package de.luckymcdev.foundryengine.editor.client.popup.test;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.editor.client.popup.PopUp;
import imgui.ImGui;
import imgui.flag.ImGuiPopupFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;

/**
 * Test popup used by {@code TestPanel} to exercise the {@link PopUp} system.
 * Modal, always-auto-resize.
 */
public class TestPopUp extends PopUp {
	public static final TestPopUp INSTANCE = new TestPopUp();

	private final ImString name = new ImString(64);
	private int counter = 0;

	private TestPopUp() {
		super(
			Common.id("test_popup"),
			"Test Popup",
			true,
			ImGuiPopupFlags.None,
			ImGuiWindowFlags.AlwaysAutoResize
		);
	}

	@Override
	public void content() {
		ImGui.text("Hello from a modal popup!");
		ImGui.separator();

		ImGui.textWrapped(
			"This popup is registered with the EditorManager and rendered " +
				"automatically every frame. Try the controls below."
		);

		ImGui.spacing();
		ImGui.text("Your name:");
		ImGui.inputText("##name", name);
		if (!name.get().isBlank()) {
			ImGui.textColored(0xFF7EC8E3, "Hello, " + name.get() + "!");
		}

		ImGui.spacing();
		ImGui.separator();
		ImGui.spacing();

		if (ImGui.button("Increment", 120, 0)) {
			counter++;
		}
		ImGui.sameLine();
		ImGui.text("Count: " + counter);

		ImGui.spacing();
		ImGui.separator();

		float closeWidth = 120;
		if (ImGui.button("Close", closeWidth, 0)) {
			close();
		}
	}
}