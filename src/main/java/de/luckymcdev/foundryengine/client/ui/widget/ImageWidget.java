package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec2;

public class ImageWidget extends WidgetBase {
	private Identifier image = null;
	private ScaleType scaleType = ScaleType.STRETCH;
	private Vec2 tileScale = Vec2.ONE;
	private Vec2 imageSize = null;
	private NineSliceData nineSliceData = null;

	public ImageWidget(UIVec position, UIVec size) {
		super(position, size);
	}

	public <T extends ImageWidget> T setImage(Identifier image, int imageWidth, int imageHeight) {
		this.image = image;
		this.imageSize = new Vec2(imageWidth, imageHeight);
		return (T) this;
	}

	public Identifier getImage() {
		return this.image;
	}

	public <T extends ImageWidget> T setImage(Identifier image) {
		return this.setImage(image, 256, 256);
	}

	public <T extends ImageWidget> T setStretch() {
		this.scaleType = ScaleType.STRETCH;
		return (T) this;
	}

	public <T extends ImageWidget> T setTile(Vec2 tileScale) {
		this.scaleType = ScaleType.TILE;
		this.tileScale = tileScale;
		return (T) this;
	}

	public <T extends ImageWidget> T setNineSlice(int centerWidth, int centerHeight) {
		this.scaleType = ScaleType.NINESLICE;
		int centerX = (int) imageSize.x / 2;
		int centerY = (int) imageSize.y / 2;
		this.nineSliceData = new NineSliceData(
			centerX - centerWidth / 2, centerY - centerHeight / 2,
			centerX + centerWidth / 2, centerY + centerHeight / 2
		);
		return (T) this;
	}

	@Override
	void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		if (this.image == null || this.imageSize == null) {
			return;
		}
		UIArea drawArea = this.getRenderArea(tickDelta);
		switch (this.scaleType) {
			case STRETCH -> guiGraphics.blit(this.image, drawArea.x, drawArea.x + drawArea.width, drawArea.y, drawArea.y + drawArea.height, 0.0f, 1.0f, 0.0f, 1.0f);
			case TILE -> renderTiledTexture(guiGraphics, drawArea, new UIArea(0, 0, (int) imageSize.x, (int) imageSize.y));
			case NINESLICE -> {
				if (this.nineSliceData == null) {
					return;
				}
				int cornerWidth = nineSliceData.sliceLeft;
				int cornerHeight = nineSliceData.sliceTop;
				renderTexture(guiGraphics, new UIArea(drawArea.x, drawArea.y, cornerWidth, cornerHeight),
					new UIArea(0, 0, cornerWidth, cornerHeight));
				renderTexture(guiGraphics, new UIArea(drawArea.x + drawArea.width - cornerWidth, drawArea.y, cornerWidth, cornerHeight),
					new UIArea((int) imageSize.x - cornerWidth, 0, cornerWidth, cornerHeight));
				renderTexture(guiGraphics, new UIArea(drawArea.x, drawArea.y + drawArea.height - cornerHeight, cornerWidth, cornerHeight),
					new UIArea(0, (int) imageSize.y - cornerHeight, cornerWidth, cornerHeight));
				renderTexture(guiGraphics, new UIArea(drawArea.x + drawArea.width - cornerWidth, drawArea.y + drawArea.height - cornerHeight, cornerWidth, cornerHeight),
					new UIArea((int) imageSize.x - cornerWidth, (int) imageSize.y - cornerHeight, cornerWidth, cornerHeight));

				renderTiledTexture(guiGraphics, new UIArea(drawArea.x + cornerWidth, drawArea.y, drawArea.width - cornerWidth * 2, cornerHeight),
					new UIArea(cornerWidth, 0, (int) imageSize.x - cornerWidth * 2, cornerHeight));
				renderTiledTexture(guiGraphics, new UIArea(drawArea.x, drawArea.y + cornerHeight, cornerWidth, drawArea.height - cornerHeight * 2),
					new UIArea(0, cornerHeight, cornerWidth, (int) imageSize.y - cornerHeight * 2));
				renderTiledTexture(guiGraphics, new UIArea(drawArea.x + cornerWidth, drawArea.y + drawArea.height - cornerHeight,
						drawArea.width - cornerWidth * 2, cornerHeight),
					new UIArea(cornerWidth, (int) imageSize.y - cornerHeight, (int) imageSize.x - cornerWidth * 2, cornerHeight));
				renderTiledTexture(guiGraphics, new UIArea(drawArea.x + drawArea.width - cornerWidth, drawArea.y + cornerHeight,
						cornerWidth, drawArea.height - cornerHeight * 2),
					new UIArea((int) imageSize.x - cornerWidth, cornerHeight, cornerWidth, (int) imageSize.y - cornerHeight * 2));

				renderTiledTexture(guiGraphics, new UIArea(drawArea.x + cornerWidth, drawArea.y + cornerHeight, drawArea.width - cornerWidth * 2, drawArea.height - cornerHeight * 2),
					new UIArea(cornerWidth, cornerHeight, (int) imageSize.x - cornerWidth * 2, (int) imageSize.y - cornerHeight * 2));
			}
		}
	}

	private void renderTexture(GuiGraphicsExtractor guiGraphics, UIArea drawArea, UIArea subArea) {
		if (this.image == null) {
			return;
		}
		guiGraphics.blit(
			this.image,
			drawArea.x, drawArea.x + drawArea.width,
			drawArea.y, drawArea.y + drawArea.height,
			subArea.x / imageSize.x, (subArea.x + subArea.width) / imageSize.x,
			subArea.y / imageSize.y, (subArea.y + subArea.height) / imageSize.y
		);
	}

	private void renderTiledTexture(GuiGraphicsExtractor guiGraphics, UIArea drawArea, UIArea subArea) {
		if (this.image == null) {
			return;
		}
		int xStep = (int) (subArea.width * tileScale.x);
		int yStep = (int) (subArea.height * tileScale.y);
		int xTotal = Mth.ceil((double) drawArea.width / xStep);
		int yTotal = Mth.ceil((double) drawArea.height / yStep);
		guiGraphics.pose().pushMatrix();
		guiGraphics.enableScissor(drawArea.x, drawArea.y, drawArea.x + drawArea.width, drawArea.y + drawArea.height);
		for (int x = 0; x < xTotal; x++) {
			for (int y = 0; y < yTotal; y++) {
				int drawX = drawArea.x + xStep * x;
				int drawY = drawArea.y + yStep * y;
				guiGraphics.blit(
					this.image,
					drawX, drawX + xStep,
					drawY, drawY + yStep,
					subArea.x / imageSize.x, (subArea.x + subArea.width) / imageSize.x,
					subArea.y / imageSize.y, (subArea.y + subArea.height) / imageSize.y
				);
			}
		}
		guiGraphics.disableScissor();
		guiGraphics.pose().popMatrix();
	}

	enum ScaleType {
		STRETCH,
		TILE,
		NINESLICE
	}

	static class NineSliceData {
		public int sliceLeft;
		public int sliceTop;
		public int sliceRight;
		public int sliceBottom;

		public NineSliceData(int sliceLeft, int sliceTop, int sliceRight, int sliceBottom) {
			this.sliceLeft = sliceLeft;
			this.sliceTop = sliceTop;
			this.sliceRight = sliceRight;
			this.sliceBottom = sliceBottom;
		}
	}
}