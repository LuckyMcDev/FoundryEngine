package de.luckymcdev.foundryengine.editor.client.panel.test;

import de.luckymcdev.foundryengine.Common;
import de.luckymcdev.foundryengine.editor.client.config.PanelCategory;
import de.luckymcdev.foundryengine.editor.client.panel.Panel;
import de.luckymcdev.foundryengine.imgui.client.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.imgui.client.gizmo.ImGuiGizmoCamera;
import de.luckymcdev.foundryengine.imgui.client.gizmo.ImGuiGizmoConfig;
import de.luckymcdev.foundryengine.imgui.client.gizmo.ImGuiGizmoMiniBar;
import de.luckymcdev.foundryengine.imgui.client.gizmo.ImGuiGizmoScreen;
import de.luckymcdev.foundryengine.imgui.client.gizmo.ImGuiGizmoTheme;
import de.luckymcdev.foundryengine.imgui.client.icon.ImIcons;
import imgui.ImGui;
import imgui.extension.imguizmo.flag.Mode;
import imgui.extension.imguizmo.flag.Operation;
import org.joml.Matrix4f;

public class GizmoTestPanel extends Panel {
	public static final GizmoTestPanel INSTANCE = new GizmoTestPanel();

	private static final float MINI_BAR_BTN_W = 30.0f;
	private static final float MINI_BAR_BTN_H = 26.0f;
	private static final float[] DEFAULT_BOUNDS = {-0.5f, -0.5f, -0.5f, 0.5f, 0.5f, 0.5f};

	private final ImGuiGizmoScreen gizmo;
	private final ImGuiGizmoCamera camera;
	private final Matrix4f modelMatrix;

	// Kept alive so that toggling the bar off/on preserves button size and selection.
	private final ImGuiGizmoMiniBar miniBar;

	private boolean showMiniBar = true;
	private boolean showGrid = true;
	private boolean showViewCube = true;
	private boolean lockTransform = false;
	private boolean lockView = false;
	private int selectedTheme = 0;
	private boolean showMesh = true;

	private GizmoTestPanel() {
		super(new Builder(Common.id("gizmo_test"))
			.icon(ImIcons.CUBE)
			.category(PanelCategory.OPEN));

		this.modelMatrix = new Matrix4f();
		this.camera = new ImGuiGizmoCamera();

		this.miniBar = new ImGuiGizmoMiniBar(ImGuiGizmoMiniBar.Anchor.TOP_LEFT)
			.setButtonSize(MINI_BAR_BTN_W, MINI_BAR_BTN_H);

		this.gizmo = new ImGuiGizmoScreen("gizmo_test_panel")
			.setSize(0, 400)
			.setConfig(ImGuiGizmoConfig.universal().build())
			.setTheme(ImGuiGizmoTheme.dark().build())
			.setCamera(camera)
			.setMiniBar(miniBar)
			.setRenderModelWireframe(true);

		resetMatrix();
	}

	private void resetMatrix() {
		modelMatrix.identity().scale(2.0f);
	}

	/**
	 * Switch the operation/mode while preserving every other config setting.
	 */
	private void applyOperation(int op, int mode) {
		miniBar.setOperation(op);

		gizmo.setConfig(gizmo.getConfig().toBuilder()
			.withOperation(op)
			.withMode(mode)
			.withLocalBounds(null)
			.withBoundsSnap(null)
			.build());
	}

	private void applyBounds() {
		miniBar.setOperation(Operation.BOUNDS);

		gizmo.setConfig(gizmo.getConfig().toBuilder()
			.withOperation(Operation.BOUNDS)
			.withMode(Mode.LOCAL)
			.withLocalBounds(DEFAULT_BOUNDS)
			.withBoundsSnap(new float[]{0.1f, 0.1f, 0.1f, 0.1f, 0.1f, 0.1f})
			.build());
	}

