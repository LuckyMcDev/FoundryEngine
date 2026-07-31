package de.luckymcdev.foundryengine.client.ui.screen;

import de.luckymcdev.foundryengine.client.ui.UIArea;
import de.luckymcdev.foundryengine.client.ui.widget.WidgetBase;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public abstract class EngineScreen extends Screen {
	private final WidgetBase root;
	private final boolean debug;
	private boolean widgetsInitialized;
	float tick = 0.0f;
	long lastNanos = 0;

	public EngineScreen(boolean debug) {
		super(Component.empty());
		this.root = new WidgetBase();
		this.debug = debug;
	}

	public EngineScreen() {
		this(false);
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
	 * Whether {@link #init()} has completed at least once (i.e. the widget tree exists).
	 */
	protected final boolean isWidgetsInitialized() {
		return widgetsInitialized;
	}

	@Override
	protected void init() {
		lastNanos = System.nanoTime();
		this.root.onInit();
		this.root.updateArea(new UIArea(0, 0, this.width, this.height));
	}

	public void addWidgets(WidgetBase... widgets) {
		for (WidgetBase widget : widgets) {
			this.root.addWidget(widget);
		}
	}

	public void addWidget(WidgetBase widget) {
		this.root.addWidget(widget);
	}

	public void removeWidget(WidgetBase widget) {
		this.root.removeWidget(widget);
	}

	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		this.root.updateArea(new UIArea(0, 0, width, height));
	}

	@Override
	public final void tick() {
		this.root.preTick();
		doTick();
		this.root.tick();
	}

	public void doTick() {
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
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
		long diffNanos = System.nanoTime() - lastNanos;
		tick += diffNanos / 50_000_000.0f;
		float tickDelta = tick - Mth.floor(tick);
		lastNanos = System.nanoTime();

		renderWidget(this.root, guiGraphics, mouseX, mouseY, tickDelta);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		// Extract data from MouseButtonEvent
		this.root.mouseClicked(event.x(), event.y(), event.button());
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean mouseReleased(MouseButtonEvent event) {
		this.root.mouseReleased(event.x(), event.y(), event.button());
		return super.mouseReleased(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
		this.root.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
		return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
	}

	@Override
	public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
		this.root.mouseDragged(event.x(), event.y(), event.button(), dx, dy);
		return super.mouseDragged(event, dx, dy);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		this.root.keyPressed(event.key(), event.scancode(), event.modifiers());
		return super.keyPressed(event);
	}

	@Override
	public boolean keyReleased(KeyEvent event) {
		this.root.keyReleased(event.key(), event.scancode(), event.modifiers());
		return super.keyReleased(event);
	}

	@Override
	public boolean charTyped(CharacterEvent event) {
		//TODO: Find a better fix for this?
		this.root.charTyped(event.codepointAsString().charAt(0), 0);
		return super.charTyped(event);
	}
}