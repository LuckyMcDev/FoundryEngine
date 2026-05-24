package de.luckymcdev.foundryengine.common.event;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.ModLoader;
import net.neoforged.fml.ModLoadingIssue;
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
            } catch (Exception e) {
                LOGGER.error("Uncaught error in event callback", e);
                ModLoadingIssue issue = ModLoadingIssue.error(
                        "Uncaught error in event callback: " + e.getMessage());
                ModLoader.addLoadingIssue(issue);
            }
        }
    }

    public void clear() {
        this.listeners.clear();
    }
}