package de.luckymcdev.foundryengine.client.ui.widget;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import dev.vfyjxf.taffy.geometry.TaffyLine;
import dev.vfyjxf.taffy.geometry.TaffyRect;
import dev.vfyjxf.taffy.geometry.TaffySize;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.AvailableSpace;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.FlexWrap;
import dev.vfyjxf.taffy.style.GridAutoFlow;
import dev.vfyjxf.taffy.style.GridPlacement;
import dev.vfyjxf.taffy.style.LengthPercentage;
import dev.vfyjxf.taffy.style.LengthPercentageAuto;
import dev.vfyjxf.taffy.style.TaffyDimension;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import dev.vfyjxf.taffy.style.TaffyPosition;
import dev.vfyjxf.taffy.style.TaffyStyle;
import dev.vfyjxf.taffy.style.TrackSizingFunction;
import dev.vfyjxf.taffy.tree.Layout;
import dev.vfyjxf.taffy.tree.NodeId;
import dev.vfyjxf.taffy.tree.TaffyTree;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.joml.Vector2i;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WidgetBase {
	final TaffyStyle style = new TaffyStyle();
	WidgetBase parent;
	TaffyTree tree;
	NodeId nodeId;
	UIArea rootAvailableArea;

	TaffyDisplay baseDisplay = TaffyDisplay.FLEX;
	float rotation = 0.0f;
	Vector2i offset = new Vector2i(0, 0);
	int zIndex = 0;

	boolean focused = false;
	boolean enabled = true;
	boolean visible = true;

	boolean shouldLerp = false;

	UIArea lastArea;
	UIArea uiArea;
	boolean areaChanged = false;

	Layout lastLayout;

	List<WidgetBase> children = new ArrayList<>();

	boolean initialized = false;

	@Nullable Component tooltip;

	private static NodeId buildNode(TaffyTree tree, WidgetBase widget) {
		widget.tree = tree;
		NodeId node = tree.newLeaf(widget.style);
		widget.nodeId = node;
		for (WidgetBase child : widget.children) {
			if (child.style.getDisplay() == TaffyDisplay.NONE) {
				continue;
			}
			tree.addChild(node, buildNode(tree, child));
		}
		return node;
	}

	public <T extends WidgetBase> T setWidth(float pixels) {
		this.style.size.width = TaffyDimension.length(pixels);
		return (T) this;
	}

	public <T extends WidgetBase> T setWidthPercent(float percent) {
		this.style.size.width = TaffyDimension.percent(percent);
		return (T) this;
	}

	public <T extends WidgetBase> T setWidth(TaffyDimension width) {
		this.style.size.width = width;
		return (T) this;
	}

	public <T extends WidgetBase> T setHeight(float pixels) {
		this.style.size.height = TaffyDimension.length(pixels);
		return (T) this;
	}

	public <T extends WidgetBase> T setHeightPercent(float percent) {
		this.style.size.height = TaffyDimension.percent(percent);
		return (T) this;
	}

	public <T extends WidgetBase> T setHeight(TaffyDimension height) {
		this.style.size.height = height;
		return (T) this;
	}

	public <T extends WidgetBase> T setSize(float width, float height) {
		this.setWidth(width);
		this.setHeight(height);
		return (T) this;
	}

	public <T extends WidgetBase> T setSize(TaffyDimension width, TaffyDimension height) {
		this.setWidth(width);
		this.setHeight(height);
		return (T) this;
	}

	public <T extends WidgetBase> T setFlexGrow(float grow) {
		this.style.flexGrow = grow;
		return (T) this;
	}

	public <T extends WidgetBase> T setFlexShrink(float shrink) {
		this.style.flexShrink = shrink;
		return (T) this;
	}

	public <T extends WidgetBase> T setFlexBasis(float pixels) {
		this.style.flexBasis = TaffyDimension.length(pixels);
		return (T) this;
	}

	public <T extends WidgetBase> T setFlexBasis(TaffyDimension basis) {
		this.style.flexBasis = basis;
		return (T) this;
	}

	public <T extends WidgetBase> T setAlignSelf(AlignItems alignSelf) {
		this.style.alignSelf = alignSelf;
		return (T) this;
	}

	public <T extends WidgetBase> T setMargin(int all) {
		this.style.margin = TaffyRect.all(LengthPercentageAuto.length(all));
		return (T) this;
	}

	public <T extends WidgetBase> T setMargin(int horizontal, int vertical) {
		this.style.margin = new TaffyRect<>(
			LengthPercentageAuto.length(horizontal),
			LengthPercentageAuto.length(horizontal),
			LengthPercentageAuto.length(vertical),
			LengthPercentageAuto.length(vertical)
		);
		return (T) this;
	}

	public <T extends WidgetBase> T setMargin(int left, int right, int top, int bottom) {
		this.style.margin = new TaffyRect<>(
			LengthPercentageAuto.length(left),
			LengthPercentageAuto.length(right),
			LengthPercentageAuto.length(top),
			LengthPercentageAuto.length(bottom)
		);
		return (T) this;
	}

	public <T extends WidgetBase> T setPadding(int all) {
		this.style.padding = TaffyRect.all(LengthPercentage.length(all));
		return (T) this;
	}

	public <T extends WidgetBase> T setPadding(int horizontal, int vertical) {
		this.style.padding = new TaffyRect<>(
			LengthPercentage.length(horizontal),
			LengthPercentage.length(horizontal),
			LengthPercentage.length(vertical),
			LengthPercentage.length(vertical)
		);
		return (T) this;
	}

	public <T extends WidgetBase> T setPadding(int left, int right, int top, int bottom) {
		this.style.padding = new TaffyRect<>(
			LengthPercentage.length(left),
			LengthPercentage.length(right),
			LengthPercentage.length(top),
			LengthPercentage.length(bottom)
		);
		return (T) this;
	}

	public <T extends WidgetBase> T setGap(int all) {
		this.style.gap = TaffySize.all(LengthPercentage.length(all));
		return (T) this;
	}

	public <T extends WidgetBase> T setGap(int rowGap, int columnGap) {
		this.style.gap = TaffySize.of(LengthPercentage.length(rowGap), LengthPercentage.length(columnGap));
		return (T) this;
	}

	public <T extends WidgetBase> T setDisplay(TaffyDisplay display) {
		this.baseDisplay = display;
		this.style.display = this.visible ? display : TaffyDisplay.NONE;
		return (T) this;
	}

	public <T extends WidgetBase> T setFlexDirection(FlexDirection direction) {
		this.style.flexDirection = direction;
		this.baseDisplay = TaffyDisplay.FLEX;
		this.style.display = this.visible ? TaffyDisplay.FLEX : TaffyDisplay.NONE;
		return (T) this;
	}

	public <T extends WidgetBase> T setFlexWrap(FlexWrap wrap) {
		this.style.flexWrap = wrap;
		return (T) this;
	}

	public <T extends WidgetBase> T setAlignItems(AlignItems alignItems) {
		this.style.alignItems = alignItems;
		return (T) this;
	}

	public <T extends WidgetBase> T setAlignContent(AlignContent alignContent) {
		this.style.alignContent = alignContent;
		return (T) this;
	}

	public <T extends WidgetBase> T setJustifyContent(AlignContent justifyContent) {
		this.style.justifyContent = justifyContent;
		return (T) this;
	}

	public <T extends WidgetBase> T setAspectRatio(float aspectRatio) {
		this.style.aspectRatio = aspectRatio;
		return (T) this;
	}

	public <T extends WidgetBase> T setPositionAbsolute() {
		this.style.position = TaffyPosition.ABSOLUTE;
		return (T) this;
	}

	public <T extends WidgetBase> T setInsetLeft(float pixels) {
		this.style.inset.left = LengthPercentageAuto.length(pixels);
		return (T) this;
	}

	public <T extends WidgetBase> T setInsetLeftPercent(float percent) {
		this.style.inset.left = LengthPercentageAuto.percent(percent);
		return (T) this;
	}

	public <T extends WidgetBase> T setInsetRight(float pixels) {
		this.style.inset.right = LengthPercentageAuto.length(pixels);
		return (T) this;
	}

	public <T extends WidgetBase> T setInsetRightPercent(float percent) {
		this.style.inset.right = LengthPercentageAuto.percent(percent);
		return (T) this;
	}

	public <T extends WidgetBase> T setInsetTop(float pixels) {
		this.style.inset.top = LengthPercentageAuto.length(pixels);
		return (T) this;
	}

	public <T extends WidgetBase> T setInsetTopPercent(float percent) {
		this.style.inset.top = LengthPercentageAuto.percent(percent);
		return (T) this;
	}

	public <T extends WidgetBase> T setInsetBottom(float pixels) {
		this.style.inset.bottom = LengthPercentageAuto.length(pixels);
		return (T) this;
	}

	public <T extends WidgetBase> T setInsetBottomPercent(float percent) {
		this.style.inset.bottom = LengthPercentageAuto.percent(percent);
		return (T) this;
	}

	public <T extends WidgetBase> T setInset(int left, int right, int top, int bottom) {
		this.style.inset = new TaffyRect<>(
			LengthPercentageAuto.length(left),
			LengthPercentageAuto.length(right),
			LengthPercentageAuto.length(top),
			LengthPercentageAuto.length(bottom)
		);
		return (T) this;
	}

	public <T extends WidgetBase> T setGridTemplateColumns(TrackSizingFunction... columns) {
		this.style.gridTemplateColumns = List.of(columns);
		this.baseDisplay = TaffyDisplay.GRID;
		this.style.display = this.visible ? TaffyDisplay.GRID : TaffyDisplay.NONE;
		return (T) this;
	}

	public <T extends WidgetBase> T setGridTemplateRows(TrackSizingFunction... rows) {
		this.style.gridTemplateRows = List.of(rows);
		this.baseDisplay = TaffyDisplay.GRID;
		this.style.display = this.visible ? TaffyDisplay.GRID : TaffyDisplay.NONE;
		return (T) this;
	}

	public <T extends WidgetBase> T setGridAutoFlow(GridAutoFlow flow) {
		this.style.gridAutoFlow = flow;
		return (T) this;
	}

	public <T extends WidgetBase> T setGridRow(int start, int end) {
		this.style.gridRow = new TaffyLine<>(GridPlacement.line(start), GridPlacement.line(end));
		return (T) this;
	}

	public <T extends WidgetBase> T setGridColumn(int start, int end) {
		this.style.gridColumn = new TaffyLine<>(GridPlacement.line(start), GridPlacement.line(end));
		return (T) this;
	}

	public float getRotation() {
		return this.rotation;
	}

	public <T extends WidgetBase> T setRotation(float rotation) {
		this.rotation = rotation;
		return (T) this;
	}

	public WidgetBase getParent() {
		return this.parent;
	}

	public <T extends WidgetBase> T setParent(WidgetBase parent) {
		this.parent = parent;
		return (T) this;
	}

	public Vector2i getOffset() {
		return this.offset;
	}

	public <T extends WidgetBase> T setOffset(Vector2i offset) {
		this.offset = offset;
		if (this.initialized) {
			this.updateArea();
		}
		return (T) this;
	}

	public int getZIndex() {
		return this.zIndex;
	}

	public <T extends WidgetBase> T setZIndex(int zIndex) {
		this.zIndex = zIndex;
		return (T) this;
	}

	public boolean getShouldLerp() {
		return this.shouldLerp;
	}

	public <T extends WidgetBase> T setShouldLerp(boolean shouldLerp) {
		this.shouldLerp = shouldLerp;
		return (T) this;
	}

	public TaffyStyle getStyle() {
		return this.style;
	}

	private WidgetBase getRoot() {
		if (this.parent == null) {
			return this;
		}
		return this.parent.getRoot();
	}

	public void updateArea() {
		WidgetBase root = this.getRoot();
		if (root != this) {
			root.recomputeLayout();
		}
	}

	public void updateArea(UIArea parentArea) {
		WidgetBase root = this.getRoot();
		if (root == this) {
			root.rootAvailableArea = parentArea;
		}
		root.recomputeLayout();
	}

	private void recomputeLayout() {
		if (this.rootAvailableArea == null) {
			return;
		}
		this.style.size = TaffySize.of(
			TaffyDimension.length(this.rootAvailableArea.width),
			TaffyDimension.length(this.rootAvailableArea.height)
		);
		this.tree = new TaffyTree();
		this.nodeId = this.tree.newLeaf(this.style);
		for (WidgetBase child : this.children) {
			if (child.style.getDisplay() == TaffyDisplay.NONE) {
				continue;
			}
			this.tree.addChild(this.nodeId, buildNode(this.tree, child));
		}
		this.tree.computeLayout(this.nodeId, TaffySize.of(
			AvailableSpace.definite(this.rootAvailableArea.width),
			AvailableSpace.definite(this.rootAvailableArea.height)
		));
		this.applyLayout(this, this.rootAvailableArea.x, this.rootAvailableArea.y);
	}

	private void applyLayout(WidgetBase widget, int originX, int originY) {
		if (widget.tree == null || widget.nodeId == null) {
			return;
		}
		Layout layout = widget.tree.getLayout(widget.nodeId);
		if (layout == null) {
			return;
		}
		int x = originX + (int) layout.location().x + widget.offset.x;
		int y = originY + (int) layout.location().y + widget.offset.y;
		widget.lastArea = widget.uiArea;
		widget.uiArea = new UIArea(x, y, (int) layout.size().width, (int) layout.size().height);
		widget.areaChanged = !Objects.equals(widget.lastArea, widget.uiArea);
		widget.lastLayout = layout;
		for (WidgetBase child : widget.children) {
			if (child.style.getDisplay() == TaffyDisplay.NONE) {
				continue;
			}
			this.applyLayout(child, x, y);
		}
	}

	public void onInit() {
		this.initialized = true;
		for (WidgetBase child : this.children) {
			child.onInit();
		}
	}

	public void preTick() {
		if (this.areaChanged) {
			this.lastArea = this.uiArea;
		}
		for (WidgetBase child : this.children) {
			child.preTick();
		}
	}

	public void tick() {
		for (WidgetBase child : this.children) {
			child.tick();
		}
	}

	public <T extends WidgetBase> T addWidget(WidgetBase widget) {
		widget.setParent(this);
		this.children.add(widget);
		this.onChildAdded(widget);
		return (T) this;
	}

	public <T extends WidgetBase> T removeWidget(WidgetBase widget) {
		if (this.children.remove(widget)) {
			this.onChildRemoved(widget);
		}
		return (T) this;
	}

	void onChildAdded(WidgetBase child) {
	}

	void onChildRemoved(WidgetBase child) {
	}

	public List<WidgetBase> getChildren() {
		return this.children;
	}

	public UIArea getArea() {
		return this.uiArea;
	}

	public Layout getLastLayout() {
		return this.lastLayout;
	}

	public UIArea getRenderArea(float tickDelta) {
		if (shouldLerp && this.lastArea != null) {
			return this.lastArea.lerp(this.uiArea, tickDelta);
		}
		return this.uiArea;
	}

	public boolean contains(double x, double y) {
		if (!this.visible || this.style.getDisplay() == TaffyDisplay.NONE) {
			return false;
		}
		return this.uiArea.isInArea(x, y);
	}

	public boolean isFocused() {
		return this.focused;
	}

	public void setFocused(boolean focused) {
		this.focused = focused;
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public boolean isVisible() {
		return this.visible;
	}

	public <T extends WidgetBase> T setVisible(boolean visible) {
		this.visible = visible;
		this.style.display = visible ? this.baseDisplay : TaffyDisplay.NONE;
		return (T) this;
	}

	public @Nullable Component getTooltip() {
		return this.tooltip;
	}

	public <T extends WidgetBase> T setTooltip(@Nullable Component tooltip) {
		this.tooltip = tooltip;
		return (T) this;
	}

	/**
	 * Queues a vanilla tooltip for the next frame when the widget is hovered. Call from a widget's
	 * {@code renderContent}/{@code renderOverlay} implementation.
	 */
	protected void renderTooltip(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
		if (this.tooltip != null && this.contains(mouseX, mouseY)) {
			guiGraphics.setTooltipForNextFrame(Minecraft.getInstance().font, this.tooltip, mouseX, mouseY);
		}
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (WidgetBase child : this.children) {
			if (child.enabled) {
				if (child.mouseClicked(mouseX, mouseY, button)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		for (WidgetBase child : this.children) {
			if (child.enabled) {
				if (child.mouseReleased(mouseX, mouseY, button)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
		for (WidgetBase child : this.children) {
			if (child.enabled) {
				if (child.mouseDragged(mouseX, mouseY, button, dx, dy)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		for (WidgetBase child : this.children) {
			if (child.enabled) {
				if (child.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean keyPressed(int key, int scanCode, int modifiers) {
		for (WidgetBase child : this.children) {
			if (child.enabled) {
				if (child.keyPressed(key, scanCode, modifiers)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean keyReleased(int key, int scanCode, int modifiers) {
		for (WidgetBase child : this.children) {
			if (child.enabled) {
				if (child.keyReleased(key, scanCode, modifiers)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean charTyped(char c, int modifiers) {
		for (WidgetBase child : this.children) {
			if (child.enabled) {
				if (child.charTyped(c, modifiers)) {
					return true;
				}
			}
		}
		return false;
	}

	private int getDepth(WidgetBase current, int depth) {
		if (current.parent == null) {
			return depth;
		}
		return getDepth(current.parent, depth + 1);
	}

	float getRenderDepth() {
		return zIndex * 0.1f + getDepth(this, 0) * 0.001f + this.parent.children.indexOf(this) * 0.00001f;
	}

	public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
		if (!visible) {
			return;
		}
		renderBackground(guiGraphics, mouseX, mouseY, tickDelta, debug);
		renderContent(guiGraphics, mouseX, mouseY, tickDelta, debug);
		renderOverlay(guiGraphics, mouseX, mouseY, tickDelta, debug);
	}

	void renderBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
	}

	void renderContent(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
	}

	void renderOverlay(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
	}

	public void preRender(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
	}

	public void postRender(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta, boolean debug) {
	}

	public void preRenderChild(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
	}

	public void postRenderChild(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float tickDelta) {
	}
}
