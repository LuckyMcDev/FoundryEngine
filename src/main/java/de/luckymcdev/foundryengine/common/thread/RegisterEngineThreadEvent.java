package de.luckymcdev.foundryengine.common.thread;

import de.luckymcdev.foundryengine.common.Common;
import net.neoforged.bus.api.Event;

/**
 * Event to Register a new EngineThread.
 * Call via {@link ThreadManager}
 */
public class RegisterEngineThreadEvent extends Event {
    public void register(EngineThread thread) {
        Common.getThreadManager().register(thread);
    }
}
