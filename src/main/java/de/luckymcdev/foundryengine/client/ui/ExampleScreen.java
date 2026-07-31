package de.luckymcdev.foundryengine.client.ui;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.ui.screen.EngineScreen;
import de.luckymcdev.foundryengine.client.ui.widget.ButtonWidget;
import de.luckymcdev.foundryengine.client.ui.widget.PanelWidget;
import de.luckymcdev.foundryengine.common.util.color.Color;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import org.slf4j.Logger;

public class ExampleScreen extends EngineScreen {
	public static final Logger LOGGER = LogUtils.getLogger();

	public ExampleScreen() {
		super(true);
	}

	@Override
	protected void init() {
		if (shouldBuildWidgets()) {
			buildWidgets();
		}
		super.init();
	}

	private void buildWidgets() {
		PanelWidget panel = new PanelWidget();
		panel.setPositionAbsolute();
		panel.setInsetLeftPercent(0.25f);
		panel.setInsetTopPercent(0.25f);
		panel.setWidthPercent(0.5f);
		panel.setHeightPercent(0.5f);
		panel.setFlexDirection(FlexDirection.ROW);
		panel.setJustifyContent(AlignContent.CENTER);
		panel.setAlignItems(AlignItems.CENTER);

		panel.setBackgroundColor(Color.LIGHT_GRAY);
		panel.setBorder(Color.RED, 2);

		ButtonWidget button = new ButtonWidget((mouseX, mouseY, btn) -> {
			LOGGER.debug("Button clicked! Mouse: {} {} | Button ID: {}", mouseX, mouseY, btn);
			Client.getPlayer().connection.sendChat("HELLO");
		});
		button.setSize(100, 20);
		button.setFlexShrink(0);

		button.setBackgroundColor(Color.DARK_GRAY);
		button.setHoverColor(Color.LIGHT_GRAY);
		button.setBorderColor(Color.BLACK);

		panel.addWidget(button);

		this.addWidget(panel);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
