package de.luckymcdev.foundryengine.client.ui.screen;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.widget.WidgetBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Base class for container screens that integrates the Taffy-based widget system.
 * Extends {@link AbstractContainerScreen}
 * to provide slot rendering alongside custom widgets (buttons, panels, text, etc.).
 */
public abstract class EngineContainerScreen<M extends AbstractContainerMenu> extends AbstractContainerScreen<M> {

	private final WidgetBase root;
	private final WidgetBase backgroundRoot;
	private final boolean debug;
	float tick = 0.0f;
	long lastNanos = 0;
	private boolean widgetsInitialized;

	public EngineContainerScreen(M menu, Inventory inventory, Component title, boolean debug) {
		super(menu, inventory, title);
		this.root = new WidgetBase();
		this.backgroundRoot = new WidgetBase();
		this.debug = debug;
	}

	public EngineContainerScreen(M menu, Inventory inventory, Component title) {
		this(menu, inventory, title, false);
	}

	public boolean shouldDebug() {
		return this.debug;
	}

	/**
	 * Guards one-time widget construction across {@link #init()} calls. Minecraft re-invokes
	 * {@code init()} on every resize; returning {@code true} only on the first call prevents
	 * screens from re-adding their widget tree (which would render duplicates).
	 */
	protected final boolean shouldBuildWidgets() {
		if (widgetsInitialized) {
			return false;
		}
		widgetsInitialized = true;
		return true;
	}

	/**
	 * Whether {@link #init()} has completed at least once (i.e. the widget trees exist).
	 */
	protected final boolean isWidgetsInitialized() {
		return widgetsInitialized;
	}

	/**
	 * Area the widget trees are laid out within. Defaults to the full screen.
	 * Override and return {@link #getGuiArea()} to place widgets relative to the
	 * container GUI (so their coordinates ignore where the GUI is on-screen).
	 */
	protected UIArea getWidgetArea() {
		return new UIArea(0, 0, this.width, this.height);
	}

	/**
	 * The pixel rect occupied by the container GUI (slots + labels). Widgets laid out
	 * against this rect use {@code (0,0)} as the top-left of the container frame.
	 */
	protected final UIArea getGuiArea() {
		return new UIArea(this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
	}

	@Override
	protected void init() {
		super.init();
		lastNanos = System.nanoTime();
		this.root.onInit();
		this.root.updateArea(this.getWidgetArea());
		this.backgroundRoot.onInit();
		this.backgroundRoot.updateArea(this.getWidgetArea());
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		this.root.updateArea(this.getWidgetArea());
		this.backgroundRoot.updateArea(this.getWidgetArea());
	}

	/**
	 * Adds a foreground widget (rendered above slots, below carried item / tooltips).
	 */
	public void addWidget(WidgetBase widget) {
		this.root.addWidget(widget);
	}

	/**
	 * Adds foreground widgets.
	 */
	public void addWidgets(WidgetBase... widgets) {
		for (WidgetBase widget : widgets) {
			this.root.addWidget(widget);
		}
	}

	/**
	 * Removes a foreground widget.
	 */
	public void removeWidget(WidgetBase widget) {
		this.root.removeWidget(widget);
	}

	/**
	 * Adds a background widget (rendered behind slots & labels).
	 */
	public void addBackgroundWidget(WidgetBase widget) {
		this.backgroundRoot.addWidget(widget);
	}

	/**
	 * Adds background widgets.
	 */
	public void addBackgroundWidgets(WidgetBase... widgets) {
		for (WidgetBase widget : widgets) {
			this.backgroundRoot.addWidget(widget);
		}
	}

	/**
	 * Removes a background widget.
	 */
	public void removeBackgroundWidget(WidgetBase widget) {
		this.backgroundRoot.removeWidget(widget);
	}

	/**
	 * Override to run per-game-tick logic alongside the widgets.
	 */
	public void doTick() {
	}

	@Override
	protected final void containerTick() {
		super.containerTick();
		this.root.preTick();
		this.root.tick();
		this.backgroundRoot.preTick();
		this.backgroundRoot.tick();
		this.doTick();
	}

	private void renderWidget(WidgetBase widget, GuiGraphicsExtractor graphics, int mouseX, int mouseY, float tickDelta) {
		widget.preRender(graphics, mouseX, mouseY, tickDelta, this.debug);
		widget.render(graphics, mouseX, mouseY, tickDelta, this.debug);
		widget.postRender(graphics, mouseX, mouseY, tickDelta, this.debug);

		widget.preRenderChild(graphics, mouseX, mouseY, tickDelta);
		for (WidgetBase child : widget.getChildren()) {
			renderWidget(child, graphics, mouseX, mouseY, tickDelta);
		}
		widget.postRenderChild(graphics, mouseX, mouseY, tickDelta);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractBackground(graphics, mouseX, mouseY, partialTick);
		renderWidget(this.backgroundRoot, graphics, mouseX, mouseY, 1.0f);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		long now = System.nanoTime();
		tick += (now - lastNanos) / 50_000_000.0f;
		float tickDelta = tick - Mth.floor(tick);
		lastNanos = now;

		extractContents(guiGraphics, mouseX, mouseY, partialTick);

		renderWidget(this.root, guiGraphics, mouseX, mouseY, tickDelta);

		extractCarriedItem(guiGraphics, mouseX, mouseY);
		//? if 26.1 {
		extractSnapbackItem(guiGraphics);
		//?}
		extractTooltip(guiGraphics, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (this.root.mouseClicked(event.x(), event.y(), event.button())) {
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		if (this.root.mouseReleased(event.x(), event.y(), event.button())) {
			return true;
		}
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		if (this.root.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
			return true;
		}
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		if (this.root.mouseDragged(event.x(), event.y(), event.button(), dx, dy)) {
			return true;
		}
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		if (this.root.keyPressed(event.key(), event.scancode(), event.modifiers())) {
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		if (this.root.keyReleased(event.key(), event.scancode(), event.modifiers())) {
			return true;
		}
		return super.keyReleased(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		if (this.root.charTyped(event.codepointAsString(), 0)) {
			return true;
		}
		return super.charTyped(event);
	}
}