package io.github.luckymcdev.foundryengine.common.thread;

import io.github.luckymcdev.foundryengine.common.Common;
import net.neoforged.bus.api.Event;

public class RegisterEngineThreadEvent extends Event {
    public void register(EngineThread thread) {
        Common.getThreadManager().register(thread);
    }
}
