package de.luckymcdev.foundryengine.client.ui;

import com.mojang.logging.LogUtils;
import de.luckymcdev.foundryengine.client.Client;
import de.luckymcdev.foundryengine.client.ui.screen.EngineScreen;
import de.luckymcdev.foundryengine.client.ui.widget.ButtonWidget;
import de.luckymcdev.foundryengine.client.ui.widget.PanelWidget;
import de.luckymcdev.foundryengine.common.util.color.Color;
import net.minecraft.world.phys.Vec2;
import org.slf4j.Logger;

public class ExampleScreen extends EngineScreen {
    public static final Logger LOGGER = LogUtils.getLogger();

    public ExampleScreen() {
        super(true);
    }

    @Override
    protected void init() {
        PanelWidget panel = new PanelWidget(
                new UIVec(0.25, 0.25, 0, 0),
                new UIVec(0.5, 0.5, 0, 0)
        );

        panel.setBackgroundColor(Color.LIGHT_GRAY);
        panel.setBorder(Color.RED, 2);

        ButtonWidget button = new ButtonWidget(
                new UIVec(0.5, 0.5, 0, 0),
                new UIVec(0, 0, 100, 20),
                (mouseX, mouseY, btn) -> {
                    LOGGER.debug("Button clicked! Mouse: {} {} | Button ID: {}", mouseX, mouseY, btn);
                    Client.getPlayer().connection.sendChat("HELLO");
                }
        );

        button.setBackgroundColor(Color.DARK_GRAY);
        button.setHoverColor(Color.LIGHT_GRAY);
        button.setBorderColor(Color.BLACK);

        button.setAnchorPoint(new Vec2(0.5f, 0.5f));

        panel.addWidget(button);

        this.addWidgets(panel);

        super.init();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}