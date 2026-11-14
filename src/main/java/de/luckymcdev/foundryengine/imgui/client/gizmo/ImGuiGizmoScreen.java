package de.luckymcdev.foundryengine.imgui.client.gizmo;

import foundry.imgui.api.ImGuiMC;
import imgui.ImDrawList;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.extension.imguizmo.flag.Operation;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector4f;

public final class ImGuiGizmoScreen {

	private final String id;
	private final ImGuiGizmoMatrices matrices = new ImGuiGizmoMatrices();
	private final Matrix4f afterView = new Matrix4f();
	private ImGuiGizmoConfig config;
	private ImGuiGizmoTheme theme;
	private ImGuiGizmoCamera camera;
	private ImGuiGizmoMiniBar miniBar;
	private float width = 0.0f;
	private float height = 0.0f;
	private @Nullable AbstractTexture texture;
	private boolean flipTextureY = true;
	private boolean wasUsedLastFrame = false;

	// -1 means fall through to config value
	private int runtimeOperation = -1;
	private float runtimeGridSize = -1.0f;

	private boolean renderModelWireframe = false;
	private int modelWireframeColor = 0xFFEEEEEE;

	private float modelScale = 1.0f;

	public ImGuiGizmoScreen(String id) {
		this.id = id;
		config = ImGuiGizmoConfig.universal().build();
		theme = ImGuiGizmoTheme.dark().build();
		camera = new ImGuiGizmoCamera();
	}

	private static float colLen(float[] m, int off) {
		float a = m[off], b = m[off + 1], c = m[off + 2];
		return (float) Math.sqrt(a * a + b * b + c * c);
	}

	public ImGuiGizmoScreen setSize(float w, float h) {
		width = w;
		height = h;
		return this;
	}

	public ImGuiGizmoScreen setTexture(@Nullable AbstractTexture texture) {
		this.texture = texture;
		return this;
	}

	public ImGuiGizmoScreen setFlipTextureY(boolean flip) {
		flipTextureY = flip;
		return this;
	}

	public ImGuiGizmoScreen setRuntimeOperation(int op) {
		runtimeOperation = op;
		return this;
	}

	public ImGuiGizmoScreen setRuntimeGridSize(float s) {
		runtimeGridSize = s;
		return this;
	}

	public ImGuiGizmoScreen setRenderModelWireframe(boolean enable, int colorArgb) {
		this.renderModelWireframe = enable;
		this.modelWireframeColor = colorArgb;
		return this;
	}

	public ImGuiGizmoScreen setRenderModelWireframe(boolean enable) {
		return setRenderModelWireframe(enable, modelWireframeColor);
	}

	public ImGuiGizmoScreen setModelMatrix(Matrix4f m) {
		ImGuiGizmoMatrices.fromMatrix4f(m, matrices.modelRaw());
		matrices.sync();
		return this;
	}

