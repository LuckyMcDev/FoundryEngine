package de.luckymcdev.foundryengine.script.client;

import de.luckymcdev.foundryengine.Client;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class MaliciousScriptScreen extends Screen {
	private static final Component TITLE = Component.literal("Warning: Script Sandbox Disabled")
		.withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
	private static final Component MESSAGE = Component.literal(
		"The developer of this modpack has disabled the script classloader restrictions.\n\n"
			+ "Scripts can now access arbitrary Java classes, which means they can read and write files, "
			+ "run programs and access the network on your computer.\n\n"
			+ "Only continue if you trust the source of this modpack.");

	private final Screen parent;
	private final Runnable onAccept;
	private final Runnable onDecline;

	public MaliciousScriptScreen(Screen parent, Runnable onAccept, Runnable onDecline) {
		super(TITLE);
		this.parent = parent;
		this.onAccept = onAccept;
		this.onDecline = onDecline;
	}

	@Override
	protected void init() {
		var titleWidget = new StringWidget(this.title, font);
		titleWidget.setPosition((width - titleWidget.getWidth()) / 2, 40);
		addRenderableWidget(titleWidget);

		var message = new MultiLineTextWidget(MESSAGE, font);
		message.setMaxWidth(width - 80);
		message.setCentered(true);
		message.setPosition(40, 70);
		addRenderableWidget(message);

		addRenderableWidget(Button.builder(Component.literal("I understand the risks"), b -> finish(onAccept))
			.bounds(width / 2 - 155, height - 50, 150, 20)
			.build());
		addRenderableWidget(Button.builder(Component.literal("Keep sandbox enabled"), b -> finish(onDecline))
			.bounds(width / 2 + 5, height - 50, 150, 20)
			.build());
	}

	private void finish(Runnable action) {
		action.run();
		Client.setScreen(parent);
	}

	@Override
	public boolean shouldCloseOnEsc() {
		return false;
	}
}