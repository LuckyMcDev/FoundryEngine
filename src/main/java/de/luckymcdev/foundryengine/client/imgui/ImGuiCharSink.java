package de.luckymcdev.foundryengine.client.imgui;

import de.luckymcdev.foundryengine.client.Client;
import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiMouseCursor;
import imgui.flag.ImGuiStyleVar;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSink;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.List;

@ApiStatus.Internal
public class ImGuiCharSink implements FormattedCharSink {
	private final StringBuilder buffer;
	private ImFont font;
	private int textColor;
	private HoverEvent hoverEvent;
	private ClickEvent clickEvent;

	public ImGuiCharSink() {
		this.buffer = new StringBuilder();
	}

	private static void openUri(final URI uri) {
		Util.getPlatform().openUri(uri);
	}

	public void setup() {
		this.font = ImGui.getFont();
		this.textColor = ImGraphicsExtractor.getColor(ImGuiCol.Text);
	}

	public void reset() {
		this.font = null;
		this.textColor = 0;
		this.buffer.setLength(0);
		this.hoverEvent = null;
		this.clickEvent = null;
	}

	@Override
	public boolean accept(final int positionInCurrentSequence, final @NotNull Style style, final int codePoint) {
		final ImFont font = ImGraphicsExtractor.getStyleFont(style);
		final int styleColor = style.getColor() != null ? style.getColor().getValue() : this.textColor;
		if (font != this.font || styleColor != this.textColor || style.getHoverEvent() != this.hoverEvent || style.getClickEvent() != this.clickEvent) {
			if (!this.buffer.isEmpty()) {
				this.finish();
			}
			this.font = ImGraphicsExtractor.getStyleFont(style);
			this.textColor = styleColor;
			this.hoverEvent = style.getHoverEvent();
			this.clickEvent = style.getClickEvent();
		}
		this.buffer.appendCodePoint(codePoint);
		return true;
	}

	public void finish() {
		if (!this.buffer.isEmpty()) {
			ImGui.pushStyleVar(ImGuiStyleVar.ItemSpacing, 0, 0);
			ImGui.pushFont(this.font, 0.0F);
			ImGui.textColored(0xFF000000 | (this.textColor & 0xFF0000) >> 16 | (this.textColor & 0xFF00) | (this.textColor & 0xFF) << 16, this.buffer.toString());
			ImGui.popStyleVar();
			this.buffer.setLength(0);

			if (ImGui.isItemClicked() && this.clickEvent != null) {
				this.handleClick();
			}
			if (ImGui.isItemHovered() && this.hoverEvent != null) {
				if (this.clickEvent != null) {
					ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
				}
				this.handleHover();
			}

			ImGui.sameLine();
			ImGui.popFont();
		}
	}

	private void handleClick() {
		final Minecraft minecraft = Minecraft.getInstance();
		switch (this.clickEvent) {
			case ClickEvent.OpenUrl(final URI uri1): {
				if (!minecraft.options.chatLinks().get()) {
					return;
				}

				if (minecraft.options.chatLinksPrompt().get()) {
					final Screen oldScreen = minecraft.screen;
					minecraft.setScreen(new ConfirmLinkScreen((confirm) -> {
						if (confirm) {
							openUri(uri1);
						}

						minecraft.setScreen(oldScreen);
					}, uri1.toString(), false));
				} else {
					openUri(uri1);
				}
				break;
			}
			case final ClickEvent.OpenFile file: {
				openUri(file.file().toURI());
				break;
			}
			case ClickEvent.RunCommand(final String cmd): {
				final LocalPlayer player = Minecraft.getInstance().player;
				if (player == null) {
					break;
				}
				player.connection.sendUnattendedCommand(Commands.trimOptionalPrefix(cmd), minecraft.screen);
				break;
			}
			case ClickEvent.CopyToClipboard(final String text): {
				minecraft.keyboardHandler.setClipboard(text);
				break;
			}
			case ClickEvent.SuggestCommand(final String command): {
				break;
			}
			default: {
				Client.LOGGER.error("Don't know how to handle {}", this.clickEvent);
				break;
			}
		}
	}

	private void handleHover() {
		final Minecraft minecraft = Minecraft.getInstance();
		switch (this.hoverEvent) {
			case HoverEvent.ShowItem(final ItemStackTemplate item): {
				final List<Component> tooltip = Screen.getTooltipFromItem(minecraft, item.create());
				ImGui.beginTooltip();
				for (final Component line : tooltip) {
					ImGraphicsExtractor.component(line, ImGui.getFontSize() * 35.0f);
				}
				ImGui.endTooltip();
				break;
			}
			case HoverEvent.ShowEntity(final HoverEvent.EntityTooltipInfo info): {
				if (minecraft.options.advancedItemTooltips) {
					ImGui.beginTooltip();
					final List<Component> tooltip = info.getTooltipLines();
					for (final Component line : tooltip) {
						ImGraphicsExtractor.component(line, ImGui.getFontSize() * 35.0f);
					}
					ImGui.endTooltip();
				}
				break;
			}
			case HoverEvent.ShowText(final Component component): {
				ImGui.beginTooltip();
				ImGraphicsExtractor.component(component, ImGui.getFontSize() * 35.0f);
				ImGui.endTooltip();
				break;
			}
			default: {
				break;
			}
		}
	}
}
