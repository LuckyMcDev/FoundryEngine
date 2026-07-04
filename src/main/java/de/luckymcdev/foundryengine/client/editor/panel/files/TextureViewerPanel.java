package de.luckymcdev.foundryengine.client.editor.panel.files;

import de.luckymcdev.foundryengine.client.editor.config.PanelCategory;
import de.luckymcdev.foundryengine.client.editor.panel.editor.EditorPanel;
import de.luckymcdev.foundryengine.client.imgui.ImGraphicsExtractor;
import de.luckymcdev.foundryengine.client.imgui.icon.ImIcons;
import imgui.ImGui;
import imgui.ImVec2;
import imgui.flag.ImGuiWindowFlags;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.io.File;

public class TextureViewerPanel extends EditorPanel {

	private static final float MIN_ZOOM = 0.1f;
	private static final float MAX_ZOOM = 15f;
	private static final int CHECKER_SIZE = 8;

	private final Identifier textureIdentifier;
	private final File textureFile;

	private ImGraphicsExtractor.Image image;
	private String sourcePath;

	private boolean fitToWindow = true;
	private float zoom = 1f;

	public TextureViewerPanel(Identifier id, Component title, Identifier identifier) {
		super(new Builder(id, title)
			.icon(ImIcons.FA.FA_IMAGES)
			.category(PanelCategory.EDITOR_FILES));
		this.textureIdentifier = identifier;
		this.textureFile = null;
		loadTexture();
	}

	public TextureViewerPanel(Identifier id, Component title, File file) {
		super(new Builder(id, title)
			.icon(ImIcons.FA.FA_IMAGES)
			.category(PanelCategory.EDITOR_FILES));
		this.textureIdentifier = null;
		this.textureFile = file;
		loadTexture();
	}

	private void loadTexture() {
		ImGraphicsExtractor.Image old = this.image;
		if (textureIdentifier != null) {
			this.image = ImGraphicsExtractor.getTexture(textureIdentifier);
			this.sourcePath = textureIdentifier.toString();
		} else {
			this.image = ImGraphicsExtractor.getTexture(textureFile);
			this.sourcePath = textureFile.getAbsolutePath();
		}

		if (old != null) {
			old.close();
		}
	}


	@Override
	public void content(ImGraphicsExtractor g) {
		if (image.glId() == -1 || image.width() <= 0 || image.height() <= 0) {
			ImGui.textColored(1f, 0.4f, 0.4f, 1f, "Failed to load texture:");
			ImGui.textWrapped(sourcePath);
			if (ImGui.button("Retry")) {
				loadTexture();
			}
			return;
		}

		drawToolbar();
		ImGui.separator();

		float availWidth = Math.max(1f, ImGui.getContentRegionAvailX());
		float availHeight = Math.max(1f, ImGui.getContentRegionAvailY());

		float displayW;
		float displayH;
		if (fitToWindow) {
			float scale = Math.min(availWidth / image.width(), availHeight / image.height());
			displayW = image.width() * scale;
			displayH = image.height() * scale;
		} else {
			displayW = image.width() * zoom;
			displayH = image.height() * zoom;
		}

		ImGui.beginChild("##texture_scroll", availWidth, availHeight, false,
			ImGuiWindowFlags.HorizontalScrollbar);

		// Center horizontally if the image is narrower than the available space
		float centerOffset = Math.max(0, (ImGui.getContentRegionAvailX() - displayW) / 2f);
		if (centerOffset > 0) {
			ImGui.setCursorPosX(ImGui.getCursorPosX() + centerOffset);
		}

		drawCheckerboard(displayW, displayH);
		g.drawImage(image.glId(), displayW, displayH);

		if (ImGui.isItemHovered()) {
			ImGui.beginTooltip();
			ImGui.text(image.width() + " x " + image.height());
			ImGui.text(sourcePath);
			ImGui.endTooltip();

			if (!fitToWindow && ImGui.getIO().getKeyCtrl()) {
				float wheel = ImGui.getIO().getMouseWheel();
				if (wheel != 0) {
					zoom = Math.clamp(zoom + wheel * 0.1f, MIN_ZOOM, MAX_ZOOM);
				}
			}

			if (ImGui.isMouseDoubleClicked(0)) {
				fitToWindow = !fitToWindow;
			}
		}

		ImGui.endChild();
	}

	private void drawToolbar() {
		if (ImGui.button(fitToWindow ? "Fit: On" : "Fit: Off")) {
			fitToWindow = !fitToWindow;
			if (!fitToWindow) {
				zoom = 1f;
			}
		}
		ImGui.sameLine();

		ImGui.beginDisabled(fitToWindow);
		if (ImGui.button("-")) {
			zoom = Math.clamp(zoom - 0.25f, MIN_ZOOM, MAX_ZOOM);
		}
		ImGui.sameLine();
		ImGui.text(String.format("%.0f%%", fitToWindow ? 100f : zoom * 100f));
		ImGui.sameLine();
		if (ImGui.button("+")) {
			zoom = Math.clamp(zoom + 0.25f, MIN_ZOOM, MAX_ZOOM);
		}
		ImGui.sameLine();
		if (ImGui.button("100%")) {
			zoom = 1f;
		}
		ImGui.endDisabled();

		ImGui.sameLine();
		if (ImGui.button("Copy Path")) {
			ImGui.setClipboardText(sourcePath);
		}
		ImGui.sameLine();
		if (ImGui.button("Reload")) {
			loadTexture();
		}
	}

	private void drawCheckerboard(float w, float h) {
		ImVec2 origin = ImGui.getCursorScreenPos();
		var drawList = ImGui.getWindowDrawList();
		for (int y = 0; y * CHECKER_SIZE < h; y++) {
			boolean toggle = (y % 2 == 0);
			for (int x = 0; x * CHECKER_SIZE < w; x++) {
				float x0 = origin.x + x * CHECKER_SIZE;
				float y0 = origin.y + y * CHECKER_SIZE;
				float x1 = Math.min(x0 + CHECKER_SIZE, origin.x + w);
				float y1 = Math.min(y0 + CHECKER_SIZE, origin.y + h);
				int color = toggle ? 0xFF3A3A3A : 0xFF2A2A2A;
				drawList.addRectFilled(x0, y0, x1, y1, color);
				toggle = !toggle;
			}
		}
	}

	@Override
	protected void onClosed() {
		if(image != null) {
			image.close();
		}
	}
}