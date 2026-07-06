package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.UIVec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.world.item.ItemStack;

public class ItemWidget extends WidgetBase {
	private ItemStack item = ItemStack.EMPTY;

	public ItemWidget(UIVec position, UIVec size) {
		super(position, size);
	}

	public ItemStack getItem() {
		return this.item;
	}

	public <T extends ItemWidget> T setItem(ItemStack item) {
		this.item = item;
		return (T) this;
	}

	@Override
	void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		if (this.item == null || this.item.isEmpty()) {
			return;
		}
		UIArea drawArea = this.getRenderArea(tickDelta);
		if (debug) {
			guiGraphics.fill(RenderPipelines.GUI, drawArea.x, drawArea.y, drawArea.x + drawArea.width, drawArea.y + drawArea.height, 0xFF000000);
		}
		guiGraphics.pose().pushMatrix();
		float scaleX = drawArea.width / 16.0f;
		float scaleY = drawArea.height / 16.0f;
		guiGraphics.pose().translate(drawArea.x, drawArea.y);
		guiGraphics.pose().scale(scaleX, scaleY);
		guiGraphics.item(this.item, 0, 0);
		guiGraphics.itemDecorations(Minecraft.getInstance().font, this.item, 0, 0);
		guiGraphics.pose().popMatrix();
		if (this.contains(mouseX, mouseY)) {
			guiGraphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.item, mouseX, mouseY);
		}
	}
}