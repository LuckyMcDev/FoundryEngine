package de.luckymcdev.foundryengine.common.event;

import com.mojang.logging.LogUtils;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class EventGroup<T> {
    private static final Logger LOGGER = LogUtils.getLogger();

    private final List<EventCallback<T>> listeners = new CopyOnWriteArrayList<>();

    public void add(EventCallback<T> callback) {
        listeners.add(callback);
    }

    public void post(T event) {
        for (EventCallback<T> listener : listeners) {
            try {
                listener.execute(event);
            } catch (Throwable e) {
                LOGGER.error("Uncaught error in event callback", e);
                var server = ServerLifecycleHooks.getCurrentServer();
                String loc = e.getStackTrace().length > 0 ? " (" + e.getStackTrace()[0].getFileName() + ":" + e.getStackTrace()[0].getLineNumber() + ")" : "";
                if (server != null) {
                    server.getPlayerList().broadcastSystemMessage(
                            Component.literal("§c[Script Error] Event callback: " + e + loc), false);
                } else {
                    LOGGER.error("Uncaught error in event callback (no server): {}", e.toString());
                }
            }
        }
    }

    public void clear() {
        this.listeners.clear();
    }
}