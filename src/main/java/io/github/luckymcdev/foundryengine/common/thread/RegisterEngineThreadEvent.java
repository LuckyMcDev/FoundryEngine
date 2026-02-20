package io.github.luckymcdev.foundryengine.common.thread;

import io.github.luckymcdev.foundryengine.common.Instances;
import net.neoforged.bus.api.Event;

public class RegisterEngineThreadEvent extends Event {
    public void register(EngineThread thread) {
        Instances.getThreadManager().register(thread);
    }
}
