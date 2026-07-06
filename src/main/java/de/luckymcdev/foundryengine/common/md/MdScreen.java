package de.luckymcdev.foundryengine.common.md;

import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.common.Common;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.ScrollableLayout;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;

/**
 * Screen that displays GFM markdown content with scrolling support.
 */
public class MdScreen extends Screen {
	private final Component markdownComponent;

	/**
	 * Creates an MdScreen from a pre-rendered Component.
	 */
	public MdScreen(Component title, Component md) {
		super(title);
		this.markdownComponent = md;
	}

	/**
	 * Creates an MdScreen by parsing a markdown string.
	 */
	public MdScreen(Component title, String md) {
		super(title);
		this.markdownComponent = MarkdownParser.parse(md);
	}

	/**
	 * Creates an MdScreen by reading markdown from a file path.
	 */
	public MdScreen(Component title, Path mdFile) {
		super(title);
		this.markdownComponent = MarkdownParser.parse(Common.getFileContent(mdFile));
	}

	/**
	 * Creates an MdScreen by reading markdown from a resource identifier.
	 */
	public MdScreen(Component title, Identifier mdFile) {
		super(title);
		this.markdownComponent = MarkdownParser.parse(Client.getIdSource(mdFile));
	}

	@Override
	protected void init() {
		super.init();

		int padding = 5;
		int titleHeight = 20;

		ScrollableLayout scrollableLayout = getLayout(padding, titleHeight);
		scrollableLayout.setMinWidth(this.width - (padding * 2));
		scrollableLayout.setMaxHeight(this.height - (padding * 2) - titleHeight);
		scrollableLayout.setX(padding);
		scrollableLayout.setY(padding + titleHeight);
		scrollableLayout.arrangeElements();

		scrollableLayout.visitWidgets(this::addRenderableWidget);
	}

	private ScrollableLayout getLayout(int padding, int titleHeight) {
		LinearLayout contentLayout = LinearLayout.vertical();

		MultiLineTextWidget textWidget = new MultiLineTextWidget(
			markdownComponent,
			this.font
		);

		textWidget.setMaxWidth(this.width - (padding * 2) - 20);

		contentLayout.addChild(textWidget);
		contentLayout.arrangeElements();

		return new ScrollableLayout(
			this.minecraft,
			contentLayout,
			this.height - (padding * 2) - titleHeight
		);
	}

	/**
	 * Returns true - this screen never pauses the game.
	 */
	@Override
	public boolean isPauseScreen() {
		return false;
	}
}