package io.github.luckymcdev.foundryengine.client.editor.event;

import io.github.luckymcdev.foundryengine.client.editor.Panel;
import io.github.luckymcdev.foundryengine.common.Instances;
import net.neoforged.bus.api.Event;

public class RegisterPanelEvent extends Event {
    public void register(Panel panel) {
        Instances.getBuiltInEditor().register(panel);
    }
}
