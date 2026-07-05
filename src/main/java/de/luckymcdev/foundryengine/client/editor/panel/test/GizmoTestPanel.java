package de.luckymcdev.foundryengine.client.editor.panel.test;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.Panel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.gizmo.ImGuiGizmoCamera;
import de.luckymcdev.foundryengine.client.imgui.gizmo.ImGuiGizmoConfig;
import de.luckymcdev.foundryengine.client.imgui.gizmo.ImGuiGizmoMiniBar;
import de.luckymcdev.foundryengine.client.imgui.gizmo.ImGuiGizmoScreen;
import de.luckymcdev.foundryengine.client.imgui.gizmo.ImGuiGizmoTheme;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import de.luckymcdev.foundryengine.common.Common;
import imgui.ImGui;
import org.joml.Matrix4f;

public class GizmoTestPanel extends Panel {
	public static final GizmoTestPanel INSTANCE = new GizmoTestPanel();

	private final ImGuiGizmoScreen gizmo;
	private final ImGuiGizmoCamera camera;
	private final Matrix4f modelMatrix;

	private boolean showMiniBar = true;
	private boolean showGrid = true;
	private boolean showViewCube = true;
	private boolean lockTransform = false;
	private boolean lockView = false;
	private int selectedTheme = 0;

	private GizmoTestPanel() {
		super(new Builder(Common.id("gizmo_test"))
			.icon(ImIcons.CUBE)
			.category(PanelCategory.OPEN));

		this.modelMatrix = new Matrix4f();
		this.camera = new ImGuiGizmoCamera();

		this.gizmo = new ImGuiGizmoScreen("gizmo_test_panel")
			.setSize(0, 400)
			.setConfig(ImGuiGizmoConfig.universal().build())
			.setTheme(ImGuiGizmoTheme.dark().build())
			.setCamera(camera);

		ImGuiGizmoMiniBar miniBar = new ImGuiGizmoMiniBar(ImGuiGizmoMiniBar.Anchor.TOP_LEFT)
			.setButtonSize(30f, 26f);
		gizmo.setMiniBar(miniBar);

		resetMatrix();
	}

	private void resetMatrix() {
		modelMatrix.identity().scale(2f);
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		if (!requireLocal()) return;

		ImGui.text("Gizmo Configuration");
		ImGui.separator();

		if (ImGui.checkbox("Show Mini Bar##gizmo", showMiniBar)) {
			showMiniBar = !showMiniBar;
			gizmo.setMiniBar(showMiniBar ? new ImGuiGizmoMiniBar(ImGuiGizmoMiniBar.Anchor.TOP_LEFT) : null);
		}
		ImGui.sameLine();
		if (ImGui.checkbox("Show Grid##gizmo", showGrid)) {
			showGrid = !showGrid;
			gizmo.setConfig(gizmo.getConfig().withShowGrid(showGrid));
		}
		ImGui.sameLine();
		if (ImGui.checkbox("Show View Cube##gizmo", showViewCube)) {
			showViewCube = !showViewCube;
			gizmo.setConfig(gizmo.getConfig().withShowGrid(showViewCube));
		}

		if (ImGui.checkbox("Lock Transform##gizmo", lockTransform)) {
			lockTransform = !lockTransform;
			gizmo.setConfig(gizmo.getConfig().toBuilder().withLockTransform(lockTransform).build());
		}

		ImGui.sameLine();
		if (ImGui.checkbox("Lock View##gizmo", lockView)) {
			lockView = !lockView;
			camera.setLocked(lockView);
		}

		if (ImGui.radioButton("Dark Theme##gizmo", selectedTheme == 0)) {
			selectedTheme = 0;
			gizmo.setTheme(ImGuiGizmoTheme.dark().build());
		}
		ImGui.sameLine();
		if (ImGui.radioButton("Warm Theme##gizmo", selectedTheme == 1)) {
			selectedTheme = 1;
			gizmo.setTheme(ImGuiGizmoTheme.warm().build());
		}

		ImGui.separator();
		ImGui.text("Quick Configs:");

		if (ImGui.button("Translate")) {
			gizmo.setConfig(ImGuiGizmoConfig.translate().build());
		}
		ImGui.sameLine();
		if (ImGui.button("Rotate")) {
			gizmo.setConfig(ImGuiGizmoConfig.rotate().build());
		}
		ImGui.sameLine();
		if (ImGui.button("Scale")) {
			gizmo.setConfig(ImGuiGizmoConfig.scale().build());
		}
		ImGui.sameLine();
		if (ImGui.button("Bounds")) {
			gizmo.setConfig(ImGuiGizmoConfig.bounds().build());
		}

		if (ImGui.button("Reset Matrix")) {
			resetMatrix();
		}
		ImGui.sameLine();
		if (ImGui.button("Reset Camera")) {
			camera.reset();
		}

		ImGui.separator();
		ImGui.text("Camera (Yaw/Pitch/Distance):");
		ImGui.text(String.format("  Yaw: %.1f°  Pitch: %.1f°  Distance: %.2f", camera.getYaw(), camera.getPitch(), camera.getDistance()));

		ImGui.separator();
		ImGui.text("Model Matrix:");
		float[] m = gizmo.getMatrices().getModelRaw();
		ImGui.text(String.format("  [%.3f, %.3f, %.3f, %.3f]", m[0], m[1], m[2], m[3]));
		ImGui.text(String.format("  [%.3f, %.3f, %.3f, %.3f]", m[4], m[5], m[6], m[7]));
		ImGui.text(String.format("  [%.3f, %.3f, %.3f, %.3f]", m[8], m[9], m[10], m[11]));
		ImGui.text(String.format("  [%.3f, %.3f, %.3f, %.3f]", m[12], m[13], m[14], m[15]));

		ImGui.separator();
		ImGui.textWrapped("Interaction: Right-click drag to orbit · Mouse wheel to zoom · Drag colored arrows/circles to transform");

		gizmo.setModelMatrix(modelMatrix);
		gizmo.render();

		if (gizmo.wasImguiGizmoUsed()) {
			g.redTextIf("Gizmo in use", true);
		}

		modelMatrix.set(gizmo.getMatrices().getModelMatrix());
	}
}