	@Override
	public void content(ImGraphicsExtractor g) {
		ImGui.text("Gizmo Configuration");
		ImGui.separator();

		if (ImGui.checkbox("Show Mini Bar##gizmo", showMiniBar)) {
			showMiniBar = !showMiniBar;
			if (showMiniBar) {
				miniBar.setOperation(gizmo.getConfig().operation);
				gizmo.setMiniBar(miniBar);
			} else {
				gizmo.setMiniBar(null);
			}
		}
		ImGui.sameLine();
		if (ImGui.checkbox("Show Grid##gizmo", showGrid)) {
			showGrid = !showGrid;
			gizmo.setConfig(gizmo.getConfig().withShowGrid(showGrid));
		}
		ImGui.sameLine();
		if (ImGui.checkbox("Show View Cube##gizmo", showViewCube)) {
			showViewCube = !showViewCube;
			// Correct flag (was previously withShowGrid) and no direct mutator exists.
			gizmo.setConfig(gizmo.getConfig().toBuilder()
				.withShowViewCube(showViewCube)
				.build());
		}
		if (ImGui.checkbox("Show Mesh##gizmo", showMesh)) {
			showMesh = !showMesh;
			gizmo.setRenderModelWireframe(showMesh);
		}
		if (ImGui.checkbox("Lock Transform##gizmo", lockTransform)) {
			lockTransform = !lockTransform;
			gizmo.setConfig(gizmo.getConfig().toBuilder()
				.withLockTransform(lockTransform)
				.build());
		}

		ImGui.sameLine();
		if (ImGui.checkbox("Lock View##gizmo", lockView)) {
			lockView = !lockView;
			camera.setLocked(lockView);
			// Must also update the config, otherwise the view cube keeps drawing
			// and keeps mutating the camera via syncFromViewMatrix().
			gizmo.setConfig(gizmo.getConfig().toBuilder()
				.withLockView(lockView)
				.build());
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

		if (ImGui.button("Translate##gizmo")) {
			applyOperation(Operation.TRANSLATE, Mode.LOCAL);
		}
		ImGui.sameLine();
		if (ImGui.button("Rotate##gizmo")) {
			applyOperation(Operation.ROTATE, Mode.LOCAL);
		}
		ImGui.sameLine();
		if (ImGui.button("Scale##gizmo")) {
			applyOperation(Operation.SCALEU, Mode.LOCAL);
		}
		ImGui.sameLine();
		if (ImGui.button("Bounds##gizmo")) {
			applyBounds();
		}

		if (ImGui.button("Reset Matrix##gizmo")) {
			resetMatrix();
		}
		ImGui.sameLine();
		if (ImGui.button("Reset Camera##gizmo")) {
			camera.reset();
		}

		ImGui.separator();
		ImGui.text("Camera (Yaw/Pitch/Distance):");
		ImGui.text(String.format("  Yaw: %.1f\u00b0  Pitch: %.1f\u00b0  Distance: %.2f",
			camera.getYaw(), camera.getPitch(), camera.getDistance()));

		ImGui.separator();
		ImGui.text("Model Matrix:");
		float[] m = gizmo.getMatrices().getModelRaw();
		ImGui.text(String.format("  [%.3f, %.3f, %.3f, %.3f]", m[0], m[1], m[2], m[3]));
		ImGui.text(String.format("  [%.3f, %.3f, %.3f, %.3f]", m[4], m[5], m[6], m[7]));
		ImGui.text(String.format("  [%.3f, %.3f, %.3f, %.3f]", m[8], m[9], m[10], m[11]));
		ImGui.text(String.format("  [%.3f, %.3f, %.3f, %.3f]", m[12], m[13], m[14], m[15]));

		ImGui.separator();
		ImGui.textWrapped("Interaction: Right-click drag to orbit \u00b7 Mouse wheel to zoom \u00b7 Drag colored arrows/circles to transform");

		gizmo.setModelMatrix(modelMatrix);
		gizmo.render();

		g.redTextIf("Gizmo in use", gizmo.wasImguiGizmoUsed());

		modelMatrix.set(gizmo.getMatrices().getModelMatrix());
	}
}