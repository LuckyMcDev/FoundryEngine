package de.luckymcdev.foundryengine.client.ui;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.ui.screen.EngineScreen;
import de.luckymcdev.foundryengine.client.ui.widget.ButtonWidget;
import de.luckymcdev.foundryengine.client.ui.widget.MinecraftButtonWidget;
import de.luckymcdev.foundryengine.client.ui.widget.MinecraftCheckboxWidget;
import de.luckymcdev.foundryengine.client.ui.widget.MinecraftSliderWidget;
import de.luckymcdev.foundryengine.client.ui.widget.PanelWidget;
import de.luckymcdev.foundryengine.common.util.color.Color;
import dev.vfyjxf.taffy.style.AlignContent;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.network.chat.Component;
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
		panel.setGap(8);

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

		MinecraftButtonWidget mcButton = MinecraftButtonWidget.of(Component.literal("Minecraft Button"), (mouseX, mouseY, btn) -> {
			LOGGER.debug("Minecraft button clicked! Mouse: {} {} | Button ID: {}", mouseX, mouseY, btn);
		});
		mcButton.setSize(100, 20);
		mcButton.setFlexShrink(0);

		panel.addWidget(mcButton);

		MinecraftCheckboxWidget checkbox = MinecraftCheckboxWidget.of(true, Component.literal("Check me"), (box, selected) -> {
			LOGGER.debug("Checkbox toggled: {}", selected);
		});
		checkbox.setSize(120, 20);
		checkbox.setFlexShrink(0);
		checkbox.setTooltip(Component.literal("A vanilla-style checkbox"));

		panel.addWidget(checkbox);

		MinecraftSliderWidget slider = MinecraftSliderWidget.of(0.5, Component.literal("Volume"), (value) -> {
			LOGGER.debug("Slider value: {}", value);
		});
		slider.setSize(120, 20);
		slider.setFlexShrink(0);
		slider.setTooltip(Component.literal("A vanilla-style slider"));

		panel.addWidget(slider);

		this.addWidget(panel);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