	public void render() {
		ImVec2 avail = ImGui.getContentRegionAvail();
		float w = Math.max(1.0f, width > 0.0f ? width : avail.x);
		float h = Math.max(1.0f, height > 0.0f ? height : avail.y);

		ImGuiGizmoTheme.pushColor(ImGuiCol.ChildBg, theme.backgroundArgb);
		ImGuiGizmoTheme.pushColor(ImGuiCol.Border, theme.borderArgb);
		ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, theme.borderRounding);
		ImGui.pushStyleVar(ImGuiStyleVar.ChildBorderSize, theme.borderThickness);
		ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f);

		boolean childOk = ImGui.beginChild("##ImguiGizmoScreen_" + id, w, h, true,
			ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);

		ImGui.popStyleVar(3);
		ImGui.popStyleColor(2);

		if (!childOk) {
			ImGui.endChild();
			return;
		}

		ImVec2 winPos = ImGui.getWindowPos();
		ImVec2 winSize = ImGui.getWindowSize();
		float wx = winPos.x, wy = winPos.y;
		float ww = winSize.x, wh = winSize.y;

		matrices.updateFromCamera(camera, ww, wh);

		boolean isOrtho = config.orthographic || camera.isOrthographic();
		ImGuiGizmoMatrices.Context ctx = matrices.beginContext(isOrtho, wx, wy, ww, wh);

		float gs = runtimeGridSize > 0.0f ? runtimeGridSize : config.gridSize;
		if (config.showGrid) {
			ctx.drawGrid(gs);
		}

		if (texture != null) {
			float v0 = flipTextureY ? 1.0f : 0.0f;
			float v1 = flipTextureY ? 0.0f : 1.0f;
			ImGui.setCursorPos(0.0f, 0.0f);
			ImGuiMC.image(ImGuiMC.getTexture(texture), ww, wh, 0.0f, v0, 1.0f, v1);
			ctx.reanchor();
		}

		if (renderModelWireframe) {
			drawModelWireframe(wx, wy, ww, wh);
		}

		int operation = resolveOperation();
		if (!config.lockTransform && operation >= 0) {
			float[] snap = buildSnapArray(operation);
			wasUsedLastFrame = ctx.drawManipulator(operation, config.mode, snap, config.localBounds, config.boundsSnap);
		} else {
			wasUsedLastFrame = false;
		}

		camera.handleInput(config.lockView);

		if (!config.lockView && config.showViewCube) {
			boolean cubeChanged = ctx.drawViewCubeTopRight(
				theme.viewCubeSize, config.viewCubeArmLength, theme.viewCubeBackground);
			if (cubeChanged) {
				ImGuiGizmoMatrices.toMatrix4f(matrices.viewRaw(), afterView);
				camera.syncFromViewMatrix(afterView);
			}
		}

		if (miniBar != null) {
			miniBar.render(this, wx, wy, ww, wh);
		}

		matrices.sync();
		applyModelScale();
		ImGui.endChild();
	}

	private void drawModelWireframe(float wx, float wy, float ww, float wh) {
		Matrix4f mvp = new Matrix4f(matrices.getProjMatrix())
			.mul(matrices.getViewMatrix())
			.mul(matrices.getModelMatrix());

		ImDrawList dl = ImGui.getWindowDrawList();

		// Unit cube (±0.5) — the model matrix supplies the real size/orientation.
		float h = 0.5f;
		float[][] corners = {
			{-h,-h,-h},{ h,-h,-h},{ h, h,-h},{-h, h,-h},
			{-h,-h, h},{ h,-h, h},{ h, h, h},{-h, h, h},
		};
		int[][] edges = {
			{0,1},{1,2},{2,3},{3,0},
			{4,5},{5,6},{6,7},{7,4},
			{0,4},{1,5},{2,6},{3,7},
		};

		float[] sx = new float[8], sy = new float[8];
		boolean[] ok = new boolean[8];
		Vector4f p = new Vector4f();
		for (int i = 0; i < 8; i++) {
			p.set(corners[i][0], corners[i][1], corners[i][2], 1.0f);
			mvp.transform(p);
			if (p.w <= 0.0001f) { ok[i] = false; continue; }
			ok[i] = true;
			sx[i] = wx + (p.x / p.w * 0.5f + 0.5f) * ww;
			sy[i] = wy + (0.5f - p.y / p.w * 0.5f) * wh;
		}
		for (int[] e : edges) {
			if (!ok[e[0]] || !ok[e[1]]) continue;
			dl.addLine(sx[e[0]], sy[e[0]], sx[e[1]], sy[e[1]], modelWireframeColor, 1.5f);
		}

		// Local axes at the model origin (X=red, Y=green, Z=blue).
		drawLocalAxis(dl, mvp, wx, wy, ww, wh,  1.0f, 0.0f, 0.0f, 0xFF3030FF);
		drawLocalAxis(dl, mvp, wx, wy, ww, wh,  0.0f, 1.0f, 0.0f, 0xFF30FF30);
		drawLocalAxis(dl, mvp, wx, wy, ww, wh,  0.0f, 0.0f, 1.0f, 0xFFFF3030);
	}

	private void drawLocalAxis(ImDrawList dl, Matrix4f mvp,
							   float wx, float wy, float ww, float wh,
							   float x, float y, float z, int color) {
		Vector4f a = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f); mvp.transform(a);
		Vector4f b = new Vector4f(x,  y,  z, 1.0f); mvp.transform(b);
		if (a.w <= 0.0001f || b.w <= 0.0001f) return;
		float ax = wx + (a.x / a.w * 0.5f + 0.5f) * ww;
		float ay = wy + (0.5f - a.y / a.w * 0.5f) * wh;
		float bx = wx + (b.x / b.w * 0.5f + 0.5f) * ww;
		float by = wy + (0.5f - b.y / b.w * 0.5f) * wh;
		dl.addLine(ax, ay, bx, by, color, 2.0f);
	}

	// priority: mini-bar > runtimeOperation > config
	private int resolveOperation() {
		if (miniBar != null && miniBar.getSelectedOperation() >= 0) {
			return miniBar.getSelectedOperation();
		}
		return runtimeOperation >= 0 ? runtimeOperation : config.operation;
	}

	private float[] buildSnapArray(int operation) {
		if ((operation & Operation.TRANSLATE) != 0 && config.snapTranslation) {
			return new float[]{config.snapTranslationStep, config.snapTranslationStep, config.snapTranslationStep};
		}
		if ((operation & Operation.ROTATE) != 0 && config.snapRotation) {
			return new float[]{config.snapRotationDeg, config.snapRotationDeg, config.snapRotationDeg};
		}
		if (((operation & Operation.SCALE) != 0 || (operation & Operation.SCALEU) != 0) && config.snapScale) {
			return new float[]{config.snapScaleStep, config.snapScaleStep, config.snapScaleStep};
		}
		return null;
	}

	// Re-normalises the rotation columns and reapplies modelScale after the gizmo may have modified them.
	private void applyModelScale() {
		if (modelScale == 1.0f) {
			return;
		}
		float[] raw = matrices.modelRaw();
		float sx = colLen(raw, 0);
		float sy = colLen(raw, 4);
		float sz = colLen(raw, 8);
		if (sx < 0.0001f || sy < 0.0001f || sz < 0.0001f) {
			return;
		}
		float rx = modelScale / sx;
		float ry = modelScale / sy;
		float rz = modelScale / sz;
		raw[0] *= rx;
		raw[1] *= rx;
		raw[2] *= rx;
		raw[4] *= ry;
		raw[5] *= ry;
		raw[6] *= ry;
		raw[8] *= rz;
		raw[9] *= rz;
		raw[10] *= rz;
		matrices.sync();
	}

	public ImGuiGizmoMatrices getMatrices() {
		return matrices;
	}

	public boolean wasImguiGizmoUsed() {
		return wasUsedLastFrame;
	}

	public ImGuiGizmoCamera getCamera() {
		return camera;
	}

	public ImGuiGizmoScreen setCamera(ImGuiGizmoCamera cam) {
		camera = cam;
		return this;
	}

	public ImGuiGizmoConfig getConfig() {
		return config;
	}

	public ImGuiGizmoScreen setConfig(ImGuiGizmoConfig cfg) {
		config = cfg;
		return this;
	}

	public ImGuiGizmoTheme getTheme() {
		return theme;
	}

	public ImGuiGizmoScreen setTheme(ImGuiGizmoTheme t) {
		theme = t;
		return this;
	}

	public ImGuiGizmoMiniBar getMiniBar() {
		return miniBar;
	}

	public ImGuiGizmoScreen setMiniBar(ImGuiGizmoMiniBar bar) {
		miniBar = bar;
		return this;
	}

	public String getId() {
		return id;
	}

	public float getModelScale() {
		return modelScale;
	}

	public ImGuiGizmoScreen setModelScale(float scale) {
		modelScale = Math.max(0.0001f, scale);
		return this;
	}
}
